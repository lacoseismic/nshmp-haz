package gov.usgs.earthquake.nshmp.www;

import static com.google.common.base.Strings.isNullOrEmpty;
import static gov.usgs.earthquake.nshmp.www.meta.Region.CEUS;
import static gov.usgs.earthquake.nshmp.www.meta.Region.COUS;
import static gov.usgs.earthquake.nshmp.www.meta.Region.WUS;
import static java.lang.Runtime.getRuntime;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.ValueFormat;
import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.eq.model.HazardModel;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.www.meta.Edition;
import gov.usgs.earthquake.nshmp.www.meta.ParamType;
import gov.usgs.earthquake.nshmp.www.meta.Region;
import gov.usgs.earthquake.nshmp.www.meta.Util;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.EmbeddedServer;

/**
 * Micronaut controller utility objects and methods.
 *
 * @author Peter Powers
 */
public class ServletUtil {

  @Inject
  private EmbeddedServer server;

  @Value("${nshmp-haz.installed-model}")
  private Model model;
  public static Model INSTALLED_MODEL;
  public static HazardModel HAZARD_MODEL;

  public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ssXXX");

  static final ListeningExecutorService CALC_EXECUTOR;
  static final ExecutorService TASK_EXECUTOR;

  public static final int THREAD_COUNT;

  public static final Gson GSON;

  /* Stateful flag to reject requests while a result is pending. */
  static boolean uhtBusy = false;
  static long hitCount = 0;
  static long missCount = 0;

  static {
    /* TODO modified for deagg-epsilon branch; should be context var */
    THREAD_COUNT = getRuntime().availableProcessors();
    CALC_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(THREAD_COUNT));
    TASK_EXECUTOR = Executors.newSingleThreadExecutor();
    GSON = new GsonBuilder()
        .registerTypeAdapter(Edition.class, new Util.EnumSerializer<Edition>())
        .registerTypeAdapter(Region.class, new Util.EnumSerializer<Region>())
        .registerTypeAdapter(Imt.class, new Util.EnumSerializer<Imt>())
        .registerTypeAdapter(Vs30.class, new Util.EnumSerializer<Vs30>())
        .registerTypeAdapter(ValueFormat.class, new Util.EnumSerializer<ValueFormat>())
        .registerTypeAdapter(Double.class, new Util.DoubleSerializer())
        .registerTypeAdapter(ParamType.class, new Util.ParamTypeSerializer())
        .registerTypeAdapter(Site.class, new Util.SiteSerializer())
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()
        .create();
  }

  @EventListener
  void shutdown(ShutdownEvent event) {
    CALC_EXECUTOR.shutdown();
    TASK_EXECUTOR.shutdown();
  }

  @EventListener
  void startup(StartupEvent event) {
    INSTALLED_MODEL = model;
    HAZARD_MODEL = loadModel(model);
  }

  private HazardModel loadModel(Model model) {
    Path path;
    URL url;
    URI uri;
    String uriString;
    String[] uriParts;
    FileSystem fs;

    try {
      url = Paths.get(model.path).toUri().toURL();
      uri = new URI(url.toString().replace(" ", "%20"));
      uriString = uri.toString();

      /*
       * When the web sevice is deployed inside a JAR file (and not unpacked by
       * the servlet container) model resources will not exist on disk as
       * otherwise expected. In this case, load the resources directly out of
       * the JAR file as well. This is slower, but with the preload option
       * enabled it may be less of an issue if the models are already in memory.
       */

      if (uriString.indexOf("!") != -1) {
        uriParts = uri.toString().split("!");

        try {
          fs = FileSystems.getFileSystem(
              URI.create(uriParts[0]));
        } catch (FileSystemNotFoundException fnx) {
          fs = FileSystems.newFileSystem(
              URI.create(uriParts[0]),
              new HashMap<String, String>());
        }

        path = fs.getPath(uriParts[1].replaceAll("%20", " "));
      } else {
        path = Paths.get(uri);
      }

      return HazardModel.load(path);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static boolean emptyRequest(HttpServletRequest request) {
    return isNullOrEmpty(request.getQueryString()) &&
        (request.getPathInfo() == null || request.getPathInfo().equals("/"));
  }

  public static Timer timer() {
    return new Timer();
  }

  /*
   * Simple timer object. The servlet timer just runs. The calculation timer can
   * be started later.
   */
  public static final class Timer {

    Stopwatch servlet = Stopwatch.createStarted();
    Stopwatch calc = Stopwatch.createUnstarted();

    Timer start() {
      calc.start();
      return this;
    }

    public String servletTime() {
      return servlet.toString();
    }

    public String calcTime() {
      return calc.toString();
    }
  }

  abstract static class TimedTask<T> implements Callable<T> {

    final String url;
    final ServletContext context;
    final Timer timer;

    TimedTask(String url, ServletContext context) {
      this.url = url;
      this.context = context;
      this.timer = ServletUtil.timer();
    }

    abstract T calc() throws Exception;

    @Override
    public T call() throws Exception {
      timer.start();
      return calc();
    }
  }

  /*
   * For sites located west of -115 (in the WUS but not in the CEUS-WUS overlap
   * zone) and site classes of vs30=760, client requests come in with
   * region=COUS, thereby limiting the conversion of imt=any to the set of
   * periods supported by both models. In order for the service to return what
   * the client suggests should be returned, we need to do an addiitional
   * longitude check. TODO clean; fix client eq-hazard-tool
   */
  static Region checkRegion(Region region, double lon) {
    if (region == COUS) {
      return (lon <= WUS.uimaxlongitude) ? WUS : (lon >= CEUS.uiminlongitude) ? CEUS : COUS;
    }
    return region;
  }

}

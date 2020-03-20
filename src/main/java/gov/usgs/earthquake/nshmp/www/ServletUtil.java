package gov.usgs.earthquake.nshmp.www;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Runtime.getRuntime;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import gov.usgs.earthquake.nshmp.internal.www.meta.ParamType;
import gov.usgs.earthquake.nshmp.www.meta.MetaUtil;
import gov.usgs.earthquake.nshmp.www.meta.Region;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

/**
 * Micronaut controller utility objects and methods.
 *
 * @author U.S. Geological Survey
 */
public class ServletUtil {

  @Value("${nshmp-haz.installed-model}")
  private Model model;

  private static Model INSTALLED_MODEL;
  private static Map<BaseModel, HazardModel> HAZARD_MODELS = new EnumMap<>(BaseModel.class);
  public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ssXXX");

  public static final ListeningExecutorService CALC_EXECUTOR;
  public static final ExecutorService TASK_EXECUTOR;

  public static final int THREAD_COUNT;

  public static final Gson GSON;

  /* Stateful flag to reject requests while a result is pending. */
  public static boolean uhtBusy = false;
  public static long hitCount = 0;
  public static long missCount = 0;

  static {
    /* TODO modified for deagg-epsilon branch; should be context var */
    THREAD_COUNT = getRuntime().availableProcessors();
    CALC_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(THREAD_COUNT));
    TASK_EXECUTOR = Executors.newSingleThreadExecutor();
    GSON = new GsonBuilder()
        .registerTypeAdapter(Region.class, new MetaUtil.EnumSerializer<Region>())
        .registerTypeAdapter(Imt.class, new MetaUtil.EnumSerializer<Imt>())
        .registerTypeAdapter(Vs30.class, new MetaUtil.EnumSerializer<Vs30>())
        .registerTypeAdapter(ValueFormat.class, new MetaUtil.EnumSerializer<ValueFormat>())
        .registerTypeAdapter(Double.class, new MetaUtil.DoubleSerializer())
        .registerTypeAdapter(ParamType.class, new MetaUtil.ParamTypeSerializer())
        .registerTypeAdapter(Site.class, new MetaUtil.SiteSerializer())
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()
        .create();
  }

  public static Model installedModel() {
    return INSTALLED_MODEL;
  }

  public static Map<BaseModel, HazardModel> hazardModels() {
    return HAZARD_MODELS;
  }

  @EventListener
  void shutdown(ShutdownEvent event) {
    CALC_EXECUTOR.shutdown();
    TASK_EXECUTOR.shutdown();
  }

  @EventListener
  void startup(StartupEvent event) {
    INSTALLED_MODEL = model;

    model.models().forEach(baseModel -> {
      HAZARD_MODELS.put(baseModel, loadModel(baseModel));
    });

    HAZARD_MODELS = Map.copyOf(HAZARD_MODELS);
  }

  private HazardModel loadModel(BaseModel model) {
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

  public abstract static class TimedTask<T> implements Callable<T> {

    final String url;
    final Timer timer;

    public TimedTask(String url) {
      this.url = url;
      this.timer = ServletUtil.timer();
    }

    public abstract T calc() throws Exception;

    @Override
    public T call() throws Exception {
      timer.start();
      return calc();
    }
  }

  public abstract static class TimedTaskContext<T> extends TimedTask<T> {
    ServletContext context;

    public TimedTaskContext(String url, ServletContext context) {
      super(url);
      this.context = context;
    }
  }

}

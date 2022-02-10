package gov.usgs.earthquake.nshmp.www;

import static java.lang.Runtime.getRuntime;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.ValueFormat;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.SiteData;
import gov.usgs.earthquake.nshmp.www.meta.MetaUtil;

import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.http.HttpResponse;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

/**
 * Micronaut controller utility objects and methods.
 *
 * @author U.S. Geological Survey
 */
@Singleton
public class ServletUtil {

  public static final Gson GSON;
  public static final Gson GSON2;

  public static final ListeningExecutorService CALC_EXECUTOR;
  public static final ExecutorService TASK_EXECUTOR;

  public static final int THREAD_COUNT;

  @Value("${nshmp-haz.model-path}")
  private Path modelPath;

  private static HazardModel HAZARD_MODEL;

  static {
    THREAD_COUNT = getRuntime().availableProcessors();
    CALC_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(THREAD_COUNT));
    TASK_EXECUTOR = Executors.newSingleThreadExecutor();
    GSON = new GsonBuilder()
        .registerTypeAdapter(Imt.class, new WsUtils.EnumSerializer<Imt>())
        .registerTypeAdapter(ValueFormat.class, new WsUtils.EnumSerializer<ValueFormat>())
        .registerTypeAdapter(Double.class, new WsUtils.DoubleSerializer())
        .registerTypeAdapter(Site.class, new MetaUtil.SiteSerializer())
        .registerTypeHierarchyAdapter(Path.class, new PathConverter())
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()
        .create();

    // removed old IMT and ValueFormat enum serialization
    GSON2 = new GsonBuilder()
        .registerTypeAdapter(Double.class, new WsUtils.DoubleSerializer())
        .registerTypeAdapter(Site.class, new MetaUtil.SiteSerializer())
        .registerTypeHierarchyAdapter(Path.class, new PathConverter())
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()
        .create();

  }

  public static HazardModel model() {
    return HAZARD_MODEL;
  }

  @EventListener
  void shutdown(ShutdownEvent event) {
    CALC_EXECUTOR.shutdown();
    TASK_EXECUTOR.shutdown();
  }

  @EventListener
  void startup(StartupEvent event) {
    HAZARD_MODEL = loadModel(modelPath);
  }

  private HazardModel loadModel(Path path) {
    URL url;
    URI uri;
    String uriString;
    String[] uriParts;
    FileSystem fs;

    try {
      url = path.toUri().toURL();
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

  private static class PathConverter implements JsonSerializer<Path> {
    @Override
    public JsonElement serialize(
        Path path,
        Type type,
        JsonSerializationContext context) {
      return new JsonPrimitive(path.toAbsolutePath().normalize().toString());
    }
  }

  public static HttpResponse<String> error(
      Logger logger,
      Throwable e,
      String name,
      String url) {
    var msg = e.getMessage() + " (see logs)";
    var svcResponse = ResponseBody.error()
        .name(name)
        .url(url)
        .request(url)
        .response(msg)
        .build();
    var response = GSON2.toJson(svcResponse);
    logger.error("Servlet error", e);
    return HttpResponse.serverError(response);
  }

  public static String imtShortLabel(Imt imt) {
    if (imt.equals(Imt.PGA) || imt.equals(Imt.PGV)) {
      return imt.name();
    } else if (imt.isSA()) {
      return imt.period() + " s";
    }
    return imt.toString();
  }

  public static Object serverData(int threads, Stopwatch timer) {
    return new Server(threads, timer);
  }

  public static Site createSite(Location location, double vs30, SiteData siteData) {
    Site.Builder builder = Site.builder()
        .location(location)
        .vs30(vs30);
    SiteData.Values sdValues = siteData.get(location);
    sdValues.z1p0.ifPresent(builder::z1p0);
    sdValues.z2p5.ifPresent(builder::z2p5);
    return builder.build();
  }

  private static class Server {

    final int threads;
    final String timer;
    final String version;

    Server(int threads, Stopwatch timer) {
      this.threads = threads;
      this.timer = timer.toString();
      this.version = "TODO where to get version?";
    }
  }

}

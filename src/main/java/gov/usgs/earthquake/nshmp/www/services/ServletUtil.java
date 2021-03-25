package gov.usgs.earthquake.nshmp.www.services;

import static java.lang.Runtime.getRuntime;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.WsUtils;
import gov.usgs.earthquake.nshmp.www.meta.MetaUtil;

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

  public static final Gson GSON;
  public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd'T'HH:mm:ssXXX");

  static final ListeningExecutorService CALC_EXECUTOR;
  static final ExecutorService TASK_EXECUTOR;

  static final int THREAD_COUNT;

  /* Stateful flag to reject requests while a result is pending. */
  static boolean uhtBusy = false;
  static long hitCount = 0;
  static long missCount = 0;

  @Value("${nshmp-haz.model-path}")
  private Path modelPath;

  // private static List<HazardModel> HAZARD_MODELS = new ArrayList<>();

  private static HazardModel HAZARD_MODEL;
  private static final String MODEL_INFO = "model-info.json";

  static {
    /* TODO modified for deagg-epsilon branch; should be context var */
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
  }

  // static List<HazardModel> hazardModels() {
  // return List.copyOf(HAZARD_MODELS);
  // }

  static HazardModel model() {
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
    // TODO should model path just be libs/model
    // try {
    // var modelFinder = new ModelFinder();
    // Files.walkFileTree(modelPath, modelFinder);
    // modelFinder.paths().forEach(path -> HAZARD_MODELS.add(loadModel(path)));
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
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

  public static Timer timer() {
    return new Timer();
  }

  /*
   * Simple timer object. The servlet timer just runs. The calculation timer can
   * be started later.
   */
  @Deprecated
  public static final class Timer {
    Stopwatch servlet = Stopwatch.createStarted();
    Stopwatch calc = Stopwatch.createUnstarted();

    public Timer start() {
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

  // private static class ModelFinder extends SimpleFileVisitor<Path> {
  // private List<Path> paths;
  //
  // ModelFinder() {
  // paths = new ArrayList<>();
  // }
  //
  // List<Path> paths() {
  // return List.copyOf(paths);
  // }
  //
  // @Override
  // public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
  // var fileName = path.getFileName();
  //
  // if (fileName != null && fileName.toString().equals(MODEL_INFO)) {
  // paths.add(path.getParent());
  // }
  //
  // return FileVisitResult.CONTINUE;
  // }
  // }

  private static class PathConverter implements JsonSerializer<Path> {

    @Override
    public JsonElement serialize(
        Path path,
        Type type,
        JsonSerializationContext context) {
      return new JsonPrimitive(path.toAbsolutePath().normalize().toString());
    }
  }

}

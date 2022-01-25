package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Disaggregation;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Sites;
import gov.usgs.earthquake.nshmp.calc.ThreadCount;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.Logging;
import gov.usgs.earthquake.nshmp.model.HazardModel;

/**
 * Disaggregate probabilistic seismic hazard at a return period of interest or
 * at specific ground motion levels.
 *
 * @author U.S. Geological Survey
 */
public class DisaggEpsilon {

  private static final Gson GSON = new GsonBuilder()
      .serializeSpecialFloatingPointValues()
      .serializeNulls()
      .create();

  /**
   * Entry point for the disaggregation of probabilisitic seismic hazard.
   *
   * <p>Two approaches to disaggregation of seimic hazard are possible with this
   * application. In the first approach, the 'sites' file is the same as it
   * would be for a hazard calculation, and disaggregation is performed for all
   * configured intensity measures at the 'returnPeriod' (in years) of interest
   * specified in the config file (default = 2475 years).
   *
   * <p>In the second approach, the sites file includes columns for each
   * spectral period and the target ground motion level to disaggregate for
   * each. For example, the target values could be a risk-targeted response
   * spectrum, or they could be ground motion levels precomputed for a specific
   * return period.
   *
   * <p>It is important to note that the first approach will do the full hazard
   * calculation and compute hazard curves from which the target disaggregation
   * ground motion level will be determined. In the second approach, the ground
   * motion targets are known and the time consuming hazard curve calculation
   * can be avoided.
   *
   * <p>Please refer to the nshmp-haz <a
   * href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/docs">
   * docs</a> for comprehensive descriptions of source models, configuration
   * files, site files, and hazard calculations.
   *
   * @see <a
   *      href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/blob/main/docs/pages/Building-&-Running.md">
   *      nshmp-haz Building & Running</a>
   * @see <a
   *      href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples">
   *      example calculations</a>
   */
  public static void main(String[] args) {

    /* Delegate to run which has a return value for testing. */

    Optional<String> status = run(args);
    if (status.isPresent()) {
      System.err.print(status.get());
      System.exit(1);
    }
    System.exit(0);
  }

  static Optional<String> run(String[] args) {
    int argCount = args.length;

    if (argCount < 2 || argCount > 3) {
      return Optional.of(USAGE);
    }

    Logging.init();
    Logger log = Logger.getLogger(DisaggCalc.class.getName());
    Path tmpLog = HazardCalc.createTempLog();

    try {
      FileHandler fh = new FileHandler(Preconditions.checkNotNull(tmpLog.getFileName()).toString());
      fh.setFormatter(new Logging.ConsoleFormatter());
      log.getParent().addHandler(fh);

      log.info(PROGRAM + ": " + HazardCalc.VERSION);
      Path modelPath = Paths.get(args[0]);
      HazardModel model = HazardModel.load(modelPath);

      log.info("");
      Path siteFile = Paths.get(args[1]);
      log.info("Site and spectra file: " + siteFile.toAbsolutePath().normalize());
      checkArgument(siteFile.toString().endsWith(".csv"), "Only *.csv site files supported");

      int colsToSkip = headerCount(siteFile);
      List<Imt> imts = readImtList(siteFile, colsToSkip);

      CalcConfig config = model.config();
      if (argCount == 3) {
        Path userConfigPath = Paths.get(args[2]);
        config = CalcConfig.copyOf(model.config())
            .extend(CalcConfig.from(userConfigPath))
            .build();
      }
      log.info(config.toString());

      List<Site> sites = ImmutableList.copyOf(Sites.fromCsv(siteFile, config, model.siteData()));
      log.info("Sites: " + sites.size());

      log.info("Site data columns: " + colsToSkip);
      List<Map<Imt, Double>> imtImlMaps = readSpectra(siteFile, imts, colsToSkip);
      log.info("Spectra: " + imtImlMaps.size());

      checkArgument(sites.size() == imtImlMaps.size(), "Sites and spectra lists different sizes");

      Path out = calc(model, config, sites, imtImlMaps, log);

      log.info(PROGRAM + ": finished");

      /* Transfer log and write config, windows requires fh.close() */
      fh.close();
      Files.move(tmpLog, out.resolve(PROGRAM + ".log"));
      config.write(out);

      return Optional.empty();

    } catch (Exception e) {
      return HazardCalc.handleError(e, log, tmpLog, args, PROGRAM, USAGE);
    }
  }

  // TODO removed this set from Site; temp repair
  static final Set<String> SITE_KEYS = ImmutableSet.of(
      "name",
      "lat",
      "lon",
      "vs30",
      "vsInf",
      "z1p0",
      "z2p5");

  /* returns the number of site data columns are present. */
  private static int headerCount(Path path) throws IOException {
    String header = Files.lines(path).findFirst().get();
    Set<String> columns = ImmutableSet.copyOf(Splitter.on(',').trimResults().split(header));
    return Sets.intersection(columns, SITE_KEYS).size();
  }

  private static List<Imt> readImtList(Path path, int colsToSkip) throws IOException {
    String header = Files.lines(path).findFirst().get();
    return Splitter.on(',')
        .trimResults()
        .splitToList(header)
        .stream()
        .skip(colsToSkip)
        .map(Imt::valueOf)
        .collect(ImmutableList.toImmutableList());
  }

  private static List<Map<Imt, Double>> readSpectra(Path path, List<Imt> imts, int colsToSkip)
      throws IOException {
    return Files.lines(path)
        .skip(1)
        .map(s -> readSpectra(imts, s, colsToSkip))
        .collect(ImmutableList.toImmutableList());
  }

  private static Map<Imt, Double> readSpectra(List<Imt> imts, String line, int colsToSkip) {

    double[] imls = Splitter.on(',')
        .trimResults()
        .splitToList(line)
        .stream()
        .skip(colsToSkip)
        .mapToDouble(Double::valueOf)
        .toArray();

    EnumMap<Imt, Double> imtImlMap = new EnumMap<>(Imt.class);
    for (int i = 0; i < imts.size(); i++) {
      imtImlMap.put(imts.get(i), imls[i]);
    }
    return imtImlMap;
  }

  /*
   * Compute hazard curves using the supplied model, config, and sites. Method
   * returns the path to the directory where results were written.
   *
   * TODO consider refactoring to supply an Optional<Double> return period to
   * HazardCalc.calc() that will trigger disaggregations if the value is
   * present.
   */
  private static Path calc(
      HazardModel model,
      CalcConfig config,
      List<Site> sites,
      List<Map<Imt, Double>> rtrSpectra,
      Logger log) throws IOException {

    ExecutorService exec = null;
    ThreadCount threadCount = config.performance.threadCount;
    if (threadCount == ThreadCount.ONE) {
      exec = MoreExecutors.newDirectExecutorService();
      log.info("Threads: Running on calling thread");
    } else {
      exec = Executors.newFixedThreadPool(threadCount.value());
      log.info("Threads: " + ((ThreadPoolExecutor) exec).getCorePoolSize());
    }

    log.info(PROGRAM + ": calculating ...");
    Path outDir = createOutputDir(config.output.directory);
    Path siteDir = outDir.resolve("vs30-" + (int) sites.get(0).vs30());
    Files.createDirectory(siteDir);

    Stopwatch stopwatch = Stopwatch.createStarted();

    for (int i = 0; i < sites.size(); i++) {

      Site site = sites.get(i);
      Map<Imt, Double> spectrum = rtrSpectra.get(i);

      // use IMLs from site spectra
      Hazard hazard = HazardCalcs.hazard(model, config, site, exec);
      Disaggregation disagg = Disaggregation.atImls(hazard, spectrum, exec);

      List<Response> responses = new ArrayList<>(spectrum.size());
      for (Imt imt : spectrum.keySet()) {
        ResponseData imtMetadata = new ResponseData(
            ImmutableList.of(),
            site,
            imt,
            spectrum.get(imt));
        Response response = new Response(
            imtMetadata,
            disagg.toJson(imt, false, true, true, false));
        responses.add(response);
      }
      Result result = new Result(responses);

      String filename = String.format(
          "edisagg_%.2f_%.2f.json",
          site.location().longitude,
          site.location().latitude);

      Path resultPath = siteDir.resolve(filename);
      Writer writer = Files.newBufferedWriter(resultPath);
      GSON.toJson(result, writer);
      writer.close();
      log.info(String.format(
          "     %s of %s sites completed in %s",
          i + 1, sites.size(), stopwatch));
    }

    exec.shutdown();
    return siteDir;
  }

  private static class Result {

    final List<Response> response;

    Result(List<Response> response) {
      this.response = response;
    }
  }

  private static final class ResponseData {

    final List<String> models;
    final double longitude;
    final double latitude;
    final String imt;
    final double iml;
    final double vs30;

    ResponseData(List<String> models, Site site, Imt imt, double iml) {
      this.models = models;
      this.longitude = site.location().longitude;
      this.latitude = site.location().latitude;
      this.imt = imt.toString();
      this.iml = iml;
      this.vs30 = site.vs30();
    }
  }

  private static final class Response {

    final ResponseData metadata;
    final Object data;

    Response(ResponseData metadata, Object data) {
      this.metadata = metadata;
      this.data = data;
    }
  }

  static Path createOutputDir(Path dir) throws IOException {
    int i = 1;
    Path incrementedDir = dir;
    while (Files.exists(incrementedDir)) {
      incrementedDir = incrementedDir.resolveSibling(dir.getFileName() + "-" + i);
      i++;
    }
    Files.createDirectories(incrementedDir);
    return incrementedDir;
  }

  private static final String PROGRAM = DisaggEpsilon.class.getSimpleName();
  private static final String USAGE_COMMAND =
      "java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.DisaggEpsilon model sites [config]";

  private static final String USAGE = new StringBuilder()
      .append(NEWLINE)
      .append(PROGRAM).append(" [").append(HazardCalc.VERSION).append("]").append(NEWLINE)
      .append(NEWLINE)
      .append("Usage:").append(NEWLINE)
      .append("  ").append(USAGE_COMMAND).append(NEWLINE)
      .append(NEWLINE)
      .append("Where:").append(NEWLINE)
      .append("  'model' is a model directory")
      .append(NEWLINE)
      .append(
          "  'sites' is a *.csv file of locations, site parameters and (optional) target ground motion levels")
      .append(NEWLINE)
      .append("     - Header: lon,lat,PGA,SA0P01,SA0P02,...")
      .append(NEWLINE)
      .append("       (spectral periods must be ascending)")
      .append(NEWLINE)
      .append("  'config' (optional) supplies a calculation configuration")
      .append(NEWLINE)
      .toString();

}

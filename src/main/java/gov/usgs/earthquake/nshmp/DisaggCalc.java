package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;
import static gov.usgs.earthquake.nshmp.calc.DataType.DISAGG_DATA;
import static gov.usgs.earthquake.nshmp.calc.DataType.GMM;
import static gov.usgs.earthquake.nshmp.calc.DataType.SOURCE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Disaggregation;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.HazardExport;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Sites;
import gov.usgs.earthquake.nshmp.calc.ThreadCount;
import gov.usgs.earthquake.nshmp.data.Interpolator;
import gov.usgs.earthquake.nshmp.data.XySequence;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.Logging;
import gov.usgs.earthquake.nshmp.model.HazardModel;

/**
 * Disaggregate probabilistic seismic hazard at a return period of interest or
 * at specific ground motion levels.
 *
 * @author U.S. Geological Survey
 */
public class DisaggCalc {

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
   * specified in the config file (default = 2475 years, equivalent to 2% in 50
   * years).
   *
   * <p>In the second approach, the sites file includes columns for each
   * spectral period or other intensity measure and the target ground motion
   * level to disaggregate for each. For example, the target values could be a
   * risk-targeted spectral accelerations, or they could be ground motion levels
   * precomputed for a specific return period.
   *
   * <p>Note that the first approach will do the full hazard calculation and
   * compute hazard curves from which the target disaggregation ground motion
   * level will be determined. In the second approach, the ground motion targets
   * are known and the time consuming hazard curve calculation can be avoided.
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
    String tmpLogName = checkNotNull(tmpLog.getFileName()).toString();

    try {
      FileHandler fh = new FileHandler(tmpLogName);
      fh.setFormatter(new Logging.ConsoleFormatter());
      log.getParent().addHandler(fh);

      log.info(PROGRAM + ": " + HazardCalc.VERSION);
      Path modelPath = Paths.get(args[0]);
      HazardModel model = HazardModel.load(modelPath);

      log.info("");
      Path siteFile = Paths.get(args[1]);
      log.info("Site file: " + siteFile.toAbsolutePath().normalize());
      checkArgument(
          siteFile.toString().endsWith(".csv"),
          "Only *.csv site files supported");

      /* Calculation configuration, possibly user supplied. */
      CalcConfig config = model.config();
      if (argCount == 3) {
        Path userConfigPath = Paths.get(args[2]);
        config = CalcConfig.copyOf(model.config())
            .extend(CalcConfig.from(userConfigPath))
            .build();
      }
      log.info(config.toString());

      /* Column header data. */
      Set<String> allColumns = columns(siteFile);
      Set<String> siteColumns = new HashSet<>(allColumns);
      siteColumns.retainAll(SITE_KEYS);
      int colsToSkip = siteColumns.size(); // needed?
      log.info("Site data columns: " + colsToSkip);

      /* Sites */
      List<Site> sites = Sites.fromCsv(siteFile, config, model.siteData());
      log.info("Sites: " + sites.size());

      Set<Imt> modelImts = model.config().hazard.imts;

      /*
       * If no IML columns present, disaggregate at IMTs and return period from
       * config, otherwise disaggregate at target IMLs are present.
       *
       * We've removed support for gejson site files at present.
       */
      Path out;
      if (siteColumns.size() == allColumns.size()) {

        checkArgument(
            modelImts.containsAll(config.hazard.imts),
            "Config specifies IMTs not supported by model");
        double returnPeriod = config.disagg.returnPeriod;
        out = calcRp(model, config, sites, returnPeriod, log);

      } else {

        List<Imt> imts = readImtList(siteFile, colsToSkip);
        checkArgument(
            modelImts.containsAll(imts),
            "Sites file contains IMTs not supported by model");
        List<Map<Imt, Double>> imls = readSpectra(siteFile, imts, colsToSkip);
        checkArgument(
            sites.size() == imls.size(),
            "Sites and spectra lists different sizes");
        log.info("Spectra: " + imls.size()); // 1:1 with sites
        out = calcIml(model, config, sites, imls, log);

      }

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

  private static final Set<String> SITE_KEYS = ImmutableSet.of(
      Site.Key.NAME,
      Site.Key.LAT,
      Site.Key.LON,
      Site.Key.VS30,
      Site.Key.VS_INF,
      Site.Key.Z1P0,
      Site.Key.Z2P5);

  private static Set<String> columns(Path path) throws IOException {
    String header = Files.lines(path).findFirst().get();
    return Arrays.stream(header.split(","))
        .map(String::trim)
        .collect(toSet());
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
   */
  private static Path calcRp(
      HazardModel model,
      CalcConfig config,
      List<Site> sites,
      double returnPeriod,
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

    log.info(PROGRAM + " (return period): calculating ...");

    HazardExport handler = HazardExport.create(model, config, sites, log);
    Path disaggDir = handler.outputDir().resolve("disagg");
    Files.createDirectory(disaggDir);

    Stopwatch stopwatch = Stopwatch.createStarted();
    int logInterval = sites.size() < 100 ? 1 : sites.size() < 1000 ? 10 : 100;

    for (int i = 0; i < sites.size(); i++) {
      Site site = sites.get(i);

      Hazard hazard = HazardCalcs.hazard(model, config, site, exec);
      handler.write(hazard);

      Map<Imt, Double> imls = imlsForReturnPeriod(hazard, returnPeriod);
      Disaggregation disagg = Disaggregation.atImls(hazard, imls, exec);

      Response response = new Response.Builder()
          .config(config)
          .site(site)
          .returnPeriod(returnPeriod)
          .imls(imls)
          .disagg(disagg)
          .build();

      String filename = disaggFilename(site);
      Path resultPath = disaggDir.resolve(filename);
      Writer writer = Files.newBufferedWriter(resultPath);
      GSON.toJson(response, writer);
      writer.close();

      if (i % logInterval == 0) {
        log.info(String.format(
            "     %s of %s sites completed in %s",
            i + 1, sites.size(), stopwatch));
      }
    }
    handler.expire();

    log.info(String.format(
        PROGRAM + " (return period): %s sites completed in %s",
        handler.resultCount(), handler.elapsedTime()));

    exec.shutdown();
    return handler.outputDir();
  }

  /* Hazard curves are already in log-x space. */
  private static final Interpolator IML_INTERPOLATER = Interpolator.builder()
      .logy()
      .decreasingY()
      .build();

  /** Compute the return period intercepts from a hazard result. */
  public static Map<Imt, Double> imlsForReturnPeriod(
      Hazard hazard,
      double returnPeriod) {

    double rate = 1.0 / returnPeriod;
    Map<Imt, Double> imls = new EnumMap<>(Imt.class);
    for (Entry<Imt, XySequence> entry : hazard.curves().entrySet()) {
      double iml = IML_INTERPOLATER.findX(entry.getValue(), rate);
      imls.put(entry.getKey(), Math.exp(iml));
    }
    return imls;
  }

  /*
   * Compute hazard curves using the supplied model, config, and sites. Method
   * returns the path to the directory where results were written.
   */
  private static Path calcIml(
      HazardModel model,
      CalcConfig config,
      List<Site> sites,
      List<Map<Imt, Double>> imls,
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

    log.info(PROGRAM + " (IML): calculating ...");
    Path outDir = createOutputDir(config.output.directory);
    Path disaggDir = outDir.resolve("disagg");
    Files.createDirectory(disaggDir);

    Stopwatch stopwatch = Stopwatch.createStarted();
    int logInterval = sites.size() < 100 ? 1 : sites.size() < 1000 ? 10 : 100;

    for (int i = 0; i < sites.size(); i++) {

      Site site = sites.get(i);
      Map<Imt, Double> siteImls = imls.get(i);

      Hazard hazard = HazardCalcs.hazard(model, config, site, exec);
      Disaggregation disagg = Disaggregation.atImls(hazard, siteImls, exec);

      Response response = new Response.Builder()
          .config(config)
          .site(site)
          .imls(siteImls)
          .disagg(disagg)
          .build();

      String filename = disaggFilename(site);
      Path resultPath = disaggDir.resolve(filename);
      Writer writer = Files.newBufferedWriter(resultPath);
      GSON.toJson(response, writer);
      writer.close();

      if (i % logInterval == 0) {
        log.info(String.format(
            "     %s of %s sites completed in %s",
            i + 1, sites.size(), stopwatch));
      }
    }

    log.info(String.format(
        PROGRAM + " (IML): %s sites completed in %s",
        sites.size(), stopwatch));

    exec.shutdown();
    return outDir;
  }

  private static final class Response {

    final Response.Metadata metadata;
    final Object data;

    Response(Response.Metadata metadata, Object data) {
      this.metadata = metadata;
      this.data = data;
    }

    static final class Metadata {

      final String name;
      final double longitude;
      final double latitude;
      final double vs30;
      final Double returnPeriod;
      final Map<String, Double> imls;

      Metadata(Site site, Double returnPeriod, Map<Imt, Double> imls) {
        this.name = site.name();
        this.longitude = site.location().longitude;
        this.latitude = site.location().latitude;
        this.vs30 = site.vs30();
        this.returnPeriod = returnPeriod;
        this.imls = imls.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().name(),
                Entry::getValue,
                (x, y) -> y,
                () -> new LinkedHashMap<String, Double>()));
      }
    }

    static final class Builder {

      Site site;
      Disaggregation disagg;
      Double returnPeriod; // optional
      Map<Imt, Double> imls;
      CalcConfig config;

      Builder imls(Map<Imt, Double> imls) {
        this.imls = imls;
        return this;
      }

      Builder returnPeriod(double returnPeriod) {
        this.returnPeriod = returnPeriod;
        return this;
      }

      Builder site(Site site) {
        this.site = site;
        return this;
      }

      Builder disagg(Disaggregation disagg) {
        this.disagg = disagg;
        return this;
      }

      Builder config(CalcConfig config) {
        this.config = config;
        return this;
      }

      Response build() {

        // default toJson(imt, false, false, false)
        List<ImtDisagg> disaggs = imls.keySet().stream()
            .map(imt -> new ImtDisagg(imt, disagg.toJson(
                imt,
                config.output.dataTypes.contains(GMM),
                config.output.dataTypes.contains(SOURCE),
                config.output.dataTypes.contains(DISAGG_DATA))))
            .collect(toList());

        return new Response(
            new Response.Metadata(site, returnPeriod, imls),
            disaggs);
      }
    }
  }

  // this could be consolidated with DisaggService
  private static final class ImtDisagg {
    final String imt;
    final Object data;

    ImtDisagg(Imt imt, Object data) {
      this.imt = imt.name();
      this.data = data;
    }
  }

  // duplicate of that in HazardExport
  private static Path createOutputDir(Path dir) throws IOException {
    int i = 1;
    Path incrementedDir = dir;
    while (Files.exists(incrementedDir)) {
      incrementedDir = incrementedDir.resolveSibling(dir.getFileName() + "-" + i);
      i++;
    }
    Files.createDirectories(incrementedDir);
    return incrementedDir;
  }

  private static String disaggFilename(Site site) {
    return site.name().equals(Site.NO_NAME)
        ? String.format(
            "%.2f,%.2f.json",
            site.location().longitude,
            site.location().latitude)
        : site.name() + ".json";
  }

  private static final String PROGRAM = DisaggCalc.class.getSimpleName();
  private static final String USAGE_COMMAND =
      "java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.DisaggCalc model sites [config]";
  private static final String USAGE_URL1 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/docs";
  private static final String USAGE_URL2 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples";

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
      .append("  'config' (optional) supplies a calculation configuration")
      .append(NEWLINE)
      .append(NEWLINE)
      .append("For more information, see:").append(NEWLINE)
      .append("  ").append(USAGE_URL1).append(NEWLINE)
      .append("  ").append(USAGE_URL2).append(NEWLINE)
      .toString();

}

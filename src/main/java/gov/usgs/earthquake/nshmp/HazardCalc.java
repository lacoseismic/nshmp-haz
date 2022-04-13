package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.MoreExecutors;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.DataType;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.HazardExport;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Sites;
import gov.usgs.earthquake.nshmp.internal.Logging;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.SiteData;

/**
 * Compute probabilisitic seismic hazard from a {@link HazardModel}.
 *
 * @author U.S. Geological Survey
 */
public class HazardCalc {

  /**
   * Entry point for a probabilisitic seismic hazard curve calculation.
   *
   * <p>Computing hazard curves requires at least 2, and at most 3, arguments.
   * At a minimum, the path to a model directory and a file of site(s) at which
   * to perform calculations must be specified. Under the 2-argument scenario,
   * model initialization and calculation configuration settings are drawn from
   * the default configuration for the model. Sites may be defined in a CSV or
   * GeoJSON file.
   *
   * <p>To override any default calculation configuration settings, also supply
   * the path to a configuration file as a third argument.
   *
   * <p>Refer to the nshmp-haz <a
   * href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/blob/main/docs/README.md">
   * documentation</a> for comprehensive descriptions of source models,
   * configuration files, site files, and hazard calculations.
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
    Logger log = Logger.getLogger(HazardCalc.class.getName());
    Path tmpLog = createTempLog();
    String tmpLogName = checkNotNull(tmpLog.getFileName()).toString();

    try {
      FileHandler fh = new FileHandler(tmpLogName);
      fh.setFormatter(new Logging.ConsoleFormatter());
      log.getParent().addHandler(fh);

      log.info(PROGRAM + ": " + VERSION);
      Path modelPath = Paths.get(args[0]);
      HazardModel model = HazardModel.load(modelPath);

      /* Calculation configuration, possibly user supplied. */
      CalcConfig config = model.config();
      if (argCount == 3) {
        Path userConfigPath = Paths.get(args[2]);
        config = CalcConfig.copyOf(model.config())
            .extend(CalcConfig.from(userConfigPath))
            .build();
      }
      log.info(config.toString());
      log.info("");

      Path out = HazardExport.createDirectory(config.output.directory);
      SiteData siteData = config.hazard.useSiteData
          ? model.siteData()
          : SiteData.EMPTY;

      if (config.hazard.vs30s.isEmpty()) {

        List<Site> sites = readSites(args[1], siteData, OptionalDouble.empty(), log);
        log.info("Sites: " + Sites.toString(sites));
        calc(model, config, sites, out, log);

      } else {

        for (double vs30 : config.hazard.vs30s) {
          log.info("Vs30 batch: " + vs30);
          List<Site> sites = readSites(args[1], siteData, OptionalDouble.of(vs30), log);
          log.info("Sites: " + Sites.toString(sites));
          Path vs30dir = out.resolve("vs30-" + ((int) vs30));
          Files.createDirectory(vs30dir);
          calc(model, config, sites, vs30dir, log);
        }

      }

      if (config.output.dataTypes.contains(DataType.MAP)) {
        HazardMaps.createDataSets(out, config.output.returnPeriods, log);
      }

      log.info(PROGRAM + ": finished");

      /* Transfer log and write config, windows requires fh.close() */
      fh.close();
      Files.move(tmpLog, out.resolve(PROGRAM + ".log"));
      config.write(out);

      return Optional.empty();

    } catch (Exception e) {
      return handleError(e, log, tmpLog, args, PROGRAM, USAGE);
    }
  }

  static List<Site> readSites(
      String siteFile,
      SiteData siteData,
      OptionalDouble vs30,
      Logger log) {

    Path path = Paths.get(siteFile);
    log.info("Sites file: " + path.toAbsolutePath().normalize());
    String fname = siteFile.toLowerCase();
    checkArgument(fname.endsWith(".csv") || fname.endsWith(".geojson"),
        "Sites file [%s] must be a path to a *.csv or *.geojson file", siteFile);

    try {
      return fname.endsWith(".csv")
          ? Sites.fromCsv(path, siteData, vs30)
          : Sites.fromGeoJson(path, siteData, vs30);
    } catch (IOException ioe) {
      throw new IllegalArgumentException(
          "Error parsing sites file [%s]; see sites file documentation");
    }
  }

  /* Compute hazard curves using the supplied model, config, and sites. */
  private static void calc(
      HazardModel model,
      CalcConfig config,
      List<Site> sites,
      Path out,
      Logger log) throws IOException {

    int threadCount = config.performance.threadCount.value();
    final ExecutorService exec = initExecutor(threadCount);
    log.info("Threads: " + ((ThreadPoolExecutor) exec).getCorePoolSize());
    log.info(PROGRAM + ": calculating ...");

    boolean namedSites = sites.get(0).name() != Site.NO_NAME;
    HazardExport handler = HazardExport.create(model, config, namedSites, out);
    Stopwatch stopwatch = Stopwatch.createStarted();
    int logInterval = sites.size() < 100 ? 1 : sites.size() < 1000 ? 10 : 100;

    for (int i = 0; i < sites.size(); i++) {
      Site site = sites.get(i);
      Hazard hazard = HazardCalcs.hazard(model, config, site, exec);
      handler.write(hazard);
      int count = i + 1;
      if (count % logInterval == 0) {
        log.info(String.format(
            "     %s of %s sites completed in %s",
            count, sites.size(), stopwatch));
      }
    }
    exec.shutdown();
    log.info(String.format(
        PROGRAM + ": %s sites completed in %s",
        sites.size(), stopwatch));
  }

  private static ExecutorService initExecutor(int threadCount) {
    if (threadCount == 1) {
      return MoreExecutors.newDirectExecutorService();
    } else {
      return Executors.newFixedThreadPool(threadCount);
    }
  }

  static final String TMP_LOG = "nshmp-haz-log";

  static Path createTempLog() {
    Path logBase = Paths.get(".");
    Path logIncr = logBase.resolve(TMP_LOG);
    int i = 1;
    while (Files.exists(logIncr)) {
      logIncr = logBase.resolve(TMP_LOG + "-" + i);
      i++;
    }
    return logIncr;
  }

  static Optional<String> handleError(
      Exception e,
      Logger log,
      Path logfile,
      String[] args,
      String program,
      String usage) {

    log.severe(NEWLINE + "** Exiting **");
    try {
      // cleanup; do nothing on failure
      Files.deleteIfExists(logfile);
    } catch (IOException ioe) {}
    StringBuilder sb = new StringBuilder()
        .append(NEWLINE)
        .append(program + ": error").append(NEWLINE)
        .append(" Arguments: ").append(Arrays.toString(args)).append(NEWLINE)
        .append(NEWLINE)
        .append(Throwables.getStackTraceAsString(e))
        .append(usage);
    return Optional.of(sb.toString());
  }

  /** The Git application version. */
  public static final String VERSION = "TODO get version from resource";

  private static final String PROGRAM = HazardCalc.class.getSimpleName();
  private static final String USAGE_COMMAND =
      "java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.Hazard model sites [config]";
  private static final String USAGE_URL1 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/docs";
  private static final String USAGE_URL2 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples";

  private static final String USAGE = new StringBuilder()
      .append(NEWLINE)
      .append(PROGRAM).append(" [").append(VERSION).append("]").append(NEWLINE)
      .append(NEWLINE)
      .append("Usage:").append(NEWLINE)
      .append("  ").append(USAGE_COMMAND).append(NEWLINE)
      .append(NEWLINE)
      .append("Where:").append(NEWLINE)
      .append("  'model' is a model directory")
      .append(NEWLINE)
      .append("  'sites' is a *.csv file or *.geojson file of sites and data")
      .append(NEWLINE)
      .append("     - site class and basin terms are optional")
      .append(NEWLINE)
      .append("  'config' (optional) supplies a calculation configuration")
      .append(NEWLINE)
      .append(NEWLINE)
      .append("For more information, see:").append(NEWLINE)
      .append("  ").append(USAGE_URL1).append(NEWLINE)
      .append("  ").append(USAGE_URL2).append(NEWLINE)
      .append(NEWLINE)
      .toString();
}

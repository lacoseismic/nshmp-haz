package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

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
   * the default configuration. Sites may be defined in a CSV or GeoJSON file.
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

    try {
      FileHandler fh = new FileHandler(checkNotNull(tmpLog.getFileName()).toString());
      fh.setFormatter(new Logging.ConsoleFormatter());
      log.getParent().addHandler(fh);

      log.info(PROGRAM + ": " + VERSION);
      Path modelPath = Paths.get(args[0]);
      HazardModel model = HazardModel.load(modelPath);

      CalcConfig config = model.config();
      if (argCount == 3) {
        Path userConfigPath = Paths.get(args[2]);
        config = CalcConfig.copyOf(model.config())
            .extend(CalcConfig.from(userConfigPath))
            .build();
      }
      log.info(config.toString());

      log.info("");
      Sites sites = readSites(args[1], config, model.siteData(), log);
      log.info("Sites: " + sites);

      Path out = calc(model, config, sites, log);

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

  static Sites readSites(
      String arg,
      CalcConfig defaults,
      SiteData siteData,
      Logger log) {

    Path path = Paths.get(arg);
    log.info("Sites file: " + path.toAbsolutePath().normalize());
    String fname = arg.toLowerCase();
    checkArgument(fname.endsWith(".csv") || fname.endsWith(".geojson"),
        "Sites file [%s] must be a path to a *.csv or *.geojson file", arg);

    try {
      return fname.endsWith(".csv")
          ? Sites.fromCsv(path, defaults, siteData)
          : Sites.fromJson(path, defaults, siteData);
    } catch (IOException ioe) {
      throw new IllegalArgumentException(
          "Error parsing sites file [%s]; see sites file documentation");
    }
  }

  /*
   * Compute hazard curves using the supplied model, config, and sites. Method
   * returns the path to the directory where results were written.
   */
  private static Path calc(
      HazardModel model,
      CalcConfig config,
      Sites sites,
      Logger log) throws IOException, InterruptedException, ExecutionException {

    int threadCount = config.performance.threadCount.value();
    final ExecutorService exec = initExecutor(threadCount);
    log.info("Threads: " + ((ThreadPoolExecutor) exec).getCorePoolSize());
    log.info(PROGRAM + ": calculating ...");

    HazardExport handler = HazardExport.create(model, config, sites, log);
    CalcTask.Builder calcTask = new CalcTask.Builder(model, config, exec);
    WriteTask.Builder writeTask = new WriteTask.Builder(handler);

    Future<Path> out = null;
    for (Site site : sites) {
      Hazard hazard = calcTask.withSite(site).call();
      out = exec.submit(writeTask.withResult(hazard));
    }
    /* Block shutdown until last task is returned. */
    Path outputDir = out.get();

    handler.expire();
    exec.shutdown();
    log.info(String.format(
        PROGRAM + ": %s sites completed in %s",
        handler.resultCount(), handler.elapsedTime()));

    return outputDir;
  }

  private static ExecutorService initExecutor(int threadCount) {
    if (threadCount == 1) {
      return MoreExecutors.newDirectExecutorService();
    } else {
      return Executors.newFixedThreadPool(threadCount);
    }
  }

  private static final class CalcTask implements
      Callable<Hazard> {

    final HazardModel model;
    final CalcConfig config;
    final Site site;
    final Executor exec;

    CalcTask(
        HazardModel model,
        CalcConfig config,
        Site site,
        Executor exec) {

      this.model = model;
      this.config = config;
      this.site = site;
      this.exec = exec;
    }

    @Override
    public Hazard call() {
      return HazardCalcs.hazard(model, config, site, exec);
    }

    static class Builder {

      final HazardModel model;
      final CalcConfig config;
      final Executor exec;

      Builder(HazardModel model, CalcConfig config, Executor exec) {
        this.model = model;
        this.config = config;
        this.exec = exec;
      }

      /* Builds and returns the task. */
      CalcTask withSite(Site site) {
        return new CalcTask(model, config, site, exec);
      }
    }
  }

  private static final class WriteTask implements Callable<Path> {

    final HazardExport handler;
    final Hazard hazard;

    WriteTask(
        HazardExport handler,
        Hazard hazard) {
      this.handler = handler;
      this.hazard = hazard;
    }

    @Override
    public Path call() throws IOException {
      handler.write(hazard);
      return handler.outputDir();
    }

    static class Builder {

      final HazardExport handler;

      Builder(HazardExport handler) {
        this.handler = handler;
      }

      /* Builds and returns the task. */
      WriteTask withResult(Hazard hazard) {
        return new WriteTask(handler, hazard);
      }
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

  /**
   * The Git application version. This version string applies to all other
   * nshnmp-haz applications.
   */
  public static final String VERSION = version();

  private static final String PROGRAM = HazardCalc.class.getSimpleName();
  private static final String USAGE_COMMAND =
      "java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.Hazard model sites [config]";
  private static final String USAGE_URL1 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/docs";
  private static final String USAGE_URL2 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples";
  private static final String SITE_STRING = "name,lon,lat[,vs30,vsInf[,z1p0,z2p5]]";

  @Deprecated
  private static String version() {
    String version = "unknown";
    /* Assume we're running from a jar. */
    try {
      InputStream is = HazardCalc.class.getResourceAsStream("/app.properties");
      Properties props = new Properties();
      props.load(is);
      is.close();
      version = props.getProperty("app.version");
    } catch (Exception e1) {
      /* Otherwise check for a repository. */
      Path gitDir = Paths.get(".git");
      if (Files.exists(gitDir)) {
        try {
          Process pr = Runtime.getRuntime().exec("git describe --tags");
          BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
          version = br.readLine();
          br.close();
          /* Detached from repository. */
        } catch (Exception e2) {}
      }
    }
    return version;
  }

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

package gov.usgs.earthquake.nshmp;

import static gov.usgs.earthquake.nshmp.Text.NEWLINE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Disaggregation;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.HazardExport;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Sites;
import gov.usgs.earthquake.nshmp.calc.ThreadCount;
import gov.usgs.earthquake.nshmp.internal.Logging;
import gov.usgs.earthquake.nshmp.model.HazardModel;

/**
 * Deaggregate probabilisitic seismic hazard at a return period of interest.
 *
 * @author U.S. Geological Survey
 */
public class DeaggCalc {

  /**
   * Entry point for the deaggregation of probabilisitic seismic hazard.
   *
   * <p>Deaggregating siesmic hazard is largeley identical to a hazard
   * calculation except that a return period (in years) must be supplied as an
   * additional argument after the 'site(s)' argument. See the
   * {@link HazardCalc#main(String[]) HazardCalc program} for more information
   * on required parameters.
   *
   * <p>Please refer to the nshmp-haz <a
   * href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/docs"
   * target="_top">docs</a> for comprehensive descriptions of source models,
   * configuration files, site files, and hazard calculations.
   *
   * @see <a
   *      href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/blob/main/docs/pages/Building-&-Running.md"
   *      target="_top"> nshmp-haz Building & Running</a>
   * @see <a
   *      href="https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples"
   *      target="_top"> example calculations</a>
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

    if (argCount < 3 || argCount > 4) {
      return Optional.of(USAGE);
    }

    Logging.init();
    Logger log = Logger.getLogger(DeaggCalc.class.getName());
    Path tmpLog = HazardCalc.createTempLog();

    try {
      FileHandler fh = new FileHandler(Preconditions.checkNotNull(tmpLog.getFileName()).toString());
      fh.setFormatter(new Logging.ConsoleFormatter());
      log.getParent().addHandler(fh);

      log.info(PROGRAM + ": " + HazardCalc.VERSION);
      Path modelPath = Paths.get(args[0]);
      HazardModel model = HazardModel.load(modelPath);

      CalcConfig config = model.config();
      if (argCount == 4) {
        Path userConfigPath = Paths.get(args[3]);
        config = CalcConfig.copyOf(model.config())
            .extend(CalcConfig.from(userConfigPath))
            .build();
      }
      log.info(config.toString());

      log.info("");
      Sites sites = HazardCalc.readSites(args[1], config, model.siteData(), log);
      log.info("Sites: " + sites);

      double returnPeriod = Double.valueOf(args[2]);

      Path out = calc(model, config, sites, returnPeriod, log);
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

  /*
   * Compute hazard curves using the supplied model, config, and sites. Method
   * returns the path to the directory where results were written.
   *
   * TODO consider refactoring to supply an Optional<Double> return period to
   * HazardCalc.calc() that will trigger deaggregations if the value is present.
   */
  private static Path calc(
      HazardModel model,
      CalcConfig config,
      Sites sites,
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

    log.info(PROGRAM + ": calculating ...");

    HazardExport handler = HazardExport.create(model, config, sites, log);

    for (Site site : sites) {
      Hazard hazard = HazardCalcs.hazard(model, config, site, exec);
      Disaggregation disagg = HazardCalcs.disaggReturnPeriod(hazard, returnPeriod, exec);
      handler.write(hazard, Optional.of(disagg));
      log.fine(hazard.toString());
    }
    handler.expire();

    log.info(String.format(
        PROGRAM + ": %s sites completed in %s",
        handler.resultCount(), handler.elapsedTime()));

    exec.shutdown();
    return handler.outputDir();
  }

  private static final String PROGRAM = DeaggCalc.class.getSimpleName();
  private static final String USAGE_COMMAND =
      "java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.DeaggCalc model sites returnPeriod [config]";
  private static final String USAGE_URL1 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/docs";
  private static final String USAGE_URL2 =
      "https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples";
  private static final String SITE_STRING = "name,lon,lat[,vs30,vsInf[,z1p0,z2p5]]";

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
      .append("  'sites' is either:")
      .append(NEWLINE)
      .append("     - a string, e.g. ").append(SITE_STRING)
      .append(NEWLINE)
      .append("       (site class and basin terms are optional)")
      .append(NEWLINE)
      .append("       (escape any spaces or enclose string in double-quotes)")
      .append(NEWLINE)
      .append("     - or a *.csv file or *.geojson file of site data")
      .append(NEWLINE)
      .append("  'returnPeriod', in years, is a time horizon of interest")
      .append(NEWLINE)
      .append("     - e.g. one might enter 2475 to represent a 2% in 50 year probability")
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

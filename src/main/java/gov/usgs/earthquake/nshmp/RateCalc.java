package gov.usgs.earthquake.nshmp;

import static gov.usgs.earthquake.nshmp.Text.NEWLINE;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.EqRate;
import gov.usgs.earthquake.nshmp.calc.EqRateExport;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Sites;
import gov.usgs.earthquake.nshmp.calc.ThreadCount;
import gov.usgs.earthquake.nshmp.internal.Logging;
import gov.usgs.earthquake.nshmp.model.HazardModel;

/**
 * Compute earthquake rates or Poisson probabilities from a {@link HazardModel}.
 *
 * @author U.S. Geological Survey
 */
public class RateCalc {

  /**
   * Entry point for the calculation of earthquake rates and probabilities.
   *
   * <p>Computing earthquake rates requires at least 2, and at most 3,
   * arguments. At a minimum, the path to a model directory and the site(s) at
   * which to perform calculations must be specified. Under the 2-argument
   * scenario, model initialization and calculation configuration settings are
   * drawn from the config file that <i>must</i> reside at the root of the model
   * directory. Sites may be defined as a string, a CSV file, or a GeoJSON file.
   *
   * <p>To override any default or calculation configuration settings included
   * with the model, supply the path to another configuration file as a third
   * argument.
   *
   * <p>Please refer to the nshmp-haz <a
   * href="https://github.com/usgs/nshmp-haz/wiki">wiki</a> for comprehensive
   * descriptions of source models, configuration files, site files, and
   * earthquake rate calculations.
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
    Path tmpLog = HazardCalc.createTempLog();

    try {
      FileHandler fh = new FileHandler(Preconditions.checkNotNull(tmpLog.getFileName()).toString());
      fh.setFormatter(new Logging.ConsoleFormatter());
      log.getParent().addHandler(fh);

      log.info(PROGRAM + ": " + HazardCalc.VERSION);
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
      List<Site> sites = HazardCalc.readSites(args[1], config, model.siteData(), log);
      log.info("Sites: " + Sites.toString(sites));

      Path out = calc(model, config, sites, log);
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
   * Compute earthquake rates or probabilities using the supplied model, config,
   * and sites. Method returns the path to the directory where results were
   * written.
   *
   * Unlike hazard calculations, which spread work out over multiple threads for
   * a single calculation, rate calculations are single threaded. Concurrent
   * calculations for multiple sites are handled below.
   */
  private static Path calc(
      HazardModel model,
      CalcConfig config,
      List<Site> sites,
      Logger log) throws IOException, ExecutionException, InterruptedException {

    ThreadCount threadCount = config.performance.threadCount;
    EqRateExport export = null;
    if (threadCount != ThreadCount.ONE) {
      ExecutorService poolExecutor = newFixedThreadPool(threadCount.value());
      ListeningExecutorService executor = MoreExecutors.listeningDecorator(poolExecutor);
      log.info("Threads: " + ((ThreadPoolExecutor) poolExecutor).getCorePoolSize());
      log.info(PROGRAM + ": calculating ...");
      export = concurrentCalc(model, config, sites, log, executor);
      executor.shutdown();
    } else {
      log.info("Threads: Running on calling thread");
      log.info(PROGRAM + ": calculating ...");
      export = EqRateExport.create(model, config, sites, log);
      for (Site site : sites) {
        EqRate rate = EqRate.create(model, config, site);
        export.write(rate);
      }
    }
    export.expire();

    log.info(String.format(
        PROGRAM + ": %s sites completed in %s",
        export.resultCount(), export.elapsedTime()));

    return export.outputDir();
  }

  private static EqRateExport concurrentCalc(
      HazardModel model,
      CalcConfig config,
      List<Site> sites,
      Logger log,
      ListeningExecutorService executor)
      throws InterruptedException, ExecutionException, IOException {

    EqRateExport export = EqRateExport.create(model, config, sites, log);

    int submitted = 0;
    int batchSize = 10;
    List<ListenableFuture<EqRate>> rateFutures = new ArrayList<>(batchSize);

    /*
     * Although the approach below may not fully leverage all processors if
     * there are one or more longer-running calcs in the batch, processing
     * batches of locations to a List preserves submission order; as opposed to
     * using FutureCallbacks, which will reorder sites on export.
     *
     * TODO this is a terrible implementation with batch size 10. resulted from
     * refactor to exports not queueing results
     */
    for (Site site : sites) {
      Callable<EqRate> task = EqRate.callable(model, config, site);
      rateFutures.add(executor.submit(task));
      submitted++;

      if (submitted == batchSize) {
        List<EqRate> rateList = Futures.allAsList(rateFutures).get();
        for (EqRate rate : rateList) {
          export.write(rate);
        }
        submitted = 0;
        rateFutures.clear();
      }
    }
    List<EqRate> lastBatch = Futures.allAsList(rateFutures).get();
    for (EqRate rate : lastBatch) {
      export.write(rate);
    }
    return export;
  }

  private static final String PROGRAM = RateCalc.class.getSimpleName();
  private static final String USAGE_COMMAND =
      "java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.RateCalc model sites [config]";
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
      .append("  'sites' is a *.csv file or *.geojson file of sites and data")
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

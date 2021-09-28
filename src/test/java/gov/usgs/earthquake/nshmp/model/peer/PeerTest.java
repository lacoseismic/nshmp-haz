package gov.usgs.earthquake.nshmp.model.peer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.internal.Parsing.Delimiter.COMMA;
import static java.lang.Math.abs;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.Maths;
import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Sites;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.Parsing;
import gov.usgs.earthquake.nshmp.model.HazardModel;

class PeerTest {

  static final String S1_C1 = "Set1-Case1";
  static final String S1_C2 = "Set1-Case2";
  static final String S1_C2_F = "Set1-Case2-fast";
  static final String S1_C3 = "Set1-Case3";
  static final String S1_C3_F = "Set1-Case3-fast";
  static final String S1_C4 = "Set1-Case4";
  static final String S1_C4_F = "Set1-Case4-fast";
  static final String S1_C5 = "Set1-Case5";
  static final String S1_C5_F = "Set1-Case5-fast";
  static final String S1_C6 = "Set1-Case6";
  static final String S1_C6_F = "Set1-Case6-fast";
  static final String S1_C7 = "Set1-Case7";
  static final String S1_C7_F = "Set1-Case7-fast";
  static final String S1_C8A = "Set1-Case8a";
  static final String S1_C8B = "Set1-Case8b";
  static final String S1_C8B_F = "Set1-Case8b-fast";
  static final String S1_C8C = "Set1-Case8c";
  static final String S1_C10 = "Set1-Case10";
  static final String S1_C10_F = "Set1-Case10-fast";
  static final String S1_C11 = "Set1-Case11";
  static final String S1_C11_F = "Set1-Case11-fast";

  static final String S2_C1 = "Set2-Case1";
  static final String S2_C2A = "Set2-Case2a";
  static final String S2_C2A_F = "Set2-Case2a-fast";
  static final String S2_C2B = "Set2-Case2b";
  static final String S2_C2B_F = "Set2-Case2b-fast";
  static final String S2_C2C = "Set2-Case2c";
  static final String S2_C2C_F = "Set2-Case2c-fast";
  static final String S2_C2D = "Set2-Case2d";
  static final String S2_C2D_F = "Set2-Case2d-fast";
  static final String S2_C3A = "Set2-Case3a";
  static final String S2_C3A_F = "Set2-Case3a-fast";
  static final String S2_C3B = "Set2-Case3b";
  static final String S2_C3B_F = "Set2-Case3b-fast";
  static final String S2_C3C = "Set2-Case3c";
  static final String S2_C3C_F = "Set2-Case3c-fast";
  static final String S2_C3D = "Set2-Case3d";
  static final String S2_C3D_F = "Set2-Case3d-fast";
  static final String S2_C4A = "Set2-Case4a";
  static final String S2_C4A_F = "Set2-Case4a-fast";
  static final String S2_C4B = "Set2-Case4b";
  static final String S2_C4B_F = "Set2-Case4b-fast";
  static final String S2_C5A = "Set2-Case5a";
  static final String S2_C5B = "Set2-Case5b";

  static final String S3_C1A = "Set3-Case1a";
  static final String S3_C1B = "Set3-Case1a";
  static final String S3_C2 = "Set3-Case2";
  static final String S3_C3 = "Set3-Case3";
  static final String S3_C4 = "Set3-Case4";

  private static final Path PEER_DIR = Paths.get("etc", "peer");
  // private static final Path PEER_DIR = Paths.get("..", "nshmp-model-dev",
  // "models", "PEER");
  private static final Path MODEL_DIR = PEER_DIR.resolve("models");
  private static final Path RESULT_DIR = PEER_DIR.resolve("results");

  static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  static void test(
      String modelName,
      HazardModel model,
      Site site,
      double[] expected,
      double tolerance,
      ExecutorService exec) {
    Hazard result = HazardCalcs.hazard(model, model.config(), site, exec);
    // compute y-values converting to Poiss prob
    double[] actual = Doubles.toArray(
        FluentIterable
            .from(result.curves().get(Imt.PGA).yValues().boxed().collect(Collectors.toList()))
            .transform(Maths.annualRateToProbabilityConverter())
            .toList());
    checkArgument(actual.length == expected.length);

    // tests difference relative to tolerance
    // TODO maybe just decrease this tolerance and
    // do away with ratio test below
    assertArrayEquals(expected, actual, tolerance);
    // tests ratio relative to tolerance
    for (int i = 0; i < expected.length; i++) {
      String message = String.format("arrays differ at [%s] expected:<[%s]> but was:<[%s]>",
          i, expected[i], actual[i]);
      assertTrue(compare(expected[i], actual[i], tolerance), message);
    }
  }

  private static boolean compare(double expected, double actual, double tolerance) {
    return abs(actual - expected) / expected < tolerance ||
        Double.valueOf(expected).equals(Double.valueOf(actual));
  }

  static Stream<? extends Arguments> load(String modelId) throws IOException {

    /* NOTE this is disabling all logging in Loader and HazardCalc */
    LogManager.getLogManager().reset();

    Map<String, double[]> expectedsMap = loadExpecteds(modelId);
    HazardModel model = HazardModel.load(MODEL_DIR.resolve(modelId));
    CalcConfig config = model.config();
    Iterable<Site> sites = Sites.fromCsv(MODEL_DIR.resolve(
        modelId).resolve("sites.csv"),
        config,
        model.siteData());

    // ensure that only PGA is being used
    checkState(config.hazard.imts.size() == 1);
    checkState(config.hazard.imts.iterator().next() == Imt.PGA);

    List<Object[]> argsList = new ArrayList<>();
    for (Site site : sites) {
      checkState(expectedsMap.containsKey(site.name()));
      Object[] args = new Object[] { model.name(), model, site, expectedsMap.get(site.name()) };
      argsList.add(args);
    }
    return argsList.stream().map(Arguments::of);
  }

  private static Map<String, double[]> loadExpecteds(String modelId) throws IOException {
    Path results = RESULT_DIR.resolve(modelId + ".csv");
    List<String> lines = Files.readAllLines(results, UTF_8);
    Map<String, double[]> siteValuesMap = new HashMap<>();
    for (String line : Iterables.skip(lines, 1)) {
      String[] splitLine = line.split(",", 4);
      String siteName = splitLine[0];
      List<Double> values = Parsing.splitToDoubleList(splitLine[3], COMMA);
      siteValuesMap.put(siteName, Doubles.toArray(values));
    }
    return siteValuesMap;
  }

}

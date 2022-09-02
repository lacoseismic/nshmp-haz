package gov.usgs.earthquake.nshmp.model;

import static gov.usgs.earthquake.nshmp.gmm.Imt.PGA;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P2;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA1P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA5P0;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.BOSTON_MA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.CHICAGO_IL;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.LOS_ANGELES_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.NEW_MADRID_MO;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.NEW_YORK_NY;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.RENO_NV;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SALT_LAKE_CITY_UT;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_FRANCISCO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SEATTLE_WA;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.usgs.earthquake.nshmp.NamedLocation;
import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.data.XySequence;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;

/**
 * Class for end-to-end tests of hazard calculations. These tests require
 * significant system resources to load source models, and source models are
 * required to be in adjacent repositories. These tests should be run
 * frequently, but not as part of continuous integration. Consider nightlies.
 * Needs parameterization and additional regions.
 *
 * @author U.S. Geological Survey
 */
class NshmTestsConus {

  private static final double TOLERANCE = 1e-12;

  private static final List<NamedLocation> SITES = List.of(
      LOS_ANGELES_CA,
      SAN_FRANCISCO_CA,
      SEATTLE_WA,
      SALT_LAKE_CITY_UT,
      RENO_NV,
      NEW_MADRID_MO,
      BOSTON_MA,
      NEW_YORK_NY,
      CHICAGO_IL);

  private static final Set<Imt> IMTS = EnumSet.of(PGA, SA0P2, SA1P0, SA5P0);

  private static final String MODEL_NAME = "nshm-conus-2018-5.1-maint";
  private static final int MODEL_YEAR = 2018;
  private static final Path MODEL_PATH = Paths.get("../" + MODEL_NAME);
  private static final Path DATA_PATH = Paths.get("src/test/resources/e2e");

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .create();

  private static ExecutorService exec;
  private static HazardModel model;
  static Map<Location, Map<String, XySequence>> expecteds;

  @BeforeAll
  static void setUpBeforeClass() {
    model = ModelLoader.load(MODEL_PATH);
    int cores = Runtime.getRuntime().availableProcessors();
    exec = Executors.newFixedThreadPool(cores);
  }

  @AfterAll
  static void tearDownAfterClass() {
    exec.shutdown();
  }

  @ParameterizedTest
  @MethodSource("siteStream")
  final void testLocation(NamedLocation site) {
    compareCurves(site);
  }

  private static Stream<NamedLocation> siteStream() {
    return SITES.stream();
  }

  private static void compareCurves(NamedLocation location) {

    // String actual = generateActual(model, location);
    Map<String, XySequence> actual = generateActual(location);
    // String expected = readExpected(modelName, year, location);
    Map<String, XySequence> expected = readExpected(location);
    // assertEquals(expected, actual);

    // assertEquals(expected.keySet(), actual.keySet());
    for (String key : actual.keySet()) {
      assertCurveEquals(expected.get(key), actual.get(key), TOLERANCE);
    }
  }

  private static void assertCurveEquals(XySequence expected, XySequence actual, double tol) {

    // IMLs close but not exact due to exp() transform
    assertArrayEquals(
        expected.xValues().toArray(),
        actual.xValues().toArray());

    double[] expectedYs = expected.yValues().toArray();
    double[] actualYs = actual.yValues().toArray();

    // absolute y-value difference relative to tolerance
    assertArrayEquals(expectedYs, actualYs, tol);

    // relative y-value difference relative to tolerance
    for (int i = 0; i < expectedYs.length; i++) {
      String message = String.format(
          "arrays differ at [%s] expected:<[%s]> but was:<[%s]>",
          i, expectedYs[i], actualYs[i]);
      assertTrue(compare(expectedYs[i], actualYs[i], tol), message);
    }
  }

  private static boolean compare(double expected, double actual, double tolerance) {
    return abs(actual - expected) / expected < tolerance ||
        Double.valueOf(expected).equals(Double.valueOf(actual));
  }

  private static Map<String, XySequence> generateActual(NamedLocation location) {

    Site site = Site.builder().location(location.location()).build();

    CalcConfig config = CalcConfig.copyOf(model.config())
        .imts(IMTS)
        .build();

    Hazard hazard = HazardCalcs.hazard(
        model,
        config,
        site,
        exec);

    Map<String, XySequence> xyMap = hazard.curves().entrySet().stream()
        .collect(Collectors.toMap(
            e -> e.getKey().toString(),
            Entry::getValue));

    return xyMap;
  }

  private static String resultFilename(
      String modelName,
      int year,
      NamedLocation loc) {

    return modelName + "-" + year + "-" + loc.name() + ".json";
  }

  private static Map<String, XySequence> readExpected(NamedLocation loc) {

    String filename = resultFilename(MODEL_NAME, MODEL_YEAR, loc);
    Path resultPath = DATA_PATH.resolve(filename);

    JsonObject obj = null;
    try (BufferedReader br = Files.newBufferedReader(resultPath)) {
      obj = JsonParser.parseReader(br).getAsJsonObject();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    Type curveDataType = new TypeToken<Map<String, Curve>>() {}.getType();
    Map<String, Curve> curveMap = GSON.fromJson(obj, curveDataType);
    Map<String, XySequence> xyMap = curveMap.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> XySequence.create(e.getValue().xs, e.getValue().ys)));
    return xyMap;
  }

  private static class Curve {
    double[] xs;
    double[] ys;

    @SuppressWarnings("unused")
    Curve(double[] xs, double[] ys) {
      this.xs = xs;
      this.ys = ys;
    }
  }

  private static void writeExpecteds(
      String modelName,
      int year,
      List<NamedLocation> locations) throws IOException {

    for (NamedLocation location : locations) {
      // String json = generateActual(model, location);
      Map<String, XySequence> xyMap = generateActual(location);
      String json = GSON.toJson(xyMap);
      writeExpected(modelName, year, location, json);
    }
  }

  private static void writeExpected(
      String modelName,
      int year,
      NamedLocation loc,
      String json) throws IOException {

    String filename = resultFilename(modelName, year, loc);
    Path resultPath = DATA_PATH.resolve(filename);
    Files.write(resultPath, json.getBytes());
  }

  public static void main(String[] args) throws IOException {

    /* Initialize and shut down executor to generate results. */
    setUpBeforeClass();

    writeExpecteds(MODEL_NAME, MODEL_YEAR, SITES);

    tearDownAfterClass();
  }

}

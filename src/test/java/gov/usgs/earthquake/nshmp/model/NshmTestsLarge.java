package gov.usgs.earthquake.nshmp.model;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.usgs.earthquake.nshmp.NamedLocation;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.data.XySequence;

/**
 * Class for end-to-end tests of hazard calculations. These tests require
 * significant system resources to load source models, and source models are
 * required to be in adjacent repositories. These tests should be run
 * frequently, but not as part of continuous integration. Consider nightlies.
 * Needs parameterization and additional regions.
 *
 * @author U.S. Geological Survey
 */
class NshmTestsLarge {

  private static final double TOLERANCE = 1e-12;

  private static final List<NamedLocation> CONUS_SITES = List.of(
      LOS_ANGELES_CA,
      SAN_FRANCISCO_CA,
      SEATTLE_WA,
      SALT_LAKE_CITY_UT,
      RENO_NV,
      NEW_MADRID_MO,
      BOSTON_MA,
      NEW_YORK_NY,
      CHICAGO_IL);

  /*
   * These tests use project relative file paths to read/write directly to/from
   * the source tree.
   */
  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .create();

  private static ExecutorService EXEC;

  @BeforeAll
  static void setUpBeforeClass() {
    int cores = Runtime.getRuntime().availableProcessors();
    EXEC = Executors.newFixedThreadPool(cores);
  }

  @AfterAll
  static void tearDownAfterClass() {
    EXEC.shutdown();
  }

  private static final Path MODEL_PATH = Paths.get("../");
  private static final Path DATA_PATH = Paths.get("src/test/resources/e2e");

  @Test
  public void testConus2018() {
    testModel("nshm-conus", 2018, CONUS_SITES);
  }

  private static void testModel(
      String modelName,
      int year,
      List<NamedLocation> locations) {

    Path modelPath = MODEL_PATH.resolve(modelName);
    HazardModel model = ModelLoader.load(modelPath);
    for (NamedLocation location : locations) {
      compareCurves(modelName, year, model, location);
    }
  }

  private static void compareCurves(
      String modelName,
      int year,
      HazardModel model,
      NamedLocation location) {

    // String actual = generateActual(model, location);
    Map<String, XySequence> actual = generateActual(model, location);
    // String expected = readExpected(modelName, year, location);
    Map<String, XySequence> expected = readExpected(modelName, year, location);
    // assertEquals(expected, actual);

    assertEquals(expected.keySet(), actual.keySet());
    for (String key : expected.keySet()) {
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

  private static Map<String, XySequence> generateActual(
      HazardModel model,
      NamedLocation location) {

    Site site = Site.builder().location(location.location()).build();

    Hazard hazard = HazardCalcs.hazard(
        model,
        model.config(),
        site,
        EXEC);

    Map<String, XySequence> xyMap = hazard.curves().entrySet().stream()
        .collect(Collectors.toMap(
            e -> e.getKey().toString(),
            Entry::getValue));

    return xyMap;
    // return GSON.toJson(hazard.curves());
  }

  private static String resultFilename(
      String modelName,
      int year,
      NamedLocation loc) {

    return modelName + "-" + year + "-" + loc.name() + ".json";
  }

  private static Map<String, XySequence> readExpected(
      String modelName,
      int year,
      NamedLocation loc) {

    String filename = resultFilename(modelName, year, loc);
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

    Curve(double[] xs, double[] ys) {
      this.xs = xs;
      this.ys = ys;
    }
  }

  private static void writeExpecteds(
      String modelName,
      int year,
      List<NamedLocation> locations) throws IOException {

    Path modelPath = MODEL_PATH.resolve(modelName);
    HazardModel model = ModelLoader.load(modelPath);
    for (NamedLocation location : locations) {
      // String json = generateActual(model, location);
      Map<String, XySequence> xyMap = generateActual(model, location);
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

    writeExpecteds("nshm-conus", 2018, CONUS_SITES);

    tearDownAfterClass();
  }

}

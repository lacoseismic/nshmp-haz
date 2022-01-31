package gov.usgs.earthquake.nshmp.www.hazard;

import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.calc.HazardExport.curvesBySource;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkInRange;
import static gov.usgs.earthquake.nshmp.geo.Coordinates.checkLatitude;
import static gov.usgs.earthquake.nshmp.geo.Coordinates.checkLongitude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Stopwatch;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.data.MutableXySequence;
import gov.usgs.earthquake.nshmp.data.XySequence;
import gov.usgs.earthquake.nshmp.geo.Coordinates;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.SourceType;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServicesUtil;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;
import gov.usgs.earthquake.nshmp.www.services.SourceServices.SourceModel;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Probabilistic seismic hazard calculation handler for
 * {@link HazardController}.
 *
 * @author U.S. Geological Survey
 */
@Singleton
public final class HazardService {

  static final String NAME = "Hazard Service";

  /** HazardController.doGetUsage() handler. */
  public static HttpResponse<String> handleDoGetMetadata(HttpRequest<?> request) {
    var url = request.getUri().getPath();
    try {
      var usage = new UsageMetadata(ServletUtil.model());
      var response = ResponseBody.usage()
          .name(NAME)
          .url(url)
          .request(url)
          .response(usage)
          .build();
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, url);
    }
  }

  /** HazardController.doGetHazard() handler. */
  public static HttpResponse<String> processRequest(Request request) {
    try {
      Response response = process(request);
      var body = ResponseBody.success()
          .name(NAME)
          .url(request.http.getUri().getPath())
          .request(request)
          .response(response)
          .build();
      String svcResponse = ServletUtil.GSON.toJson(body);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, request.http.getUri().getPath());
    }
  }

  /*
   * Developer notes:
   *
   * Future calculation configuration options: vertical GMs
   *
   * NSHM Hazard Tool will not pass truncation and maxdir args/flags as the apps
   * apply truncation and scaling on the client.
   */

  static Response process(Request request)
      throws InterruptedException, ExecutionException {

    var stopwatch = Stopwatch.createStarted();
    var hazard = calcHazard(request);

    return new ResultBuilder()
        .request(request)
        .hazard(hazard)
        .timer(stopwatch)
        .build();
  }

  public static Hazard calcHazard(Request request)
      throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();

    // will we be passing in options for config??
    CalcConfig config = CalcConfig.copyOf(model.config()).build();

    // TODO this needs to pick up SiteData
    Site site = Site.builder()
        .location(Location.create(request.longitude, request.latitude))
        .vs30(request.vs30)
        .build();
    CompletableFuture<Hazard> future = futureHazard(model, config, site);
    return future.get();
  }

  private static CompletableFuture<Hazard> futureHazard(
      HazardModel model,
      CalcConfig config,
      Site site) {

    return CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(model, config, site, ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);
  }

  private static class UsageMetadata {

    final SourceModel model;
    final DoubleParameter longitude;
    final DoubleParameter latitude;
    final DoubleParameter vs30;

    UsageMetadata(HazardModel model) {
      this.model = new SourceModel(model);
      // perhaps move out to shared factory with parameter instances
      //
      // TODO need min max from model
      longitude = new DoubleParameter(
          "Longitude",
          "°",
          Coordinates.LON_RANGE.lowerEndpoint(),
          Coordinates.LON_RANGE.upperEndpoint());

      latitude = new DoubleParameter(
          "Latitude",
          "°",
          Coordinates.LAT_RANGE.lowerEndpoint(),
          Coordinates.LAT_RANGE.upperEndpoint());

      vs30 = new DoubleParameter(
          "Vs30",
          "m/s",
          150,
          1500);
    }
  }

  public static final class Request {

    transient HttpRequest<?> http;
    final double longitude;
    final double latitude;
    final double vs30;
    final boolean truncate;
    final boolean maxdir;

    public Request(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        int vs30,
        boolean truncate,
        boolean maxdir) {

      this.http = http;
      this.longitude = checkLongitude(longitude);
      this.latitude = checkLatitude(latitude);
      this.vs30 = checkInRange(Site.VS30_RANGE, Site.Key.VS30, vs30);
      this.truncate = truncate;
      this.maxdir = maxdir;
    }
  }

  private static final class ResponseMetadata {
    final String xlabel = "Ground Motion (g)";
    final String ylabel = "Annual Frequency of Exceedence";
    final Object server;

    ResponseMetadata(Object server) {
      this.server = server;
    }
  }

  private static final class Response {
    final ResponseMetadata metadata;
    final List<ImtCurves> hazardCurves;

    Response(ResponseMetadata metadata, List<ImtCurves> hazardCurves) {
      this.metadata = metadata;
      this.hazardCurves = hazardCurves;
    }
  }

  private static final class ImtCurves {
    final Parameter imt;
    final List<Curve> data;

    ImtCurves(Imt imt, List<Curve> data) {
      this.imt = new Parameter(imtShortLabel(imt), imt.name());
      this.data = data;
    }
  }

  private static final class Curve {
    final String component;
    final XySequence values;

    Curve(String component, XySequence values) {
      this.component = component;
      this.values = values;
    }
  }

  private static final String TOTAL_KEY = "Total";

  private static final class ResultBuilder {

    Stopwatch timer;
    Request request;

    Map<Imt, Map<SourceType, MutableXySequence>> componentMaps;
    Map<Imt, MutableXySequence> totalMap;

    ResultBuilder hazard(Hazard hazardResult) {
      // TODO necessary??
      checkState(totalMap == null, "Hazard has already been added to this builder");

      componentMaps = new EnumMap<>(Imt.class);
      totalMap = new EnumMap<>(Imt.class);

      var typeTotalMaps = curvesBySource(hazardResult);

      for (var imt : hazardResult.curves().keySet()) {

        /* Total curve for IMT. */
        XySequence.addToMap(imt, totalMap, hazardResult.curves().get(imt));

        /* Source component curves for IMT. */
        var typeTotalMap = typeTotalMaps.get(imt);
        var componentMap = componentMaps.get(imt);

        if (componentMap == null) {
          componentMap = new EnumMap<>(SourceType.class);
          componentMaps.put(imt, componentMap);
        }

        for (var type : typeTotalMap.keySet()) {
          XySequence.addToMap(type, componentMap, typeTotalMap.get(type));
        }
      }

      return this;
    }

    ResultBuilder timer(Stopwatch timer) {
      this.timer = timer;
      return this;
    }

    ResultBuilder request(Request request) {
      this.request = request;
      return this;
    }

    Response build() {
      var hazards = new ArrayList<ImtCurves>();

      for (Imt imt : totalMap.keySet()) {
        var curves = new ArrayList<Curve>();

        // total curve
        curves.add(new Curve(
            TOTAL_KEY,
            updateCurve(request, totalMap.get(imt), imt)));

        // component curves
        var typeMap = componentMaps.get(imt);
        for (SourceType type : typeMap.keySet()) {
          curves.add(new Curve(
              type.toString(),
              updateCurve(request, typeMap.get(type), imt)));
        }

        hazards.add(new ImtCurves(imt, List.copyOf(curves)));
      }

      Object server = Metadata.serverData(ServletUtil.THREAD_COUNT, timer);
      var response = new Response(
          new ResponseMetadata(server),
          List.copyOf(hazards));

      return response;
    }
  }

  private static final double TRUNCATION_LIMIT = 1e-4;

  /* Convert to linear and possibly truncate and scale to max-direction. */
  private static XySequence updateCurve(
      Request request,
      XySequence curve,
      Imt imt) {

    /*
     * If entire curve is <1e-4, this method will return a curve consisting of
     * just the first point in the supplied curve.
     *
     * TODO We probably want to move the TRUNCATION_LIMIT out to a config.
     */

    double[] yValues = curve.yValues().toArray();
    int limit = request.truncate ? truncationLimit(yValues) : yValues.length;
    yValues = Arrays.copyOf(yValues, limit);

    double scale = request.maxdir ? MaxDirection.FACTORS.get(imt) : 1.0;
    double[] xValues = curve.xValues()
        .limit(yValues.length)
        .map(Math::exp)
        .map(x -> x * scale)
        .toArray();

    return XySequence.create(xValues, yValues);
  }

  private static int truncationLimit(double[] yValues) {
    int limit = 1;
    double y = yValues[0];
    while (y > TRUNCATION_LIMIT && limit < yValues.length) {
      y = yValues[limit++];
    }
    return limit;
  }

  private static String imtShortLabel(Imt imt) {
    if (imt.equals(Imt.PGA) || imt.equals(Imt.PGV)) {
      return imt.name();
    } else if (imt.isSA()) {
      return imt.period() + " s";
    }
    return imt.toString();
  }

}

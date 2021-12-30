package gov.usgs.earthquake.nshmp.www.services;

import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.calc.HazardExport.curvesBySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.base.Stopwatch;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.data.MutableXySequence;
import gov.usgs.earthquake.nshmp.data.XySequence;
import gov.usgs.earthquake.nshmp.geo.Coordinates;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.SourceType;
import gov.usgs.earthquake.nshmp.www.HazardController;
import gov.usgs.earthquake.nshmp.www.Response;
import gov.usgs.earthquake.nshmp.www.WsUtils;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;
import gov.usgs.earthquake.nshmp.www.meta.Status;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceQueryData;
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
public final class HazardService2 {

  private static final String NAME = "Hazard Service";

  /** HazardController.doGetUsage() handler. */
  public static HttpResponse<String> handleDoGetMetadata(HttpRequest<?> request) {
    var url = request.getUri().getPath();
    try {
      var usage = new RequestMetadata(ServletUtil.model());// SourceServices.ResponseData();
      var response = new Response(Status.USAGE, NAME, url, usage, url);
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, url);
    }
  }

  /** HazardController.doGetHazard() handler. */
  public static HttpResponse<String> handleDoGetHazard(
      HttpRequest<?> request,
      RequestData args) {

    try {
      // TODO still need to validate
      // if (query.isEmpty()) {
      // return handleDoGetUsage(urlHelper);
      // }
      // query.checkParameters();

      // var data = new RequestData(query);

      Response<RequestData, ResponseData> response = process(request, args);
      String svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);

    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, request.getUri().getPath());
    }
  }

  static Response<RequestData, ResponseData> process(
      HttpRequest<?> request,
      RequestData data) throws InterruptedException, ExecutionException {

    var configFunction = new ConfigFunction();
    var siteFunction = new SiteFunction(data);
    var stopwatch = Stopwatch.createStarted();
    var hazard = ServicesUtil.calcHazard(configFunction, siteFunction);

    return new ResultBuilder()
        .hazard(hazard)
        .requestData(data)
        .timer(stopwatch)
        .url(request)
        .build();
  }

  static class ConfigFunction implements Function<HazardModel, CalcConfig> {
    @Override
    public CalcConfig apply(HazardModel model) {
      var configBuilder = CalcConfig.copyOf(model.config());
      return configBuilder.build();
    }
  }

  static class SiteFunction implements Function<CalcConfig, Site> {
    final RequestData data;

    private SiteFunction(RequestData data) {
      this.data = data;
    }

    @Override // TODO this needs to pick up SiteData
    public Site apply(CalcConfig config) {
      return Site.builder()
          .location(Location.create(data.longitude, data.latitude))
          .vs30(data.vs30)
          .build();
    }
  }

  // public static class QueryParameters {
  //
  // final double longitude;
  // final double latitude;
  // final int vs30;
  // final boolean truncate;
  // final boolean maxdir;
  //
  // public QueryParameters(
  // double longitude,
  // double latitude,
  // int vs30,
  // boolean truncate,
  // boolean maxdir) {
  //
  // this.longitude = longitude;
  // this.latitude = latitude;
  // this.vs30 = vs30;
  // this.truncate = truncate;
  // this.maxdir = maxdir;
  // }
  //
  // // void checkParameters() {
  // // checkParameter(longitude, "longitude");
  // // checkParameter(latitude, "latitude");
  // // checkParameter(vs30, "vs30");
  // // }
  // }

  // private static void checkParameter(Object param, String id) {
  // checkNotNull(param, "Missing parameter: %s", id);
  // // TODO check range here
  // }

  /* Service request and model metadata */
  static class RequestMetadata {

    final SourceModel model;
    final DoubleParameter longitude;
    final DoubleParameter latitude;
    final DoubleParameter vs30;

    RequestMetadata(HazardModel model) {
      this.model = new SourceModel(model);
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
          "Latitude",
          "m/s",
          150,
          1500);
    }
  }

  // static class RequestData {
  //
  // final double longitude;
  // final double latitude;
  // final double vs30;
  // final boolean truncate;
  // final boolean maxdir;
  //
  // RequestData(QueryParameters query) {
  // this.longitude = query.longitude;
  // this.latitude = query.latitude;
  // this.vs30 = query.vs30;
  // this.truncate = query.truncate;
  // this.maxdir = query.maxdir;
  // }
  // }

  private static final class ResponseMetadata {
    final String xlabel = "Ground Motion (g)";
    final String ylabel = "Annual Frequency of Exceedence";
    final Object server;

    ResponseMetadata(Object server) {
      this.server = server;
    }
  }

  private static String imtShortLabel(Imt imt) {
    if (imt.equals(Imt.PGA) || imt.equals(Imt.PGV)) {
      return imt.name();
    } else if (imt.isSA()) {
      return imt.period() + " s";
    }
    return imt.toString();
  }

  // @Deprecated
  // static class RequestDataOld extends ServiceRequestData {
  // final double vs30;
  //
  // RequestDataOld(Query query, double vs30) {
  // super(query);
  // this.vs30 = vs30;
  // }
  // }

  private static final class ResponseData {
    final ResponseMetadata metadata;
    final List<HazardResponse> hazardCurves;

    ResponseData(ResponseMetadata metadata, List<HazardResponse> hazardCurves) {
      this.metadata = metadata;
      this.hazardCurves = hazardCurves;
    }
  }

  private static final class HazardResponse {
    final Parameter imt;
    final List<Curve> data;

    HazardResponse(Imt imt, List<Curve> data) {
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

    String url;
    Stopwatch timer;
    RequestData request;

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

    ResultBuilder url(HttpRequest<?> request) {
      url = request.getUri().getPath();
      return this;
    }

    ResultBuilder timer(Stopwatch timer) {
      this.timer = timer;
      return this;
    }

    ResultBuilder requestData(RequestData request) {
      this.request = request;
      return this;
    }

    Response<RequestData, ResponseData> build() {
      var hazards = new ArrayList<HazardResponse>();

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

        hazards.add(new HazardResponse(imt, List.copyOf(curves)));
      }

      Object server = Metadata.serverData(ServletUtil.THREAD_COUNT, timer);
      var response = new ResponseData(new ResponseMetadata(server), List.copyOf(hazards));

      return new Response<>(Status.SUCCESS, NAME, request, response, url);
    }
  }

  private static final double TRUNCATION_LIMIT = 1e-4;

  /* Convert to linear and possibly truncate and scale to max-direction. */
  private static XySequence updateCurve(
      RequestData request,
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

  @Deprecated
  public static class Query extends ServiceQueryData {
    Integer vs30;

    public Query(Double longitude, Double latitude, Integer vs30) {
      super(longitude, latitude);
      this.vs30 = vs30;
    }

    @Override
    public boolean isNull() {
      return super.isNull() && vs30 == null;
    }

    @Override
    public void checkValues() {
      super.checkValues();
      WsUtils.checkValue(ServicesUtil.Key.VS30, vs30);
    }
  }

  public static final class RequestData {

    final double longitude;
    final double latitude;
    final int vs30;
    final boolean truncate;
    final boolean maxdir;

    public RequestData(
        double longitude,
        double latitude,
        int vs30,
        boolean truncate,
        boolean maxdir) {

      this.longitude = longitude;
      this.latitude = latitude;
      this.vs30 = vs30;
      this.truncate = truncate;
      this.maxdir = maxdir;
    }

    // void checkParameters() {
    // checkParameter(longitude, "longitude");
    // checkParameter(latitude, "latitude");
    // checkParameter(vs30, "vs30");
    // }
  }

}

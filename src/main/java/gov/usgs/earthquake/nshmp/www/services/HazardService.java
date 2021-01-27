package gov.usgs.earthquake.nshmp.www.services;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.calc.HazardExport.curvesBySource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet.UrlHelper;
import gov.usgs.earthquake.nshmp.internal.www.Response;
import gov.usgs.earthquake.nshmp.internal.www.WsUtils;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.SourceType;
import gov.usgs.earthquake.nshmp.www.HazardController;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceQueryData;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceRequestData;
import gov.usgs.earthquake.nshmp.www.services.SourceServices.SourceModel;

import io.micronaut.http.HttpResponse;

/**
 * Probabilistic seismic hazard calculation handler for
 * {@link HazardController}.
 *
 * @author U.S. Geological Survey
 */
public final class HazardService {

  /*
   * Developer notes:
   *
   * Calculations are designed to leverage all available processors by default,
   * distributing work using the ServletUtil.CALC_EXECUTOR.
   */

  private static final String NAME = "Hazard Service";

  /**
   * Handler for {@link HazardController#doGetUsage}. Returns the usage for the
   * hazard service.
   *
   * @param urlHelper The URL helper
   */
  public static HttpResponse<String> handleDoGetUsage(UrlHelper urlHelper) {
    try {
      var usage = new RequestMetadata(ServletUtil.model());// SourceServices.ResponseData();
      var response = new Response<>(Status.USAGE, NAME, urlHelper.url, usage, urlHelper);
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, urlHelper);
    }
  }

  /**
   * Handler for {@link HazardController#doGetHazard}. Returns the usage or the
   * hazard result.
   *
   * @param query The query
   * @param urlHelper The URL helper
   */
  public static HttpResponse<String> handleDoGetHazard(
      QueryParameters query,
      UrlHelper urlHelper) {

    try {
      if (query.isEmpty()) {
        return handleDoGetUsage(urlHelper);
      }
      query.checkParameters();
      var data = new RequestData(query);
      var response = process(data, urlHelper);
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, urlHelper);
    }
  }

  static Response<RequestData, ResponseData> process(
      RequestData data,
      UrlHelper urlHelper)
      throws InterruptedException, ExecutionException {

    var configFunction = new ConfigFunction();
    var siteFunction = new SiteFunction(data);
    var stopwatch = Stopwatch.createStarted();
    var hazard = ServicesUtil.calcHazard(configFunction, siteFunction);

    return new ResultBuilder()
        .hazard(hazard)
        .requestData(data)
        .timer(stopwatch)
        .urlHelper(urlHelper)
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

    @Override
    public Site apply(CalcConfig config) {
      return Site.builder()
          .basinDataProvider(config.siteData.basinDataProvider)
          .location(Location.create(data.longitude, data.latitude))
          .vs30(data.vs30)
          .build();
    }
  }

  public static class QueryParameters {

    Optional<Double> longitude;
    Optional<Double> latitude;
    Optional<Integer> vs30;

    public QueryParameters(
        Optional<Double> longitude,
        Optional<Double> latitude,
        Optional<Integer> vs30) {

      this.longitude = longitude;
      this.latitude = latitude;
      this.vs30 = vs30;
    }

    public boolean isEmpty() {
      return longitude.isEmpty() && latitude.isEmpty() && vs30.isEmpty();
    }

    public void checkParameters() {
      checkParameter(longitude, "longitude");
      checkParameter(latitude, "latitude");
      checkParameter(vs30, "vs30");
    }
  }

  private static void checkParameter(Object param, String id) {
    checkNotNull(param, "Missing parameter: %s", id);
    // TODO check range here
  }

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

  static class RequestData {

    final double longitude;
    final double latitude;
    final double vs30;

    RequestData(QueryParameters query) {
      this.longitude = query.longitude.orElseThrow();
      this.latitude = query.latitude.orElseThrow();
      this.vs30 = query.vs30.orElseThrow();
    }
  }

  private static final class ResponseMetadata {
    // final SourceModel model;
    // final double latitude;
    // final double longitude;
    // final double vs30;
    final String imt;
    final String xlabel = "Ground Motion (g)";
    final String ylabel = "Annual Frequency of Exceedence";

    ResponseMetadata(Imt imt) {
      // model = new SourceModel(ServletUtil.model());
      // latitude = data.latitude;
      // longitude = data.longitude;
      // vs30 = data.vs30;
      this.imt = imtShortLabel(imt);
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

  @Deprecated
  static class RequestDataOld extends ServiceRequestData {
    final double vs30;

    RequestDataOld(Query query, double vs30) {
      super(query);
      this.vs30 = vs30;
    }
  }

  private static final class ResponseData {
    final Object server;
    final List<HazardResponse> hazards;

    ResponseData(Object server, List<HazardResponse> hazards) {
      this.server = server;
      this.hazards = hazards;
    }
  }

  private static final class HazardResponse {
    final ResponseMetadata metadata;
    final List<Curve> data;

    HazardResponse(ResponseMetadata metadata, List<Curve> data) {
      this.metadata = metadata;
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

    UrlHelper urlHelper;
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

    ResultBuilder urlHelper(UrlHelper urlHelper) {
      this.urlHelper = urlHelper;
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
        var responseData = new ResponseMetadata(imt);
        var curves = new ArrayList<Curve>();

        // total curve
        curves.add(new Curve(TOTAL_KEY, totalMap.get(imt)));

        // component curves
        var typeMap = componentMaps.get(imt);
        for (SourceType type : typeMap.keySet()) {
          curves.add(new Curve(type.toString(), typeMap.get(type)));
        }

        hazards.add(new HazardResponse(responseData, List.copyOf(curves)));
      }

      Object server = Metadata.serverData(ServletUtil.THREAD_COUNT, timer);
      var response = new ResponseData(server, List.copyOf(hazards));

      return new Response<>(Status.SUCCESS, NAME, request, response, urlHelper);
    }
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

}

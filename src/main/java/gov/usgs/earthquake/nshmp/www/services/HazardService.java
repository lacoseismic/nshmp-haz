package gov.usgs.earthquake.nshmp.www.services;

import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.calc.HazardExport.curvesBySource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Logger;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.data.MutableXySequence;
import gov.usgs.earthquake.nshmp.data.XySequence;
import gov.usgs.earthquake.nshmp.eq.model.SourceType;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet.UrlHelper;
import gov.usgs.earthquake.nshmp.internal.www.Response;
import gov.usgs.earthquake.nshmp.internal.www.WsUtils;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.www.BaseModel;
import gov.usgs.earthquake.nshmp.www.HazardController;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.Key;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceQueryData;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceRequestData;
import gov.usgs.earthquake.nshmp.www.services.ServletUtil.Timer;
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
  private static final Logger LOGGER = Logger.getLogger(HazardService.class.getName());

  /**
   * Handler for {@link HazardController#doGetUsage}. Returns the usage for the
   * hazard service.
   * 
   * @param urlHelper The URL helper
   */
  public static HttpResponse<String> handleDoGetUsage(UrlHelper urlHelper) {
    try {
      LOGGER.info(NAME + " - Request:\n" + urlHelper.url);
      var usage = new SourceServices.ResponseData();
      var response = new Response<>(Status.USAGE, NAME, urlHelper.url, usage, urlHelper);
      var svcResponse = ServletUtil.GSON.toJson(response);
      LOGGER.info(NAME + " - Response:\n" + svcResponse);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, LOGGER, urlHelper);
    }
  }

  /**
   * Handler for {@link HazardController#doGetHazard}. Returns the usage or the
   * hazard result.
   * 
   * @param query The query
   * @param urlHelper The URL helper
   */
  public static HttpResponse<String> handleDoGetHazard(Query query, UrlHelper urlHelper) {
    try {
      var timer = ServletUtil.timer();
      LOGGER.info(NAME + " - Request:\n" + ServletUtil.GSON.toJson(query));

      if (query.isNull()) {
        return handleDoGetUsage(urlHelper);
      }

      query.checkValues();
      var data = new RequestData(query, Vs30.fromValue(query.vs30));
      var response = process(data, timer, urlHelper);
      var svcResponse = ServletUtil.GSON.toJson(response);
      LOGGER.info(NAME + " - Response:\n" + svcResponse);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, LOGGER, urlHelper);
    }
  }

  static Response<RequestData, ResponseData> process(
      RequestData data,
      Timer timer,
      UrlHelper urlHelper) throws InterruptedException, ExecutionException {
    var configFunction = new ConfigFunction();
    var siteFunction = new SiteFunction(data);
    var hazard = ServicesUtil.calcHazard(configFunction, siteFunction);

    return new ResultBuilder()
        .hazard(hazard)
        .requestData(data)
        .timer(timer)
        .urlHelper(urlHelper)
        .build();
  }

  static class ConfigFunction implements Function<BaseModel, CalcConfig> {
    @Override
    public CalcConfig apply(BaseModel baseModel) {
      var hazardModel = ServletUtil.hazardModels().get(baseModel);
      var configBuilder = CalcConfig.copyOf(hazardModel.config());
      configBuilder.imts(baseModel.imts);
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
          .location(Location.create(data.latitude, data.longitude))
          .vs30(data.vs30.value())
          .build();
    }
  }

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
      WsUtils.checkValue(Key.VS30, vs30);
    }
  }

  static class RequestData extends ServiceRequestData {
    final Vs30 vs30;

    RequestData(Query query, Vs30 vs30) {
      super(query);
      this.vs30 = vs30;
    }
  }

  @SuppressWarnings("unused")
  private static final class ResponseMetadata {
    final SourceModel model;
    final double latitude;
    final double longitude;
    final Imt imt;
    final Vs30 vs30;
    final String xlabel = "Ground Motion (g)";
    final String ylabel = "Annual Frequency of Exceedence";

    ResponseMetadata(RequestData request, Imt imt) {
      model = new SourceModel(ServletUtil.installedModel());
      latitude = request.latitude;
      longitude = request.longitude;
      this.imt = imt;
      vs30 = request.vs30;
    }
  }

  @SuppressWarnings("unused")
  private static final class ResponseData {
    final Object server;
    final List<HazardResponse> hazards;

    ResponseData(Object server, List<HazardResponse> hazards) {
      this.server = server;
      this.hazards = hazards;
    }
  }

  @SuppressWarnings("unused")
  private static final class HazardResponse {
    final ResponseMetadata metadata;
    final List<Curve> data;

    HazardResponse(ResponseMetadata metadata, List<Curve> data) {
      this.metadata = metadata;
      this.data = data;
    }
  }

  @SuppressWarnings("unused")
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
    Timer timer;
    RequestData request;

    Map<Imt, Map<SourceType, MutableXySequence>> componentMaps;
    Map<Imt, MutableXySequence> totalMap;

    ResultBuilder hazard(Hazard hazardResult) {
      checkState(totalMap == null, "Hazard has already been added to this builder");

      componentMaps = new EnumMap<>(Imt.class);
      totalMap = new EnumMap<>(Imt.class);

      var typeTotalMaps = curvesBySource(hazardResult);

      for (var imt : hazardResult.curves().keySet()) {
        // total curve
        XySequence.addToMap(imt, totalMap, hazardResult.curves().get(imt));

        // component curves
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

    ResultBuilder timer(Timer timer) {
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
        var responseData = new ResponseMetadata(request, imt);
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
}

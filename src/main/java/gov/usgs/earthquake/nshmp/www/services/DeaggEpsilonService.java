package gov.usgs.earthquake.nshmp.www.services;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Deaggregation;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.eq.model.HazardModel;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet.UrlHelper;
import gov.usgs.earthquake.nshmp.internal.www.Response;
import gov.usgs.earthquake.nshmp.internal.www.WsUtils;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.www.DeaggEpsilonController;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.Key;
import gov.usgs.earthquake.nshmp.www.services.ServletUtil.Timer;
import gov.usgs.earthquake.nshmp.www.services.SourceServices.SourceModel;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;

/**
 * Hazard deaggregation handler for {@link DeaggEpsilonController}.
 *
 * @author U.S. Geological Survey
 */
public final class DeaggEpsilonService {

  /* Developer notes: See HazardService. */

  private static final String NAME = "Epsilon Deaggregation";
  private static URL basinUrl;

  public static void init() {
    try (InputStream config = Resources.getResource("config.properties").openStream()) {
      checkNotNull(config, "Missing config.properties");

      Properties props = new Properties();
      props.load(config);
      if (props.containsKey("basin_host")) {
        /*
         * TODO Site builder tests if service is working, which may be
         * inefficient for single call services.
         */
        var url = new URL(props.getProperty("basin_host") + "/nshmp/ws/data/basin");
        basinUrl = url;
      }
    } catch (IOException | NullPointerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Handler for {@link DeaggEpsilonController#doGetDeaggEpsilon}. Returns the
   * usage or the deagg result.
   * 
   * @param query The query
   * @param urlHelper The URL helper
   */
  public static HttpResponse<String> handleDoGetDeaggEpsilon(Query query, UrlHelper urlHelper) {
    try {
      var timer = ServletUtil.timer();

      if (query.isNull()) {
        return HazardService.handleDoGetUsage(urlHelper);
      }

      query.checkValues();
      var data = new RequestData(query, Vs30.fromValue(query.vs30));
      var response = process(data, timer, urlHelper);
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, urlHelper);
    }
  }

  /* Create map of IMT to deagg IML. */
  private static EnumMap<Imt, Double> readImtsFromQuery(HttpRequest<?> request) {
    var imtImls = new EnumMap<Imt, Double>(Imt.class);
    for (var param : request.getParameters().asMap().entrySet()) {
      if (isImtParam(param.getKey())) {
        imtImls.put(
            Imt.valueOf(param.getKey()),
            Double.valueOf(param.getValue().get(0)));
      }
    }
    return imtImls;
  }

  private static boolean isImtParam(String key) {
    return key.equals("PGA") || key.startsWith("SA");
  }

  private static Response<RequestData, ResponseData> process(
      RequestData data,
      Timer timer,
      UrlHelper urlHelper)
      throws InterruptedException, ExecutionException {
    var configFunction = new ConfigFunction();
    var siteFunction = new SiteFunction(data);
    var hazard = ServicesUtil.calcHazard(configFunction, siteFunction);
    var deagg = Deaggregation.atImls(hazard, data.imtImls, ServletUtil.CALC_EXECUTOR);
    return new ResultBuilder()
        .deagg(deagg)
        .requestData(data)
        .timer(timer)
        .urlHelper(urlHelper)
        .build();
  }

  public static class Query extends HazardService.Query {
    final EnumMap<Imt, Double> imtImls;
    final Boolean basin;

    public Query(
        HttpRequest<?> request,
        Double longitude,
        Double latitude,
        Integer vs30,
        Boolean basin) {
      super(longitude, latitude, vs30);
      imtImls = readImtsFromQuery(request);
      this.basin = basin == null ? false : basin;
    }

    @Override
    public boolean isNull() {
      return super.isNull() && vs30 == null;
    }

    @Override
    public void checkValues() {
      super.checkValues();
      WsUtils.checkValue(Key.BASIN, basin);
    }
  }

  static class ConfigFunction implements Function<HazardModel, CalcConfig> {
    @Override
    public CalcConfig apply(HazardModel model) {
      var configBuilder = CalcConfig.copyOf(model.config());
      return configBuilder.build();
    }
  }

  /*
   * Developer notes:
   *
   * We're opting here to fetch basin terms ourselves. If we were to set the
   * basin provider in the config, which requires additions to config, the URL
   * is tested every time a site is created for a servlet request. While this
   * worked for maps it's not good here.
   *
   * Site has logic for parsing the basin service response, which perhaps it
   * shouldn't. TODO is it worth decomposing data objects and services
   */
  static class SiteFunction implements Function<CalcConfig, Site> {
    final RequestData data;

    private SiteFunction(RequestData data) {
      this.data = data;
    }

    @Override
    public Site apply(CalcConfig config) {
      return Site.builder()
          .location(Location.create(data.latitude, data.longitude))
          .basinDataProvider(data.basin ? basinUrl : null)
          .vs30(data.vs30.value())
          .build();
    }
  }

  static final class RequestData extends HazardService.RequestData {
    final EnumMap<Imt, Double> imtImls;
    final boolean basin;

    RequestData(Query query, Vs30 vs30) {
      super(query, vs30);
      imtImls = query.imtImls;
      basin = query.basin;
    }
  }

  @SuppressWarnings("unused")
  private static final class ResponseMetadata {
    final List<SourceModel> models;
    final double longitude;
    final double latitude;
    final String imt;
    final double iml;
    final Vs30 vs30;
    final String rlabel = "Closest Distance, rRup (km)";
    final String mlabel = "Magnitude (Mw)";
    final String εlabel = "% Contribution to Hazard";
    final Object εbins;

    ResponseMetadata(Deaggregation deagg, RequestData request, Imt imt) {
      this.models = request.models;
      this.longitude = request.longitude;
      this.latitude = request.latitude;
      this.imt = imt.toString();
      this.iml = imt.period();
      this.vs30 = request.vs30;
      this.εbins = deagg.εBins();
    }
  }

  @SuppressWarnings("unused")
  private static final class ResponseData {
    final Object server;
    final List<DeaggResponse> deaggs;

    ResponseData(Object server, List<DeaggResponse> deaggs) {
      this.server = server;
      this.deaggs = deaggs;
    }
  }

  @SuppressWarnings("unused")
  private static final class DeaggResponse {
    final ResponseMetadata metadata;
    final Object data;

    DeaggResponse(ResponseMetadata metadata, Object data) {
      this.metadata = metadata;
      this.data = data;
    }
  }

  static final class ResultBuilder {
    UrlHelper urlHelper;
    Timer timer;
    RequestData request;
    Deaggregation deagg;

    ResultBuilder deagg(Deaggregation deagg) {
      this.deagg = deagg;
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
      ImmutableList.Builder<DeaggResponse> responseListBuilder = ImmutableList.builder();

      for (Imt imt : request.imtImls.keySet()) {
        ResponseMetadata responseData = new ResponseMetadata(deagg, request, imt);
        Object deaggs = deagg.toJsonCompact(imt);
        DeaggResponse response = new DeaggResponse(responseData, deaggs);
        responseListBuilder.add(response);
      }

      List<DeaggResponse> responseList = responseListBuilder.build();
      Object server = Metadata.serverData(ServletUtil.THREAD_COUNT, timer);
      var response = new ResponseData(server, responseList);

      return new Response<>(Status.SUCCESS, NAME, request, response, urlHelper);
    }
  }

}
package gov.usgs.earthquake.nshmp.www.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.ListenableFuture;

import gov.usgs.earthquake.nshmp.Maths;
import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.EqRate;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet.UrlHelper;
import gov.usgs.earthquake.nshmp.internal.www.Response;
import gov.usgs.earthquake.nshmp.internal.www.WsUtils;
import gov.usgs.earthquake.nshmp.internal.www.meta.ParamType;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.RateController;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.meta.Metadata.DefaultParameters;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.Key;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceQueryData;
import gov.usgs.earthquake.nshmp.www.services.ServicesUtil.ServiceRequestData;
import gov.usgs.earthquake.nshmp.www.services.ServletUtil.Timer;

import io.micronaut.http.HttpResponse;

/**
 * Earthquake probability and rate calculation handler service for
 * {@link RateController}.
 *
 * @author U.S. Geological Survey
 */
public final class RateService {

  /*
   * Developer notes:
   *
   * The RateService is currently single-threaded and does not submit jobs to a
   * request queue; see HazardService. However, jobs are placed on a thread in
   * the CALC_EXECUTOR thread pool to handle parallel calculation of CEUS and
   * WUS models.
   */

  private static final String TOTAL_KEY = "Total";

  /**
   * Handler for {@link RateController#doGetUsageRate} and
   * {@link RateController#doGetUsageProbability}.
   *
   * @param service The service
   * @param urlHelper The url helper
   */
  public static HttpResponse<String> handleDoGetUsage(Service service, UrlHelper urlHelper) {
    try {
      var response = metadata(service, urlHelper);
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(String.format(svcResponse, urlHelper.urlPrefix, urlHelper.urlPrefix));
    } catch (Exception e) {
      return ServicesUtil.handleError(e, service.name, urlHelper);
    }
  }

  /**
   * Handler for {@link RateController#doGetProbability},
   * {@link RateController#doGetProbabilitySlash},
   * {@link RateController#doGetRate}, and {@link RateController#doGetRateSlash}
   *
   * @param service The service
   * @param query The query
   * @param urlHelper The url helper
   * @return
   */
  public static HttpResponse<String> handleDoGetCalc(Query query, UrlHelper urlHelper) {
    var service = query.service;

    try {
      var timer = ServletUtil.timer();

      if (query.isNull()) {
        return handleDoGetUsage(service, urlHelper);
      }

      query.checkValues();
      var requestData = new RequestData(query);
      var response = processRequest(service, requestData, urlHelper, timer);
      var svcResponse = ServletUtil.GSON.toJson(response);
      return HttpResponse.ok(svcResponse);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, service.name, urlHelper);
    }
  }

  static Response<String, Usage> metadata(Service service, UrlHelper urlHelper) {
    var parameters = service == Service.RATE ? new RateParameters() : new ProbabilityParameters();
    var usage = new Usage(service, parameters);
    return new Response<>(Status.USAGE, service.name, urlHelper.url, usage, urlHelper);
  }

  static Response<RequestData, ResponseData> processRequest(
      Service service,
      RequestData data,
      UrlHelper urlHelper,
      Timer timer) throws InterruptedException, ExecutionException {
    var rates = calc(service, data);
    var responseData = new ResponseData(new ResponseMetadata(service, data), rates, timer);
    return new Response<>(Status.SUCCESS, service.name, data, responseData, urlHelper);
  }

  private static EqRate calc(Service service, RequestData data)
      throws InterruptedException, ExecutionException {
    var location = Location.create(data.longitude, data.latitude);
    var site = Site.builder().location(location).build();
    var futureRates = new ArrayList<ListenableFuture<EqRate>>();

    /*
     * Because we need to combine model results, intially calculate incremental
     * annual rates and only convert to cumulative probabilities at the end if
     * probability service has been called.
     */

    for (var model : ServletUtil.hazardModels()) {
      var rate = process(service, model, site, data.distance, data.timespan);
      futureRates.add(rate);
    }

    var rates = futureRates.stream()
        .map((future) -> {
          try {
            return future.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        }).collect(Collectors.toList())
        .toArray(new EqRate[] {});

    var ratesCombined = EqRate.combine(rates);

    if (service == Service.PROBABILITY) {
      ratesCombined = EqRate.toCumulative(ratesCombined);
      ratesCombined = EqRate.toPoissonProbability(ratesCombined, data.timespan.get());
    }

    return ratesCombined;
  }

  private static ListenableFuture<EqRate> process(
      Service service,
      HazardModel model,
      Site site,
      double distance,
      Optional<Double> timespan) {
    var configBuilder = CalcConfig.copyOf(model.config()).distance(distance);
    if (service == Service.PROBABILITY) {
      /* Also sets value format to Poisson probability. */
      configBuilder.timespan(timespan.get());
    }
    var config = configBuilder.build();
    var task = EqRate.callable(model, config, site);
    return ServletUtil.CALC_EXECUTOR.submit(task);
  }

  public static enum Service {
    RATE(
        "Earthquake Rate Service",
        "Compute incremental earthquake annual-rates at a location",
        List.of(
            "%srate/{longitude}/{latitude}/{distance}",
            "%srate?longitude={longitude}&latitude={latitude}&distance={distance}")),

    PROBABILITY(
        "Earthquake Probability Service",
        "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
        List.of(
            "%sprobability/{longitude}/{latitude}/{distance}/{timespan}",
            "%sprobability?longitude=<double>&latitude=<double>&distance=<double>&timespan=<double>"));

    private final String name;
    private final String description;
    private final List<String> syntax;

    private Service(String name, String description, List<String> syntax) {
      this.name = name;
      this.description = description;
      this.syntax = syntax;
    }
  }

  public static class Query extends ServiceQueryData {
    final Double distance;
    final Optional<Double> timespan;
    final Service service;

    public Query(
        Service service,
        Double longitude,
        Double latitude,
        Double distance,
        Optional<Double> timespan) {
      super(longitude, latitude);
      this.service = service;
      this.distance = distance;
      this.timespan = timespan;
    }

    @Override
    public boolean isNull() {
      return super.isNull() && distance == null &&
          ((service == Service.PROBABILITY && timespan.isEmpty()) || service == Service.RATE);
    }

    @Override
    public void checkValues() {
      super.checkValues();
      WsUtils.checkValue(Key.DISTANCE, distance);
      if (service == Service.PROBABILITY) {
        WsUtils.checkValue(Key.TIMESPAN, timespan.get());
      }
    }
  }

  static final class RequestData extends ServiceRequestData {
    final double distance;
    final Optional<Double> timespan;

    RequestData(Query query) {
      super(query);
      this.distance = query.distance;
      this.timespan = query.timespan;
    }
  }

  @SuppressWarnings("unused")
  private static final class ResponseMetadata {
    final double latitude;
    final double longitude;
    final double distance;
    final Double timespan;

    final String xlabel = "Magnitude (Mw)";
    final String ylabel;

    ResponseMetadata(Service service, RequestData request) {
      var isProbability = service == Service.PROBABILITY;
      this.longitude = request.longitude;
      this.latitude = request.latitude;
      this.distance = request.distance;
      this.ylabel = isProbability ? "Probability" : "Annual Rate (yr⁻¹)";
      this.timespan = request.timespan.orElse(null);
    }
  }

  @SuppressWarnings("unused")
  private static final class ResponseData {
    final Object server;
    final ResponseMetadata metadata;
    final List<Sequence> data;

    ResponseData(ResponseMetadata metadata, EqRate rates, Timer timer) {
      server = Metadata.serverData(ServletUtil.THREAD_COUNT, timer);
      this.metadata = metadata;
      this.data = buildSequence(rates);
    }

    List<Sequence> buildSequence(EqRate rates) {
      var sequences = new ArrayList<Sequence>();

      /* Total mfd. */
      var total = (!rates.totalMfd.isClear()) ? rates.totalMfd.trim() : rates.totalMfd;
      var totalOut = new Sequence(
          TOTAL_KEY,
          total.xValues().boxed().collect(Collectors.toList()),
          total.yValues().boxed().collect(Collectors.toList()));
      sequences.add(totalOut);

      /* Source type mfds. */
      for (var entry : rates.typeMfds.entrySet()) {
        var type = entry.getValue();
        if (type.isClear()) {
          continue;
        }
        type = type.trim();
        var typeOut = new Sequence(
            entry.getKey().toString(),
            type.xValues().boxed().collect(Collectors.toList()),
            type.yValues().boxed().collect(Collectors.toList()));
        sequences.add(typeOut);
      }

      return List.copyOf(sequences);
    }
  }

  /*
   * TODO would rather use this a general container for mfds and hazard curves.
   * See HazardService.Curve
   */
  @SuppressWarnings("unused")
  private static class Sequence {
    final String component;
    final List<Double> xvalues;
    final List<Double> yvalues;

    Sequence(String component, List<Double> xvalues, List<Double> yvalues) {
      this.component = component;
      this.xvalues = xvalues;
      this.yvalues = yvalues;
    }
  }

  @SuppressWarnings("unused")
  private static class Usage {
    final String description;
    final List<String> syntax;
    final Object server;
    final DefaultParameters parameters;

    private Usage(Service service, DefaultParameters parameters) {
      description = service.description;
      this.syntax = service.syntax;
      server = Metadata.serverData(1, ServletUtil.timer());
      this.parameters = parameters;
    }
  }

  @SuppressWarnings("unused")
  private static class RateParameters extends DefaultParameters {
    final DoubleParameter distance;

    RateParameters() {
      super();
      distance = new DoubleParameter(
          "Cutoff distance (in km)",
          ParamType.NUMBER,
          0.01,
          1000.0);
    }
  }

  @SuppressWarnings("unused")
  private static class ProbabilityParameters extends RateParameters {
    final DoubleParameter timespan;

    ProbabilityParameters() {
      timespan = new DoubleParameter(
          "Forecast time span (in years)",
          ParamType.NUMBER,
          Maths.TIMESPAN_RANGE.lowerEndpoint(),
          Maths.TIMESPAN_RANGE.upperEndpoint());
    }
  }

}

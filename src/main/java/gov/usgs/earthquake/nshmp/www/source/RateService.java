package gov.usgs.earthquake.nshmp.www.source;

import static gov.usgs.earthquake.nshmp.data.DoubleData.checkInRange;
import static gov.usgs.earthquake.nshmp.geo.Coordinates.checkLatitude;
import static gov.usgs.earthquake.nshmp.geo.Coordinates.checkLongitude;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ListenableFuture;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.EqRate;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.geo.Coordinates;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.HazVersion;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ResponseMetadata;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.ServletUtil.Server;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.source.SourceService.SourceModel;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Earthquake probability and rate calculation handler service for
 * {@link RateController}.
 *
 * @author U.S. Geological Survey
 */
@Singleton
public final class RateService {

  static final String NAME_RATE = "Earthquake Rate Service";
  static final String NAME_PROBABILITY = "Earthquake Probability Service";
  static final Logger LOG = LoggerFactory.getLogger(RateService.class);

  /*
   * Developer notes:
   *
   * The RateService is currently single-threaded and does not submit jobs to a
   * request queue; see HazardService. However, jobs are placed on a thread in
   * the CALC_EXECUTOR thread pool to handle parallel calculation of CEUS and
   * WUS models.
   */

  private static final String TOTAL_KEY = "Total";

  public static HttpResponse<String> getRate(Request request) {
    var stopwatch = Stopwatch.createStarted();
    var rates = calcRate(request);
    var response = new Response.Builder()
        .timer(stopwatch)
        .request(request)
        .rates(rates)
        .build();
    var body = ResponseBody.success()
        .name(NAME_RATE)
        .url(request.http.getUri().toString())
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(request)
        .response(response)
        .build();
    String json = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(json);
  }

  public static HttpResponse<String> getProbability(ProbRequest request) {
    var stopwatch = Stopwatch.createStarted();
    var rates = calcProbability(request);
    var response = new Response.Builder()
        .timer(stopwatch)
        .request(request)
        .rates(rates)
        .build();
    var body = ResponseBody.success()
        .name(NAME_PROBABILITY)
        .url(request.http.getUri().toString())
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(request)
        .response(response)
        .build();
    String json = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(json);
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
  // static HttpResponse<String> handleDoGetCalc(HttpRequest<?> request, Query
  // query) {
  // var service = query.service;
  //
  // try {
  //
  // if (query.isNull()) {
  // return handleDoGetUsage(request, service);
  // }
  //
  // query.checkValues();
  // var requestData = new RequestData(query);
  // var response = processRequest(request, service, requestData);
  // var svcResponse = ServletUtil.GSON.toJson(response);
  // return HttpResponse.ok(svcResponse);
  // } catch (Exception e) {
  // return ServletUtil.error(LOG, e, service.name, request.getUri().getPath());
  // }
  // }

  // static ResponseBody<String, Usage<DefaultParameters>>
  // metadata(HttpRequest<?> request,
  // Service service) {
  // var parameters = service == Service.RATE ? new RateParameters() : new
  // ProbabilityParameters();
  // var usage = new Usage<DefaultParameters>(service, parameters);
  // var url = request.getUri().getPath();
  // return ResponseBody.<String, Usage<DefaultParameters>> usage()
  // .name(service.name)
  // .url(url)
  // .metadata(new ResponseMetadata(HazVersion.appVersions()))
  // .request(url)
  // .response(usage)
  // .build();
  // }
  //
  // static ResponseBody<RequestData, ResponseData> processRequest(
  // HttpRequest<?> request,
  // Service service,
  // RequestData data) throws InterruptedException, ExecutionException {
  // var timer = Stopwatch.createStarted();
  // var rates = calc(service, data);
  // var response = new Response.Builder()
  //
  // new ServiceResponseMetadata(service, data), rates, timer);
  // return ResponseBody.<RequestData, ResponseData> success()
  // .name(service.name)
  // .request(data)
  // .metadata(new ResponseMetadata(HazVersion.appVersions()))
  // .response(responseData)
  // .url(request.getUri().getPath())
  // .build();
  // }

  private static EqRate calcRate(Request request) {
    return calc(request, OptionalDouble.empty());
  }

  private static EqRate calcProbability(ProbRequest request) {
    return calc(request, OptionalDouble.of(request.timespan));
  }

  private static EqRate calc(Request request, OptionalDouble timespan) {
    var location = Location.create(request.longitude, request.latitude);
    var site = Site.builder().location(location).build();
    var futureRates = new ArrayList<ListenableFuture<EqRate>>();

    /*
     * Because we need to combine model results, intially calculate incremental
     * annual rates and only convert to cumulative probabilities at the end if
     * probability service has been called.
     */

    var model = ServletUtil.model();
    var rate = process(model, site, request.distance, timespan);
    futureRates.add(rate);

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

    if (timespan.isPresent()) {
      ratesCombined = EqRate.toCumulative(ratesCombined);
      ratesCombined = EqRate.toPoissonProbability(ratesCombined, timespan.orElseThrow());
    }

    return ratesCombined;
  }

  // private static EqRate calc(Service service, RequestData data)
  // throws InterruptedException, ExecutionException {
  // var location = Location.create(data.longitude, data.latitude);
  // var site = Site.builder().location(location).build();
  // var futureRates = new ArrayList<ListenableFuture<EqRate>>();
  //
  // /*
  // * Because we need to combine model results, intially calculate incremental
  // * annual rates and only convert to cumulative probabilities at the end if
  // * probability service has been called.
  // */
  //
  // var model = ServletUtil.model();
  // var rate = process(service, model, site, data.distance, data.timespan);
  // futureRates.add(rate);
  //
  // var rates = futureRates.stream()
  // .map((future) -> {
  // try {
  // return future.get();
  // } catch (InterruptedException | ExecutionException e) {
  // throw new RuntimeException(e);
  // }
  // }).collect(Collectors.toList())
  // .toArray(new EqRate[] {});
  //
  // var ratesCombined = EqRate.combine(rates);
  //
  // if (service == Service.PROBABILITY) {
  // ratesCombined = EqRate.toCumulative(ratesCombined);
  // ratesCombined = EqRate.toPoissonProbability(ratesCombined,
  // data.timespan.get());
  // }
  //
  // return ratesCombined;
  // }

  // private static HttpResponse<String> handleDoGetUsage(HttpRequest<?>
  // request, Service service) {
  // var response = metadata(request, service);
  // var json = ServletUtil.GSON.toJson(response);
  // return HttpResponse.ok(json);
  // }

  private static ListenableFuture<EqRate> process(
      HazardModel model,
      Site site,
      double distance,
      OptionalDouble timespan) {
    var configBuilder = CalcConfig.copyOf(model.config()).distance(distance);
    if (timespan.isPresent()) {
      /* Also sets value format to Poisson probability. */
      configBuilder.timespan(timespan.getAsDouble());
    }
    var config = configBuilder.build();
    var task = EqRate.callable(model, config, site);
    return ServletUtil.CALC_EXECUTOR.submit(task);
  }

  // private static ListenableFuture<EqRate> process(
  // Service service,
  // HazardModel model,
  // Site site,
  // double distance,
  // Optional<Double> timespan) {
  // var configBuilder = CalcConfig.copyOf(model.config()).distance(distance);
  // if (service == Service.PROBABILITY) {
  // /* Also sets value format to Poisson probability. */
  // configBuilder.timespan(timespan.get());
  // }
  // var config = configBuilder.build();
  // var task = EqRate.callable(model, config, site);
  // return ServletUtil.CALC_EXECUTOR.submit(task);
  // }

  // public static enum Service {
  // RATE(
  // "Earthquake Rate Service",
  // "Compute incremental earthquake annual-rates at a location",
  // List.of(
  // "%srate/{longitude}/{latitude}/{distance}",
  // "%srate?longitude={longitude}&latitude={latitude}&distance={distance}")),
  //
  // PROBABILITY(
  // "Earthquake Probability Service",
  // "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
  // List.of(
  // "%sprobability/{longitude}/{latitude}/{distance}/{timespan}",
  // "%sprobability?longitude=<double>&latitude=<double>&distance=<double>&timespan=<double>"));
  //
  // private final String name;
  // private final String description;
  // private final List<String> syntax;
  //
  // private Service(String name, String description, List<String> syntax) {
  // this.name = name;
  // this.description = description;
  // this.syntax = syntax;
  // }
  // }

  // public static class Query extends ServiceQueryData {
  // final Double distance;
  // final Optional<Double> timespan;
  // final Service service;
  //
  // public Query(
  // Service service,
  // Double longitude,
  // Double latitude,
  // Double distance,
  // Optional<Double> timespan) {
  // super(longitude, latitude);
  // this.service = service;
  // this.distance = distance;
  // this.timespan = timespan;
  // }
  //
  // @Override
  // public boolean isNull() {
  // return super.isNull() && distance == null &&
  // ((service == Service.PROBABILITY && timespan.isEmpty()) || service ==
  // Service.RATE);
  // }
  //
  // @Override
  // public void checkValues() {
  // super.checkValues();
  // WsUtils.checkValue(Key.DISTANCE, distance);
  // if (service == Service.PROBABILITY) {
  // WsUtils.checkValue(Key.TIMESPAN, timespan.get());
  // }
  // }
  // }

  // static final class RequestData extends ServiceRequestData {
  // final double distance;
  // final Optional<Double> timespan;
  //
  // RequestData(Query query) {
  // super(query);
  // this.distance = query.distance;
  // this.timespan = query.timespan;
  // }
  //
  // public double getDistance() {
  // return distance;
  // }
  //
  // public Optional<Double> getTimespan() {
  // return timespan;
  // }
  // }

  static class Response {

    final Metadata metadata;
    final List<Sequence> data;

    Response(Metadata metadata, List<Sequence> data) {
      this.metadata = metadata;
      this.data = data;
    }

    public Metadata getMetadata() {
      return metadata;
    }

    public List<Sequence> getData() {
      return data;
    }

    private static final class Metadata {
      final Server server;
      final String xlabel = "Ground Motion (g)";
      final String ylabel = "Annual Frequency of Exceedance";

      Metadata(Server server) {
        this.server = server;
      }

      public Server getServer() {
        return server;
      }

      public String getXLabel() {
        return xlabel;
      }

      public String getYLabel() {
        return ylabel;
      }
    }

    private static final class Builder {

      Stopwatch timer;
      Request request;
      List<Sequence> data;

      Builder timer(Stopwatch timer) {
        this.timer = timer;
        return this;
      }

      Builder request(Request request) {
        this.request = request;
        return this;
      }

      Builder rates(EqRate rates) {
        this.data = buildSequences(rates);
        return this;
      }

      private List<Sequence> buildSequences(EqRate rates) {
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

      Response build() {
        Server server = ServletUtil.serverData(ServletUtil.THREAD_COUNT, timer);
        return new Response(new Response.Metadata(server), data);
      }
    }
  }

  // private static final class ServiceResponseMetadata {
  // final double latitude;
  // final double longitude;
  // final double distance;
  // final Double timespan;
  //
  // final String xlabel = "Magnitude (Mw)";
  // final String ylabel;
  //
  // ServiceResponseMetadata(Service service, RequestData request) {
  // var isProbability = service == Service.PROBABILITY;
  // this.longitude = request.longitude;
  // this.latitude = request.latitude;
  // this.distance = request.distance;
  // this.ylabel = isProbability ? "Probability" : "Annual Rate (yr⁻¹)";
  // this.timespan = request.timespan.orElse(null);
  // }
  //
  // public double getLatitude() {
  // return latitude;
  // }
  //
  // public double getLongitude() {
  // return longitude;
  // }
  //
  // public double getDistance() {
  // return distance;
  // }
  //
  // public Double getTimespan() {
  // return timespan;
  // }
  //
  // public String getXlabel() {
  // return xlabel;
  // }
  //
  // public String getYLabel() {
  // return ylabel;
  // }
  // }

  // static final class ResponseData {
  // final Server server;
  // final ServiceResponseMetadata metadata;
  // final List<Sequence> data;
  //
  // ResponseData(ServiceResponseMetadata metadata, EqRate rates, Stopwatch
  // timer) {
  // server = ServletUtil.serverData(ServletUtil.THREAD_COUNT, timer);
  // this.metadata = metadata;
  // this.data = buildSequence(rates);
  // }
  //
  // public Server getServer() {
  // return server;
  // }
  //
  // public ServiceResponseMetadata getMetadata() {
  // return metadata;
  // }
  //
  // public List<Sequence> getData() {
  // return data;
  // }
  //
  // List<Sequence> buildSequence(EqRate rates) {
  // var sequences = new ArrayList<Sequence>();
  //
  // /* Total mfd. */
  // var total = (!rates.totalMfd.isClear()) ? rates.totalMfd.trim() :
  // rates.totalMfd;
  // var totalOut = new Sequence(
  // TOTAL_KEY,
  // total.xValues().boxed().collect(Collectors.toList()),
  // total.yValues().boxed().collect(Collectors.toList()));
  // sequences.add(totalOut);
  //
  // /* Source type mfds. */
  // for (var entry : rates.typeMfds.entrySet()) {
  // var type = entry.getValue();
  // if (type.isClear()) {
  // continue;
  // }
  // type = type.trim();
  // var typeOut = new Sequence(
  // entry.getKey().toString(),
  // type.xValues().boxed().collect(Collectors.toList()),
  // type.yValues().boxed().collect(Collectors.toList()));
  // sequences.add(typeOut);
  // }
  //
  // return List.copyOf(sequences);
  // }
  // }

  /*
   * Would rather use this a general container for mfds and hazard curves. See
   * HazardService.Curve
   */
  private static class Sequence {
    final String component;
    final List<Double> xvalues;
    final List<Double> yvalues;

    Sequence(String component, List<Double> xvalues, List<Double> yvalues) {
      this.component = component;
      this.xvalues = xvalues;
      this.yvalues = yvalues;
    }

    public String getComponent() {
      return component;
    }

    public List<Double> getXvalues() {
      return xvalues;
    }

    public List<Double> getYvalues() {
      return yvalues;
    }
  }

  // static class Usage<T extends DefaultParameters> {
  // final String description;
  // final List<String> syntax;
  // final T parameters;
  //
  // private Usage(Service service, T parameters) {
  // description = service.description;
  // this.syntax = service.syntax;
  // this.parameters = parameters;
  // }
  //
  // public String getDescription() {
  // return description;
  // }
  //
  // public List<String> getSyntax() {
  // return syntax;
  // }
  //
  // public T getParameters() {
  // return parameters;
  // }
  // }

  // static class RateParameters extends DefaultParameters {
  // final DoubleParameter distance;
  //
  // RateParameters() {
  // super();
  // distance = new DoubleParameter(
  // "Cutoff distance",
  // "km",
  // 0.01,
  // 1000.0);
  // }
  //
  // public DoubleParameter getDistance() {
  // return distance;
  // }
  // }
  //
  // static class ProbabilityParameters extends RateParameters {
  // final DoubleParameter timespan;
  //
  // ProbabilityParameters() {
  // timespan = new DoubleParameter(
  // "Forecast time span",
  // "years",
  // Maths.TIMESPAN_RANGE.lowerEndpoint(),
  // Maths.TIMESPAN_RANGE.upperEndpoint());
  // }
  //
  // public DoubleParameter getTimespan() {
  // return timespan;
  // }
  // }

  /*********************/

  private static final Range<Double> DISTANCE_RANGE = Range.closed(0.01, 1000.0);
  private static final Range<Double> TIMESPAN_RANGE = Range.closed(1.0, 10000.0);

  static class Request {

    final transient HttpRequest<?> http;
    final double longitude;
    final double latitude;
    final double distance;

    public Request(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double distance) {
      this.http = http;
      this.longitude = checkLongitude(longitude);
      this.latitude = checkLatitude(latitude);
      this.distance = checkInRange(DISTANCE_RANGE, "Distance cutoff", distance);
    }

    public double getLongitude() {
      return longitude;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getDistance() {
      return distance;
    }
  }

  static class ProbRequest extends Request {

    final double timespan;

    public ProbRequest(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double distance,
        double timespan) {
      super(http, longitude, latitude, distance);
      this.timespan = checkInRange(TIMESPAN_RANGE, "Forecast timespan", timespan);
    }

    public double getTimespan() {
      return timespan;
    }
  }

  public static HttpResponse<String> getRateMetadata(HttpRequest<?> request) {
    var url = request.getUri().toString();
    var usage = new Metadata(ServletUtil.model());
    var body = ResponseBody.usage()
        .name(NAME_RATE)
        .url(url)
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(url)
        .response(usage)
        .build();
    var json = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(json);
  }

  public static HttpResponse<String> getProbMetadata(HttpRequest<?> request) {
    var url = request.getUri().toString();
    var usage = new ProbMetadata(ServletUtil.model());
    var body = ResponseBody.usage()
        .name(NAME_RATE)
        .url(url)
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(url)
        .response(usage)
        .build();
    var json = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(json);
  }

  static class Metadata {

    final SourceModel model;
    final DoubleParameter longitude;
    final DoubleParameter latitude;
    final DoubleParameter distance;

    Metadata(HazardModel model) {
      this.model = new SourceModel(model);
      // TODO should get min max from model (fix via swagger openapi injection)
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

      distance = new DoubleParameter(
          "Cutoff distance",
          "km",
          0.01,
          1000.0);
    }

    public SourceModel getModel() {
      return model;
    }

    public DoubleParameter getLongitude() {
      return longitude;
    }

    public DoubleParameter getLatitude() {
      return latitude;
    }

    public DoubleParameter getDistance() {
      return distance;
    }
  }

  static class ProbMetadata extends Metadata {

    final DoubleParameter timespan;

    ProbMetadata(HazardModel model) {
      super(model);
      timespan = new DoubleParameter(
          "Forecast timespan",
          "years",
          1.0,
          10000.0);
    }

    public DoubleParameter getTimespan() {
      return timespan;
    }
  }

}

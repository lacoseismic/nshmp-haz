package gov.usgs.earthquake.nshmp.www.hazard;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Disaggregation;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.hazard.HazardService.Metadata;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Disaggregation service.
 *
 * @see DisaggController
 * @author U.S. Geological Survey
 */
@Singleton
public final class DisaggService {

  /*
   * Developer notes:
   *
   * Same query structure as hazard service, but either return period and imt(s)
   * OR imt=iml pairs
   */

  static final String NAME = "Disaggregation Service";
  static final Logger LOG = LoggerFactory.getLogger(DisaggService.class);

  private static Range<Double> rpRange = Range.closed(1.0, 20000.0);
  private static Range<Double> imlRange = Range.closed(0.0001, 8.0);

  /** HazardController.doGetMetadata() handler. */
  public static HttpResponse<String> getMetadata(HttpRequest<?> request) {
    var url = request.getUri().toString();
    var usage = new Metadata(ServletUtil.model());
    var response = ResponseBody.usage()
        .name(NAME)
        .url(url)
        .request(url)
        .response(usage)
        .build();
    var svcResponse = ServletUtil.GSON.toJson(response);
    return HttpResponse.ok(svcResponse);
  }

  /** HazardController.doGetDisaggIml() handler. */
  public static HttpResponse<String> getDisaggIml(RequestIml request)
      throws InterruptedException, ExecutionException {
    var stopwatch = Stopwatch.createStarted();
    var disagg = calcDisaggIml(request);
    var response = new Response.Builder()
        .timer(stopwatch)
        .request(request)
        .disagg(disagg)
        .build();
    var body = ResponseBody.success()
        .name(NAME)
        .url(request.http.getUri().toString())
        .request(request)
        .response(response)
        .build();
    String svcResponse = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(svcResponse);
  }

  /** HazardController.doGetDisaggRp() handler. */
  public static HttpResponse<String> getDisaggRp(RequestRp request)
      throws InterruptedException, ExecutionException {
    var stopwatch = Stopwatch.createStarted();
    var disagg = calcDisaggRp(request);
    var response = new Response.Builder()
        .timer(stopwatch)
        .request(request)
        .disagg(disagg)
        .build();
    var body = ResponseBody.success()
        .name(NAME)
        .url(request.http.getUri().toString())
        .request(request)
        .response(response)
        .build();
    String svcResponse = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(svcResponse);
  }

  /*
   * Developer notes:
   *
   * If disaggIml, we need to do the calculation for single XySeqs if disaggRp,
   * we don't know the imls so must compute hazard over the full curve
   *
   */

  static Disaggregation calcDisaggIml(RequestIml request)
      throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();

    // modify config to include service endpoint arguments
    CalcConfig config = CalcConfig.copyOf(model.config())
        .imts(request.imls.keySet())
        .build();

    // TODO this needs to pick up SiteData, centralize
    Site site = Site.builder()
        .location(Location.create(request.longitude, request.latitude))
        .vs30(request.vs30)
        .build();

    // use HazardService.calcHazard() instead?
    CompletableFuture<Hazard> hazFuture = CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(
            model, config, site,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    Hazard hazard = hazFuture.get();

    CompletableFuture<Disaggregation> disaggfuture = CompletableFuture.supplyAsync(
        () -> Disaggregation.atImls(
            hazard, request.imls,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    Disaggregation disagg = disaggfuture.get();

    return disagg;
  }

  static Disaggregation calcDisaggRp(RequestRp request)
      throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();

    // modify config to include service endpoint arguments
    CalcConfig config = CalcConfig.copyOf(model.config())
        .imts(request.imts)
        .build();

    // TODO this needs to pick up SiteData, centralize
    Site site = Site.builder()
        .location(Location.create(request.longitude, request.latitude))
        .vs30(request.vs30)
        .build();

    // could just get from HazardService
    CompletableFuture<Hazard> hazFuture = CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(
            model, config, site,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    Hazard hazard = hazFuture.get();

    CompletableFuture<Disaggregation> disaggfuture = CompletableFuture.supplyAsync(
        () -> Disaggregation.atReturnPeriod(
            hazard, request.returnPeriod,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    Disaggregation disagg = disaggfuture.get();

    return disagg;
  }

  static final class RequestIml {

    final transient HttpRequest<?> http;
    final double longitude;
    final double latitude;
    final double vs30;
    final Map<Imt, Double> imls;

    RequestIml(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double vs30,
        Map<Imt, Double> imls) {

      this.http = http;
      this.longitude = longitude;
      this.latitude = latitude;
      this.vs30 = vs30;
      this.imls = imls;
    }
  }

  static final class RequestRp {

    final transient HttpRequest<?> http;
    final double longitude;
    final double latitude;
    final double vs30;
    final double returnPeriod;
    final Set<Imt> imts;

    RequestRp(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double vs30,
        double returnPeriod,
        Set<Imt> imts) {

      this.http = http;
      this.longitude = longitude;
      this.latitude = latitude;
      this.vs30 = vs30;
      this.returnPeriod = returnPeriod;
      this.imts = imts.isEmpty()
          ? ServletUtil.model().config().hazard.imts
          : imts;
    }
  }

  private static final class Response {
    final Response.Metadata metadata;
    final List<ImtDisagg> disaggs;

    Response(Response.Metadata metadata, List<ImtDisagg> disaggs) {
      this.metadata = metadata;
      this.disaggs = disaggs;
    }

    private static final class Metadata {
      final Object server;
      final String rlabel = "Closest Distance, rRup (km)";
      final String mlabel = "Magnitude (Mw)";
      final String εlabel = "% Contribution to Hazard";
      final Object εbins;

      Metadata(Object server, Object εbins) {
        this.server = server;
        this.εbins = εbins;
      }
    }

    private static final class Builder {

      Stopwatch timer;
      Optional<RequestRp> requestRp = Optional.empty();
      Optional<RequestIml> requestIml = Optional.empty();
      Disaggregation disagg;

      Builder timer(Stopwatch timer) {
        this.timer = timer;
        return this;
      }

      Builder request(Object request) {
        if (request instanceof RequestRp) {
          requestRp = Optional.of((RequestRp) request);
          return this;
        }
        requestIml = Optional.of((RequestIml) request);
        return this;
      }

      Builder disagg(Disaggregation disagg) {
        this.disagg = disagg;
        return this;
      }

      Response build() {

        Set<Imt> imts = requestRp.isPresent()
            ? requestRp.orElseThrow().imts
            : requestIml.orElseThrow().imls.keySet();

        List<ImtDisagg> disaggs = imts.stream()
            .map(imt -> new ImtDisagg(imt, disagg.toJson(imt)))
            .collect(toList());

        Object server = ServletUtil.serverData(ServletUtil.THREAD_COUNT, timer);

        return new Response(
            new Response.Metadata(server, disagg.εBins()),
            disaggs);
      }
    }
  }

  private static final class ImtDisagg {
    final Parameter imt;
    final Object data;

    ImtDisagg(Imt imt, Object data) {
      this.imt = new Parameter(
          ServletUtil.imtShortLabel(imt),
          imt.name());
      this.data = data;
    }
  }
}

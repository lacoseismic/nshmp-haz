package gov.usgs.earthquake.nshmp.www.hazard;

import static gov.usgs.earthquake.nshmp.calc.DataType.DISAGG_DATA;
import static gov.usgs.earthquake.nshmp.calc.DataType.GMM;
import static gov.usgs.earthquake.nshmp.calc.DataType.SOURCE;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.DisaggCalc;
import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.DataType;
import gov.usgs.earthquake.nshmp.calc.Disaggregation;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Gmm;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.HazVersion;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ResponseMetadata;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.ServletUtil.Server;
import gov.usgs.earthquake.nshmp.www.hazard.HazardService.BaseRequest;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;
import gov.usgs.earthquake.nshmp.www.source.SourceService.SourceModel;

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

  static final String NAME = "Hazard Disaggregation";
  static final Logger LOG = LoggerFactory.getLogger(DisaggService.class);

  // TODO range check return periods and imls
  private static Range<Double> rpRange = Range.closed(1.0, 20000.0);
  private static Range<Double> imlRange = Range.closed(0.0001, 8.0);
  private static Range<Double> imlPgvRange = Range.closed(0.1, 1000.0);

  /* For Swagger selctions */
  enum DisaggDataType {
    GMM,
    SOURCE,
    DISAGG_DATA;
  }

  static HttpResponse<String> getMetadata(HttpRequest<?> request) {
    var url = request.getUri().toString();
    var usage = new Metadata(ServletUtil.model());
    var response = ResponseBody.usage()
        .name(NAME)
        .url(url)
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(url)
        .response(usage)
        .build();
    var svcResponse = ServletUtil.GSON.toJson(response);
    return HttpResponse.ok(svcResponse);
  }

  static HttpResponse<String> getDisaggIml(RequestIml request)
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
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(request)
        .response(response)
        .build();
    String svcResponse = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(svcResponse);
  }

  static HttpResponse<String> getDisaggRp(RequestRp request)
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
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
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
   */

  private static Disaggregation calcDisaggIml(RequestIml request)
      throws InterruptedException, ExecutionException {
    HazardModel model = ServletUtil.model();

    // modify config to include service endpoint arguments
    CalcConfig config = CalcConfig.copyOf(model.config())
        .imts(request.imls.keySet())
        .build();

    Location loc = Location.create(request.longitude, request.latitude);
    Site site = ServletUtil.createSite(loc, request.vs30, model.siteData());

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

  private static Disaggregation calcDisaggRp(RequestRp request)
      throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();

    // modify config to include service endpoint arguments
    CalcConfig config = CalcConfig.copyOf(model.config())
        .imts(request.imts)
        .build();

    Location loc = Location.create(request.longitude, request.latitude);
    Site site = ServletUtil.createSite(loc, request.vs30, model.siteData());

    // could just get from HazardService
    CompletableFuture<Hazard> hazFuture = CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(
            model, config, site,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    Hazard hazard = hazFuture.get();
    Map<Imt, Double> imls = DisaggCalc.imlsForReturnPeriod(
        hazard,
        request.returnPeriod);

    CompletableFuture<Disaggregation> disaggfuture = CompletableFuture.supplyAsync(
        () -> Disaggregation.atImls(
            hazard, imls,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    Disaggregation disagg = disaggfuture.get();

    return disagg;
  }

  private static DoubleParameter imlParameter(Imt imt) {
    String name = ServletUtil.imtShortLabel(imt) + " IML range";

    if (imt.equals(Imt.PGV)) {
      return new DoubleParameter(
          name,
          "cm/s",
          imlPgvRange.lowerEndpoint(),
          imlPgvRange.upperEndpoint());
    } else {
      return new DoubleParameter(name, "g", imlRange.lowerEndpoint(), imlRange.upperEndpoint());
    }
  }

  static final class RequestIml extends BaseRequest {
    final Map<Imt, Double> imls;
    final Set<DataType> dataTypes;

    RequestIml(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double vs30,
        Map<Imt, Double> imls,
        Set<DataType> dataTypes) {
      super(http, longitude, latitude, vs30);

      this.imls = imls;
      this.dataTypes = dataTypes;
    }

    public Map<Imt, Double> getImls() {
      return imls;
    }

    public Set<DataType> getDataTypes() {
      return dataTypes;
    }
  }

  static final class RequestRp extends BaseRequest {
    final double returnPeriod;
    final Set<DataType> dataTypes;
    final Set<Imt> imts;

    RequestRp(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double vs30,
        Set<Imt> imts,
        double returnPeriod,
        Set<DataType> dataTypes) {
      super(http, longitude, latitude, vs30);

      this.returnPeriod = returnPeriod;
      this.dataTypes = dataTypes;
      this.imts = imts.isEmpty()
          ? ServletUtil.model().config().hazard.imts
          : imts;
    }

    public double getReturnPeriod() {
      return returnPeriod;
    }

    public Set<DataType> getDataTypes() {
      return dataTypes;
    }

    public Set<Imt> getImts() {
      return imts;
    }
  }

  static final class Response {
    final ResponseMetadata metadata;
    final List<ImtDisagg> disaggs;

    Response(ResponseMetadata metadata, List<ImtDisagg> disaggs) {
      this.metadata = metadata;
      this.disaggs = disaggs;
    }

    public ResponseMetadata getMetadata() {
      return metadata;
    }

    public List<ImtDisagg> getDisaggs() {
      return disaggs;
    }

    private static final class ResponseMetadata {
      final Server server;
      final String rlabel = "Closest Distance, rRup (km)";
      final String mlabel = "Magnitude (Mw)";
      final String ??label = "% Contribution to Hazard";
      final Object ??bins;

      ResponseMetadata(Server server, Object ??bins) {
        this.server = server;
        this.??bins = ??bins;
      }

      public Server getServer() {
        return server;
      }

      public String getRLabel() {
        return rlabel;
      }

      public String getMLabel() {
        return mlabel;
      }

      public String get??Label() {
        return ??label;
      }

      public Object get??bins() {
        return ??bins;
      }

    }

    private static final class Builder {

      Stopwatch timer;
      Optional<RequestRp> requestRp = Optional.empty();
      Optional<RequestIml> requestIml = Optional.empty();
      Disaggregation disagg;
      CalcConfig config;

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

        Set<DataType> dataTypes = requestRp.isPresent()
            ? requestRp.orElseThrow().dataTypes
            : requestIml.orElseThrow().dataTypes;

        List<ImtDisagg> disaggs = imts.stream()
            .map(imt -> new ImtDisagg(imt, disagg.toJson(
                imt,
                dataTypes.contains(GMM),
                dataTypes.contains(SOURCE),
                dataTypes.contains(DISAGG_DATA))))
            .collect(toList());

        var server = ServletUtil.serverData(ServletUtil.THREAD_COUNT, timer);

        return new Response(
            new ResponseMetadata(server, disagg.??Bins()),
            disaggs);
      }
    }
  }

  private static final class ImlMetadata {
    private final String name;
    private final TreeMap<String, DoubleParameter> range;

    private ImlMetadata(HazardModel model) {
      name = "Intensity Measure Level";
      range = model.gmms().stream()
          .map(Gmm::supportedImts)
          .flatMap(Set::stream)
          .distinct()
          .sorted()
          .collect(
              Collectors.toMap(Imt::name, DisaggService::imlParameter, (x, y) -> x, TreeMap::new));
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

    public Parameter getImt() {
      return imt;
    }

    public Object getData() {
      return data;
    }
  }

  private static class Metadata {
    final SourceModel model;
    final DoubleParameter longitude;
    final DoubleParameter latitude;
    final ImlMetadata iml;
    final DoubleParameter returnPeriod;
    final DoubleParameter vs30;

    Metadata(HazardModel model) {
      this.model = new SourceModel(model);

      longitude = new DoubleParameter(
          "Longitude",
          "??",
          model.bounds().min.longitude,
          model.bounds().max.longitude);

      latitude = new DoubleParameter(
          "Latitude",
          "??",
          model.bounds().min.latitude,
          model.bounds().max.latitude);

      iml = new ImlMetadata(model);

      returnPeriod = new DoubleParameter(
          "Return Period",
          "yr",
          rpRange.lowerEndpoint(),
          rpRange.upperEndpoint());

      vs30 = new DoubleParameter(
          "Vs30",
          "m/s",
          150,
          1500);
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

    public ImlMetadata getIml() {
      return iml;
    }

    public DoubleParameter getReturnPeriod() {
      return returnPeriod;
    }

    public DoubleParameter getVs30() {
      return vs30;
    }
  }
}

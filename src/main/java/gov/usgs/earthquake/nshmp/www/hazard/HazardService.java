package gov.usgs.earthquake.nshmp.www.hazard;

import static gov.usgs.earthquake.nshmp.calc.HazardExport.curvesBySource;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkInRange;
import static gov.usgs.earthquake.nshmp.geo.Coordinates.checkLatitude;
import static gov.usgs.earthquake.nshmp.geo.Coordinates.checkLongitude;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.DataType;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.data.MutableXySequence;
import gov.usgs.earthquake.nshmp.data.XySequence;
import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.SourceType;
import gov.usgs.earthquake.nshmp.www.HazVersion;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ResponseMetadata;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.ServletUtil.Server;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;
import gov.usgs.earthquake.nshmp.www.source.SourceService.SourceModel;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Hazard service.
 *
 * @see HazardController
 * @author U.S. Geological Survey
 */
@Singleton
public final class HazardService {

  static final String NAME = "Hazard Curves";
  static final Logger LOG = LoggerFactory.getLogger(HazardService.class);

  private static final String TOTAL_KEY = "Total";

  public static HttpResponse<String> getMetadata(HttpRequest<?> request) {
    var url = request.getUri().toString();
    var usage = new Metadata(ServletUtil.model());
    var body = ResponseBody.usage()
        .name(NAME)
        .url(url)
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(url)
        .response(usage)
        .build();
    var json = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(json);
  }

  public static HttpResponse<String> getHazard(Request request)
      throws InterruptedException, ExecutionException {
    var stopwatch = Stopwatch.createStarted();
    var hazard = calcHazard(request);
    var response = new Response.Builder()
        .timer(stopwatch)
        .request(request)
        .hazard(hazard)
        .build();
    var body = ResponseBody.success()
        .name(NAME)
        .url(request.http.getUri().toString())
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(request)
        .response(response)
        .build();
    String json = ServletUtil.GSON2.toJson(body);
    return HttpResponse.ok(json);
  }

  /*
   * Developer notes:
   *
   * Future calculation configuration options: vertical GMs
   *
   * NSHM Hazard Tool will not pass truncation and maxdir args/flags as the apps
   * apply truncation and scaling on the client.
   */

  static Hazard calcHazard(Request request)
      throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();

    // modify config to include service endpoint arguments
    CalcConfig config = CalcConfig.copyOf(model.config())
        .imts(request.imts)
        .build();

    Location loc = Location.create(request.longitude, request.latitude);
    Site site = ServletUtil.createSite(loc, request.vs30, model.siteData());

    CompletableFuture<Hazard> future = CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(
            model, config, site,
            ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);

    return future.get();
  }

  static class Metadata {

    final SourceModel model;
    final DoubleParameter longitude;
    final DoubleParameter latitude;
    final DoubleParameter vs30;

    Metadata(HazardModel model) {
      this.model = new SourceModel(model);
      longitude = new DoubleParameter(
          "Longitude",
          "°",
          model.bounds().min.longitude,
          model.bounds().max.longitude);

      latitude = new DoubleParameter(
          "Latitude",
          "°",
          model.bounds().min.latitude,
          model.bounds().max.latitude);

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

    public DoubleParameter getVs30() {
      return vs30;
    }
  }

  /* Base request class for both hazard and disagg. */
  static class BaseRequest {
    final transient HttpRequest<?> http;
    final double longitude;
    final double latitude;
    final double vs30;

    public BaseRequest(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double vs30) {
      this.http = http;
      this.longitude = checkLongitude(longitude);
      this.latitude = checkLatitude(latitude);
      this.vs30 = checkInRange(Site.VS30_RANGE, Site.Key.VS30, vs30);
    }

    public double getLongitude() {
      return longitude;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getVs30() {
      return vs30;
    }
  }

  static final class Request extends BaseRequest {
    final boolean truncate;
    final boolean maxdir;
    final Set<Imt> imts;

    public Request(
        HttpRequest<?> http,
        double longitude,
        double latitude,
        double vs30,
        Set<Imt> imts,
        boolean truncate,
        boolean maxdir) {
      super(http, longitude, latitude, vs30);
      this.truncate = truncate;
      this.maxdir = maxdir;
      this.imts = imts.isEmpty()
          ? ServletUtil.model().config().hazard.imts
          : imts;
    }

    public boolean getTruncate() {
      return truncate;
    }

    public boolean getMaxdir() {
      return maxdir;
    }

    public Set<Imt> getImts() {
      return imts;
    }
  }

  static class Response {

    final Metadata metadata;
    final List<ImtCurves> hazardCurves;

    Response(Metadata metadata, List<ImtCurves> hazardCurves) {
      this.metadata = metadata;
      this.hazardCurves = hazardCurves;
    }

    public Metadata getMetadata() {
      return metadata;
    }

    public List<ImtCurves> getHazardCurves() {
      return hazardCurves;
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
      Map<Imt, Map<SourceType, MutableXySequence>> componentMaps;
      Map<Imt, MutableXySequence> totalMap;

      Builder timer(Stopwatch timer) {
        this.timer = timer;
        return this;
      }

      Builder request(Request request) {
        this.request = request;
        return this;
      }

      Builder hazard(Hazard hazard) {
        componentMaps = new EnumMap<>(Imt.class);
        totalMap = new EnumMap<>(Imt.class);
        var typeTotalMaps = curvesBySource(hazard);

        for (var imt : hazard.curves().keySet()) {

          /* Total curve for IMT. */
          XySequence.addToMap(imt, totalMap, hazard.curves().get(imt));

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

          hazards.add(new ImtCurves(imt, curves));
        }

        var server = ServletUtil.serverData(ServletUtil.THREAD_COUNT, timer);
        var response = new Response(
            new Response.Metadata(server),
            hazards);

        return response;
      }
    }

  }

  private static final class ImtCurves {
    final Parameter imt;
    final List<Curve> data;

    ImtCurves(Imt imt, List<Curve> data) {
      this.imt = new Parameter(ServletUtil.imtShortLabel(imt), imt.name());
      this.data = data;
    }

    public Parameter getImt() {
      return imt;
    }

    public List<Curve> getData() {
      return data;
    }
  }

  private static final class Curve {
    final String component;
    final XySequence values;

    Curve(String component, XySequence values) {
      this.component = component;
      this.values = values;
    }

    public String getComponent() {
      return component;
    }

    public XySequence getValues() {
      return values;
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
     * Consider moving to config.
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

  /* Read the 'imt' query values; can be comma-delimited. */
  static Set<Imt> readImts(HttpRequest<?> http) {
    return http.getParameters().getAll("imt").stream()
        .map(s -> s.split(","))
        .flatMap(Arrays::stream)
        .map(Imt::valueOf)
        .collect(toCollection(() -> EnumSet.noneOf(Imt.class)));
  }

  /* Read the 'out'put type query values; can be comma-delimited. */
  static Set<DataType> readDataTypes(HttpRequest<?> http) {
    return http.getParameters().getAll("out").stream()
        .map(s -> s.split(","))
        .flatMap(Arrays::stream)
        .map(DataType::valueOf)
        .collect(toCollection(() -> EnumSet.noneOf(DataType.class)));
  }
}

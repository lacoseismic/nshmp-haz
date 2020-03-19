package gov.usgs.earthquake.nshmp.www.meta;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.geo.Coordinates;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.www.meta.EnumParameter;
import gov.usgs.earthquake.nshmp.internal.www.meta.ParamType;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.ServletUtil.Timer;

/**
 * Service metadata, parameterization, and constraint strings, in JSON format.
 */
@SuppressWarnings("javadoc")
public final class Metadata {

  static final String NSHMP_HAZ_URL = "https://code.usgs.gov/ghsc/nshmp/nshmp-haz";

  /*
   * The hazard service needs to report the list of all possible IMTs supported
   * even though what can actually be supported is dependent on Edition and
   * Region. When the slash delimited service returns 'any', the same logic
   * applied on the client needs to be applied on the server to determine what
   * 'any' acutally means. See HazardService.buildRequest() methods. This field
   * currently set to the IMTs supported by Region.WUS which we know to be the
   * union of all periods currently supported by the models used.
   */
  private static final Set<Imt> HAZARD_IMTS = Region.WUS.imts;

  private static final String URL_PREFIX = "%s";

  public static final String HAZARD_USAGE = ServletUtil.GSON.toJson(
      new Default(
          "Compute hazard curve data at a location",
          URL_PREFIX + "/hazard/{edition}/{region}/{longitude}/{latitude}/{imt}/{vs30}",
          new HazardParameters()));

  public static final String DEAGG_USAGE = ServletUtil.GSON.toJson(
      new Deagg(
          "Deaggregate hazard at a location",
          URL_PREFIX +
              "/deagg/{edition}/{region}/{longitude}/{latitude}/{imt}/{vs30}/{returnPeriod}",
          new DeaggParameters()));

  @SuppressWarnings("unused")
  private static class Default {

    final String status;
    final String description;
    final String syntax;
    final Object server;
    final DefaultParameters parameters;

    private Default(
        String description,
        String syntax,
        DefaultParameters parameters) {
      this.status = Status.USAGE.toString();
      this.description = description;
      this.syntax = syntax;
      this.server = serverData(1, new Timer());
      this.parameters = parameters;
    }
  }

  public static Object serverData(int threads, Timer timer) {
    return new Server(threads, timer);
  }

  @SuppressWarnings("unused")
  private static class Server {

    final int threads;
    final String servlet;
    final String calc;

    @SerializedName("nshmp-haz")
    final Component nshmpHaz = NSHMP_HAZ_COMPONENT;

    Server(int threads, Timer timer) {
      this.threads = threads;
      this.servlet = timer.servletTime();
      this.calc = timer.calcTime();
    }

    static Component NSHMP_HAZ_COMPONENT = new Component(
        NSHMP_HAZ_URL,
        Versions.NSHMP_HAZ_VERSION);

    static final class Component {

      final String url;
      final String version;

      Component(String url, String version) {
        this.url = url;
        this.version = version;
      }
    }
  }

  @SuppressWarnings("unused")
  public static class DefaultParameters {

    // final EnumParameter<Edition> edition;
    // final EnumParameter<Region> region;
    final DoubleParameter longitude;
    final DoubleParameter latitude;

    public DefaultParameters() {

      // edition = new EnumParameter<>(
      // "Model edition",
      // ParamType.STRING,
      // EnumSet.allOf(Edition.class));
      //
      // region = new EnumParameter<>(
      // "Model region",
      // ParamType.STRING,
      // EnumSet.allOf(Region.class));

      longitude = new DoubleParameter(
          "Longitude (in decimal degrees)",
          ParamType.NUMBER,
          Coordinates.LON_RANGE.lowerEndpoint(),
          Coordinates.LON_RANGE.upperEndpoint());

      latitude = new DoubleParameter(
          "Latitude (in decimal degrees)",
          ParamType.NUMBER,
          Coordinates.LAT_RANGE.lowerEndpoint(),
          Coordinates.LAT_RANGE.upperEndpoint());
    }
  }

  @SuppressWarnings("unused")
  private static class HazardParameters extends DefaultParameters {

    final EnumParameter<Imt> imt;
    final EnumParameter<Vs30> vs30;

    HazardParameters() {

      imt = new EnumParameter<>(
          "Intensity measure type",
          ParamType.STRING,
          HAZARD_IMTS);

      vs30 = new EnumParameter<>(
          "Site soil (Vs30)",
          ParamType.STRING,
          EnumSet.allOf(Vs30.class));
    }
  }

  private static class Deagg extends Default {
    private Deagg(
        String description,
        String syntax,
        DeaggParameters parameters) {
      super(description, syntax, parameters);
    }
  }

  @SuppressWarnings("unused")
  private static class DeaggParameters extends HazardParameters {

    final DoubleParameter returnPeriod;

    DeaggParameters() {

      returnPeriod = new DoubleParameter(
          "Return period (in years)",
          ParamType.NUMBER,
          1.0,
          4000.0);
    }
  }

  public static String busyMessage(String url, long hits, long misses) {
    Busy busy = new Busy(url, hits, misses);
    return ServletUtil.GSON.toJson(busy);
  }

  static final String BUSY_MESSAGE = "Server busy. Please try again later. " +
      "We apologize for any inconvenience while we increase capacity.";

  private static class Busy {

    final String status = Status.BUSY.toString();
    final String request;
    final String message;

    private Busy(String request, long hits, long misses) {
      this.request = request;
      this.message = BUSY_MESSAGE + String.format(" (%s,%s)", hits, misses);
    }
  }

  public static String errorMessage(String url, Throwable e, boolean trace) {
    Error error = new Error(url, e, trace);
    return ServletUtil.GSON.toJson(error);
  }

  @SuppressWarnings("unused")
  private static class Error {

    final String status = Status.ERROR.toString();
    final String request;
    final String message;

    private Error(String request, Throwable e, boolean trace) {
      this.request = request;
      String message = e.getMessage() + " (see logs)";
      if (trace) {
        message += "\n" + Throwables.getStackTraceAsString(e);
      }
      this.message = message;
    }
  }

  public static Set<Imt> commonImts(Edition edition, Region region) {
    return Sets.intersection(edition.imts, region.imts);
  }

}

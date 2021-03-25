package gov.usgs.earthquake.nshmp.www.meta;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

import gov.usgs.earthquake.nshmp.geo.Coordinates;
import gov.usgs.earthquake.nshmp.www.services.ServletUtil;

/**
 * Service metadata, parameterization, and constraint strings, in JSON format.
 */
public final class Metadata {

  static final String NSHMP_HAZ_URL = "https://code.usgs.gov/ghsc/nshmp/nshmp-haz";

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
      this.server = serverData(1, Stopwatch.createStarted());
      this.parameters = parameters;
    }
  }

  public static Object serverData(int threads, Stopwatch timer) {
    return new Server(threads, timer);
  }

  private static class Server {

    final int threads;
    final String timer;
    final String version;

    Server(int threads, Stopwatch timer) {
      this.threads = threads;
      this.timer = timer.toString();
      this.version = "TODO where to get version?";
    }

    // static Component NSHMP_HAZ_COMPONENT = new Component(
    // NSHMP_HAZ_URL,
    // Versions.NSHMP_HAZ_VERSION);
    //
    // static final class Component {
    //
    // final String url;
    // final String version;
    //
    // Component(String url, String version) {
    // this.url = url;
    // this.version = version;
    // }
    // }
  }

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
          "Longitude",
          "°",
          Coordinates.LON_RANGE.lowerEndpoint(),
          Coordinates.LON_RANGE.upperEndpoint());

      latitude = new DoubleParameter(
          "Latitude",
          "°",
          Coordinates.LAT_RANGE.lowerEndpoint(),
          Coordinates.LAT_RANGE.upperEndpoint());
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

}

package gov.usgs.earthquake.nshmp.www.meta;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

import gov.usgs.earthquake.nshmp.geo.Coordinates;
import gov.usgs.earthquake.nshmp.www.ServletUtil;

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
      this.server = ServletUtil.serverData(1, Stopwatch.createStarted());
      this.parameters = parameters;
    }
  }

  public static class DefaultParameters {

    final DoubleParameter longitude;
    final DoubleParameter latitude;

    public DefaultParameters() {

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

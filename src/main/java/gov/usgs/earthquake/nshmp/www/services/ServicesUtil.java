package gov.usgs.earthquake.nshmp.www.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.Response;
import gov.usgs.earthquake.nshmp.www.WsUtils;
import gov.usgs.earthquake.nshmp.www.meta.Status;
import io.micronaut.http.HttpResponse;

public class ServicesUtil {

  public static HttpResponse<String> handleError(
      Throwable e,
      String name,
      String url) {
    var msg = e.getMessage() + " (see logs)";
    var svcResponse = new Response<>(Status.ERROR, name, url, msg, url);
    var gson = new GsonBuilder().setPrettyPrinting().create();
    var response = gson.toJson(svcResponse);
    e.printStackTrace();
    return HttpResponse.serverError(response);
  }

  static Hazard calcHazard(
      Function<HazardModel, CalcConfig> configFunction,
      Function<CalcConfig, Site> siteFunction) throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();
    CalcConfig config = configFunction.apply(model);
    Site site = siteFunction.apply(config);
    CompletableFuture<Hazard> future = calcHazard(model, config, site);
    return future.get();
  }

  @Deprecated
  static class ServiceQueryData implements ServiceQuery {

    public final Double longitude;
    public final Double latitude;

    ServiceQueryData(Double longitude, Double latitude) {
      this.longitude = longitude;
      this.latitude = latitude;
    }

    @Override
    public boolean isNull() {
      return longitude == null && latitude == null;
    }

    @Override
    public void checkValues() {
      WsUtils.checkValue(Key.LONGITUDE, longitude);
      WsUtils.checkValue(Key.LATITUDE, latitude);
    }
  }

  @Deprecated
  static class ServiceRequestData {

    public final double longitude;
    public final double latitude;

    public ServiceRequestData(ServiceQueryData query) {
      longitude = query.longitude;
      latitude = query.latitude;
    }
  }

  enum Key {
    EDITION,
    REGION,
    MODEL,
    VS30,
    LATITUDE,
    LONGITUDE,
    IMT,
    RETURNPERIOD,
    DISTANCE,
    FORMAT,
    TIMESPAN,
    BASIN;

    private String label;

    private Key() {
      label = name().toLowerCase();
    }

    @Override
    public String toString() {
      return label;
    }
  }

  @Deprecated
  private static interface ServiceQuery {
    boolean isNull();

    void checkValues();
  }

  private static CompletableFuture<Hazard> calcHazard(
      HazardModel model,
      CalcConfig config,
      Site site) {

    return CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(model, config, site, ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);
  }

}

package gov.usgs.earthquake.nshmp.www;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Hazard;
import gov.usgs.earthquake.nshmp.calc.HazardCalcs;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.model.HazardModel;

public class ServicesUtil {

  @Deprecated
  public static Hazard calcHazard(
      Function<HazardModel, CalcConfig> configFunction,
      Function<CalcConfig, Site> siteFunction) throws InterruptedException, ExecutionException {

    HazardModel model = ServletUtil.model();
    CalcConfig config = configFunction.apply(model);
    Site site = siteFunction.apply(config);
    CompletableFuture<Hazard> future = calcHazard(model, config, site);
    return future.get();
  }

  @Deprecated
  public static class ServiceQueryData implements ServiceQuery {

    public final Double longitude;
    public final Double latitude;

    public ServiceQueryData(Double longitude, Double latitude) {
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
  public static class ServiceRequestData {

    public final double longitude;
    public final double latitude;

    public ServiceRequestData(ServiceQueryData query) {
      longitude = query.longitude;
      latitude = query.latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    public double getLatitude() {
      return latitude;
    }
  }

  public enum Key {
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

  @Deprecated
  private static CompletableFuture<Hazard> calcHazard(
      HazardModel model,
      CalcConfig config,
      Site site) {

    return CompletableFuture.supplyAsync(
        () -> HazardCalcs.hazard(model, config, site, ServletUtil.CALC_EXECUTOR),
        ServletUtil.TASK_EXECUTOR);
  }

}

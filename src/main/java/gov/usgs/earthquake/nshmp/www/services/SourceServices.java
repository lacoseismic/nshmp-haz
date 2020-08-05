package gov.usgs.earthquake.nshmp.www.services;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import gov.usgs.earthquake.nshmp.calc.CalcConfig;
import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.eq.model.HazardModel;
import gov.usgs.earthquake.nshmp.gmm.Gmm;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet.UrlHelper;
import gov.usgs.earthquake.nshmp.internal.www.Response;
import gov.usgs.earthquake.nshmp.internal.www.meta.EnumParameter;
import gov.usgs.earthquake.nshmp.internal.www.meta.ParamType;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.www.meta.DoubleParameter;
import gov.usgs.earthquake.nshmp.www.meta.MetaUtil;
import gov.usgs.earthquake.nshmp.www.meta.Metadata;
import gov.usgs.earthquake.nshmp.www.meta.Region;

import io.micronaut.http.HttpResponse;

/**
 * Entry point for services related to source models. Current services:
 * <ul><li>/source/</li></ul>
 *
 * @author U.S. Geological Survey
 */
@SuppressWarnings("unused")
public class SourceServices {

  private static final String NAME = "Source Model";
  private static final String DESCRIPTION = "Installed source model listing";
  private static final String SERVICE_DESCRIPTION =
      "Utilities for querying earthquake source models";

  public static final Gson GSON;

  static {
    GSON = new GsonBuilder()
        .registerTypeAdapter(Imt.class, new MetaUtil.EnumSerializer<Imt>())
        .registerTypeAdapter(ParamType.class, new MetaUtil.ParamTypeSerializer())
        .registerTypeAdapter(Vs30.class, new MetaUtil.EnumSerializer<Vs30>())
        .registerTypeAdapter(Region.class, new RegionSerializer())
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()
        .create();
  }

  public static HttpResponse<String> handleDoGetUsage(UrlHelper urlHelper) {
    try {
      var response = new Response<>(
          Status.USAGE, NAME, urlHelper.url, new ResponseData(), urlHelper);
      var jsonString = GSON.toJson(response);
      return HttpResponse.ok(jsonString);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, urlHelper);
    }
  }

  /*
   * TODO service metadata should be in same package as services (why
   * ResponseData is currently public); rename meta package to
   */
  public static class ResponseData {
    final String description;
    final Object server;
    final Parameters parameters;

    public ResponseData() {
      this.description = "Installed source model listing";
      this.server = Metadata.serverData(ServletUtil.THREAD_COUNT, ServletUtil.timer());
      this.parameters = new Parameters();
    }
  }

  static class Parameters {
    List<SourceModel> models;
    EnumParameter<Region> region;
    DoubleParameter returnPeriod;
    EnumParameter<Vs30> vs30;

    Parameters() {
      models = ServletUtil.hazardModels().stream()
          .map(SourceModel::new)
          .collect(Collectors.toList());

      region = new EnumParameter<>(
          "Region",
          ParamType.STRING,
          EnumSet.allOf(Region.class));

      returnPeriod = new DoubleParameter(
          "Return period (in years)",
          ParamType.NUMBER,
          100.0,
          1e6);

      vs30 = new EnumParameter<Vs30>(
          "Vs30",
          ParamType.STRING,
          EnumSet.allOf(Vs30.class));
    }
  }

  public static class SourceModel {
    String display;
    Set<Gmm> gmms;
    CalcConfig config;

    private SourceModel(HazardModel model) {
      display = model.name();
      gmms = model.gmms();
      config = model.config();
    }

    public static List<SourceModel> getList() {
      return ServletUtil.hazardModels().stream()
          .map(SourceModel::new)
          .collect(Collectors.toList());
    }
  }

  enum Attributes {
    /* Source model service */
    MODEL,

    /* Serializing */
    ID,
    VALUE,
    DISPLAY,
    DISPLAYORDER,
    YEAR,
    PATH,
    REGION,
    IMT,
    VS30,
    SUPPORTS,
    MINLATITUDE,
    MINLONGITUDE,
    MAXLATITUDE,
    MAXLONGITUDE;

    /** Return upper case string */
    String toUpperCase() {
      return name().toUpperCase();
    }

    /** Return lower case string */
    String toLowerCase() {
      return name().toLowerCase();
    }
  }

  // TODO align with enum serializer if possible; consider service attribute
  // enum
  // TODO test removal of ui-min/max-lon/lat
  static final class RegionSerializer implements JsonSerializer<Region> {

    @Override
    public JsonElement serialize(Region region, Type typeOfSrc, JsonSerializationContext context) {
      var json = new JsonObject();

      json.addProperty(Attributes.VALUE.toLowerCase(), region.name());
      json.addProperty(Attributes.DISPLAY.toLowerCase(), region.toString());

      json.addProperty(Attributes.MINLATITUDE.toLowerCase(), region.minlatitude);
      json.addProperty(Attributes.MAXLATITUDE.toLowerCase(), region.maxlatitude);
      json.addProperty(Attributes.MINLONGITUDE.toLowerCase(), region.minlongitude);
      json.addProperty(Attributes.MAXLONGITUDE.toLowerCase(), region.maxlongitude);

      return json;
    }
  }

}

package gov.usgs.earthquake.nshmp.www.services;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet.UrlHelper;
import gov.usgs.earthquake.nshmp.internal.www.Response;
import gov.usgs.earthquake.nshmp.internal.www.meta.EnumParameter;
import gov.usgs.earthquake.nshmp.internal.www.meta.ParamType;
import gov.usgs.earthquake.nshmp.internal.www.meta.Status;
import gov.usgs.earthquake.nshmp.www.BaseModel;
import gov.usgs.earthquake.nshmp.www.Model;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.WsUtil;
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

  private static final Logger LOGGER = Logger.getLogger(SourceServices.class.getName());

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
      LOGGER.info(NAME + "- Request:\n" + urlHelper.url);
      var response = new Response<>(
          Status.USAGE, NAME, urlHelper.url, new ResponseData(), urlHelper);
      var jsonString = GSON.toJson(response);
      LOGGER.info(NAME + "- Response:\n" + jsonString);
      return HttpResponse.ok(jsonString);
    } catch (Exception e) {
      return WsUtil.handleError(e, NAME, LOGGER, urlHelper);
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
    SourceModel model;
    EnumParameter<Region> region;
    DoubleParameter returnPeriod;
    EnumParameter<Imt> imt;
    EnumParameter<Vs30> vs30;

    Parameters() {
      model = new SourceModel(ServletUtil.installedModel());

      region = new EnumParameter<>(
          "Region",
          ParamType.STRING,
          EnumSet.allOf(Region.class));

      returnPeriod = new DoubleParameter(
          "Return period (in years)",
          ParamType.NUMBER,
          100.0,
          1e6);

      imt = new EnumParameter<>(
          "Intensity measure type",
          ParamType.STRING,
          modelUnionImts());

      vs30 = new EnumParameter<>(
          "Site soil (Vs30)",
          ParamType.STRING,
          modelUnionVs30s());
    }
  }

  /* Union of IMTs across all models. */
  static Set<Imt> modelUnionImts() {
    return EnumSet.copyOf(ServletUtil.installedModel().models().stream()
        .flatMap(model -> model.imts.stream())
        .collect(Collectors.toSet()));
  }

  /* Union of Vs30s across all models. */
  static Set<Vs30> modelUnionVs30s() {
    return EnumSet.copyOf(ServletUtil.installedModel().models().stream()
        .flatMap(model -> model.vs30s.stream())
        .collect(Collectors.toSet()));
  }

  public static class SourceModel {
    String region;
    String display;
    String value;
    String year;
    List<BaseSourceModel> models;

    public SourceModel(Model model) {
      this.display = model.label();
      this.region = model.region().name();
      this.value = model.toString();
      this.year = model.year();
      models = model.models().stream()
          .map(m -> new BaseSourceModel(m))
          .collect(Collectors.toList());
    }
  }

  public static class BaseSourceModel {
    String year;
    String path;
    ModelConstraints constraints;

    BaseSourceModel(BaseModel model) {
      year = model.year;
      path = model.path;
      constraints = new ModelConstraints(model);
    }
  }

  private static class ModelConstraints {
    final List<String> imt;
    final List<String> vs30;

    ModelConstraints(BaseModel model) {
      this.imt = MetaUtil.enumsToNameList(model.imts);
      this.vs30 = MetaUtil.enumsToStringList(
          model.vs30s,
          vs30 -> vs30.name().substring(3));
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

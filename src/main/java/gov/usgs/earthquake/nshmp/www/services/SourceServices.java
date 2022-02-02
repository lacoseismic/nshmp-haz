package gov.usgs.earthquake.nshmp.www.services;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.gmm.Gmm;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.gmm.NehrpSiteClass;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.WsUtils;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Entry point for services related to source models. Current services:
 * <ul><li>/source/</li></ul>
 *
 * @author U.S. Geological Survey
 */
@Singleton
public class SourceServices {

  private static final String NAME = "Source Model";
  private static final String DESCRIPTION = "Installed source model listing";
  private static final String SERVICE_DESCRIPTION =
      "Utilities for querying earthquake source models";

  static final Logger LOG = LoggerFactory.getLogger(RateService.class);

  public static final Gson GSON;

  static {
    GSON = new GsonBuilder()
        .registerTypeAdapter(Imt.class, new WsUtils.EnumSerializer<Imt>())
        .disableHtmlEscaping()
        .serializeNulls()
        .setPrettyPrinting()
        .create();
  }

  public static HttpResponse<String> handleDoGetUsage(HttpRequest<?> request) {
    var url = request.getUri().getPath();
    try {
      var response = ResponseBody.usage()
          .name(NAME)
          .url(url)
          .request(url)
          .response(new ResponseData())
          .build();
      var json = GSON.toJson(response);
      return HttpResponse.ok(json);
    } catch (Exception e) {
      return ServletUtil.error(LOG, e, NAME, url);
    }
  }

  /*
   * TODO service metadata should be in same package as services (why
   * ResponseData is currently public); rename meta package to
   */
  public static class ResponseData {
    final String description;
    final Object server;
    // final Parameters parameters;

    public ResponseData() {
      this.description = "Installed source model listing";
      this.server = ServletUtil.serverData(
          ServletUtil.THREAD_COUNT,
          Stopwatch.createStarted());
      // this.parameters = new Parameters();
    }
  }

  // static class Parameters {
  // List<SourceModel> models;
  // DoubleParameter returnPeriod;
  // DoubleParameter vs30;
  //
  // Parameters() {
  // models = ServletUtil.hazardModels().stream()
  // .map(SourceModel::new)
  // .collect(Collectors.toList());
  //
  // returnPeriod = new DoubleParameter(
  // "Return period",
  // "years",
  // 100.0,
  // 1e6);
  //
  // vs30 = new DoubleParameter(
  // "Vs30",
  // "m/s",
  // 150,
  // 1500);
  // }
  // }

  public static class SourceModel {
    String name;
    Set<Gmm> gmms;
    Map<NehrpSiteClass, Double> siteClasses;
    List<Parameter> imts;

    public SourceModel(HazardModel model) {
      name = model.name();
      gmms = model.gmms();
      siteClasses = model.siteClasses();
      imts = model.gmms().stream()
          .map(Gmm::supportedImts)
          .flatMap(Set::stream)
          .distinct()
          .sorted()
          .map(imt -> new Parameter(ServletUtil.imtShortLabel(imt), imt.name()))
          .collect(toList());
    }

    // public static List<SourceModel> getList() {
    // return ServletUtil.hazardModels().stream()
    // .map(SourceModel::new)
    // .collect(Collectors.toList());
    // }
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
}

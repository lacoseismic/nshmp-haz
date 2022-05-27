package gov.usgs.earthquake.nshmp.www.source;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.usgs.earthquake.nshmp.gmm.Gmm;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.gmm.NehrpSiteClass;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.HazVersion;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ResponseMetadata;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.WsUtils;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;
import gov.usgs.earthquake.nshmp.www.services.RateService;
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

  static HttpResponse<String> handleDoGetUsage(HttpRequest<?> request) {
    var url = request.getUri().getPath();
    try {
      var response = ResponseBody.usage()
          .name(NAME)
          .url(url)
          .metadata(new ResponseMetadata(HazVersion.appVersions()))
          .request(url)
          .response(new ResponseData())
          .build();
      var json = GSON.toJson(response);
      return HttpResponse.ok(json);
    } catch (Exception e) {
      return ServletUtil.error(LOG, e, NAME, url);
    }
  }

  static class ResponseData {
    final String description;
    final SourceModel model;

    public ResponseData() {
      description = "Installed source model listing";
      model = new SourceModel(ServletUtil.model());
    }

    public String getDescription() {
      return description;
    }

    public SourceModel getSourceModel() {
      return model;
    }
  }

  public static class SourceModel {
    final String name;
    final Set<Gmm> gmms;
    final Map<NehrpSiteClass, Double> siteClasses;
    final List<Parameter> imts;

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

    public String getName() {
      return name;
    }

    public Set<Gmm> getGmms() {
      return gmms;
    }

    public Map<NehrpSiteClass, Double> getSiteClasses() {
      return siteClasses;
    }

    public List<Parameter> getImts() {
      return imts;
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
}

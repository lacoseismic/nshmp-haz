package gov.usgs.earthquake.nshmp.www.source;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.earthquake.nshmp.gmm.Gmm;
import gov.usgs.earthquake.nshmp.gmm.NehrpSiteClass;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.HazVersion;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ResponseMetadata;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.meta.Parameter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Source model service.
 *
 * @author U.S. Geological Survey
 */
@Singleton
public class SourceService {

  static final String NAME = "Model Contents";
  static final Logger LOG = LoggerFactory.getLogger(SourceService.class);

  static HttpResponse<String> getMetadata(HttpRequest<?> request) {
    var url = request.getUri().toString();
    var response = ResponseBody.usage()
        .name(NAME)
        .url(url)
        .metadata(new ResponseMetadata(HazVersion.appVersions()))
        .request(url)
        .response(new ResponseData())
        .build();
    // TODO check other services for url) and
    // request() passing in the same url obj
    var json = ServletUtil.GSON2.toJson(response);
    return HttpResponse.ok(json);
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
    final List<Double> bounds;

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
      bounds = DoubleStream.of(model.bounds().toArray()).boxed().collect(toList());
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

    public List<Double> getBounds() {
      return bounds;
    }
  }
}

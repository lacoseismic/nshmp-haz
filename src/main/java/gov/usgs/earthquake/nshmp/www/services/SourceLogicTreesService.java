package gov.usgs.earthquake.nshmp.www.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.earthquake.nshmp.model.Models;
import gov.usgs.earthquake.nshmp.www.HazVersion;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ResponseMetadata;
import gov.usgs.earthquake.nshmp.www.ServletUtil;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;

/**
 * Source model tree handler for {@link SourceLogicTreesController}
 *
 * @author U.S. Geological Survey
 */
@Singleton
public class SourceLogicTreesService {

  static final Logger LOG = LoggerFactory.getLogger(SourceLogicTreesService.class);

  private static final String NAME = "Source Logic Trees";

  /** SourceLogicTreesController.doGetMetadata() handler */
  public static HttpResponse<String> handleDoGetMetadata(HttpRequest<?> request) {
    var url = request.getUri().getPath();

    try {
      var trees = Models.trees(ServletUtil.model());
      var response = ResponseBody.success()
          .name(NAME)
          .url(url)
          .metadata(new ResponseMetadata(HazVersion.appVersions()))
          .request(url)
          .response(trees)
          .build();
      return HttpResponse.ok(ServletUtil.GSON.toJson(response));
    } catch (Exception e) {
      return ServletUtil.error(LOG, e, NAME, url);
    }
  }

  /** SourceLogicTreesController.doGetTrees() handler */
  public static HttpResponse<String> handleDoGetTrees(HttpRequest<?> request, Integer id) {
    var url = request.getUri().getPath();

    try {
      var tree = Models.tree(ServletUtil.model(), id);
      var requestData = new RequestData(id);
      var response = ResponseBody.success()
          .name(NAME)
          .url(url)
          .metadata(new ResponseMetadata(HazVersion.appVersions()))
          .request(requestData)
          .response(tree)
          .build();
      return HttpResponse.ok(ServletUtil.GSON.toJson(response));
    } catch (Exception e) {
      return ServletUtil.error(LOG, e, NAME, url);
    }
  }

  static class RequestData {
    final int id;

    RequestData(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }
  }
}

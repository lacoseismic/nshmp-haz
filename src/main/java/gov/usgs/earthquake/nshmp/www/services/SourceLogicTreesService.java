package gov.usgs.earthquake.nshmp.www.services;

import gov.usgs.earthquake.nshmp.model.Models;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServicesUtil;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.SourceLogicTreesController;

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

  private static final String NAME = "Source Logic Trees";

  /** SourceLogicTreesController.doGetMetadata() handler */
  public static HttpResponse<String> handleDoGetMetadata(HttpRequest<?> request) {
    var url = request.getUri().getPath();

    try {
      var trees = Models.trees(ServletUtil.model());
      var response = ResponseBody.success()
          .name(NAME)
          .url(url)
          .request(url)
          .response(trees)
          .build();
      return HttpResponse.ok(ServletUtil.GSON.toJson(response));
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, url);
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
          .request(requestData)
          .response(tree)
          .build();
      return HttpResponse.ok(ServletUtil.GSON.toJson(response));
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, url);
    }
  }

  private static class RequestData {
    int id;

    RequestData(int id) {
      this.id = id;
    }
  }
}

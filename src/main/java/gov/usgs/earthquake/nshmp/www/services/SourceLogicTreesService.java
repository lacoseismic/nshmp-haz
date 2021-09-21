package gov.usgs.earthquake.nshmp.www.services;

import javax.inject.Singleton;

import gov.usgs.earthquake.nshmp.model.Models;
import gov.usgs.earthquake.nshmp.www.Response;
import gov.usgs.earthquake.nshmp.www.SourceLogicTreesController;
import gov.usgs.earthquake.nshmp.www.meta.Status;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;

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
      var response = new Response<>(Status.SUCCESS, NAME, url, trees, url);
      return HttpResponse.ok(ServletUtil.GSON.toJson(response));
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, url);
    }
  }

  /** SourceLogicTreesController.doGetTrees() handler */
  public static HttpResponse<String> handleDoGetTrees(HttpRequest<?> request, Integer id) {
    var url = request.getUri().getPath();

    try {
      var tree = ServletUtil.model().tree(id);
      var requestData = new RequestData(id);

      if (tree.isEmpty()) {
        throw new RuntimeException(String.format("Id [%s] not found", id));
      }

      var response = new Response<>(Status.SUCCESS, NAME, requestData, tree.get(), url);
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

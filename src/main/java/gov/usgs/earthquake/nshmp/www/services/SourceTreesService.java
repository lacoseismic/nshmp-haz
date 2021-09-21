package gov.usgs.earthquake.nshmp.www.services;

import javax.inject.Singleton;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;

@Singleton
public class SourceTreesService {

  private static final String NAME = "Source Model Trees";

  /** SourceTreesController.doGetMetadata() handler */
  public static HttpResponse<String> handleDoGetMetadata(HttpRequest<?> request) {
    var url = request.getUri().getPath();

    try {
      var model = ServletUtil.model();
      var trees = model.trees();
      return HttpResponse.ok(ServletUtil.GSON.toJson(trees.entries()));
    } catch (Exception e) {
      return ServicesUtil.handleError(e, NAME, url);
    }
  }

  static class RequestMetadata {

  }

}

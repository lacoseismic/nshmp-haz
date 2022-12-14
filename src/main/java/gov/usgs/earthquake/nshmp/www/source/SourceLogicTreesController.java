package gov.usgs.earthquake.nshmp.www.source;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.source.SourceLogicTreesService.Metadata;
import gov.usgs.earthquake.nshmp.www.source.SourceLogicTreesService.RequestData;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

/**
 * Micronaut web service controller for the source logic trees in the current
 * installed model.
 *
 * <p>See src/main/resources/application.yml nshmp-haz.model-path for installed
 * model.
 *
 * <p>To run the Micronaut jar file with a model: java -jar
 * path/to/nshmp-haz.jar --model=<path/to/model>
 *
 * @author U.S. Geological Survey
 */
@Tag(
    name = SourceLogicTreesService.NAME,
    description = "USGS NSHM source model logic tree service")
@Controller("/trees")
public class SourceLogicTreesController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Source model logic tree listing",
      description = "Returns the ID's of logic trees in the model",
      operationId = "source-tree-metadata")
  @ApiResponse(
      description = "Source logic tree metadata",
      responseCode = "200",
      content = @Content(
          schema = @Schema(
              implementation = MetadataResponse.class)))
  @Get
  public HttpResponse<String> doGetMetadata(HttpRequest<?> http) {
    try {
      return SourceLogicTreesService.getMetadata(http);
    } catch (Exception e) {
      return ServletUtil.error(
          SourceLogicTreesService.LOG, e,
          SourceLogicTreesService.NAME,
          http.getUri().toString());
    }
  }

  /**
   * @param id Source tree id
   */
  @Operation(
      summary = "Get a source model MFD logic tree",
      description = "Returns the logic tree of MFDs for the supplied ID",
      operationId = "source-tree-mfds")
  @ApiResponse(
      description = "NSHM source logic tree",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = TreeResponse.class)))
  @Get(uri = "/{id}")
  public HttpResponse<String> doGetTree(HttpRequest<?> http, @PathVariable int id) {
    try {
      return SourceLogicTreesService.getTree(http, id);
    } catch (Exception e) {
      return ServletUtil.error(
          SourceLogicTreesService.LOG, e,
          SourceLogicTreesService.NAME,
          http.getUri().toString());
    }
  }

  // Swagger schema
  private static class MetadataResponse extends ResponseBody<String, Metadata> {}

  // Swagger schema
  private static class TreeResponse extends ResponseBody<RequestData, Object> {}
}

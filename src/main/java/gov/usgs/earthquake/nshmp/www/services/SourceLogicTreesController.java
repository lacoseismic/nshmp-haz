package gov.usgs.earthquake.nshmp.www.services;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.services.SourceLogicTreesService.RequestData;

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
 * Micronaut controller for NSHM source logic trees.
 *
 * @see SourceLogicTreesService
 * @author U.S. Geological Survey
 */
@Tag(
    name = "Source Logic Trees",
    description = "NSHM source logic trees service")
@Controller("/trees")
public class SourceLogicTreesController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      description = "Returns the tectonic setting to source logic trees in the NSHM",
      operationId = "trees_doGetMetadata",
      summary = "Hazard model source logic trees")
  @ApiResponse(
      description = "Source logic trees metadata",
      responseCode = "200",
      content = @Content(
          schema = @Schema(
              implementation = MetadataResponse.class)))
  @Get
  public HttpResponse<String> doGetMetadata(HttpRequest<?> request) {
    return SourceLogicTreesService.handleDoGetMetadata(request);
  }

  /**
   * @param id Source tree id
   */
  @Operation(
      description = "Returns the source logic tree for an id",
      operationId = "trees_goGetTrees",
      summary = "Get NSHM source logic tree")
  @ApiResponse(
      description = "NSHM source logic tree",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = TreeResponse.class)))
  @Get(uri = "/{id}")
  public HttpResponse<String> doGetTrees(HttpRequest<?> request, @PathVariable int id) {
    return SourceLogicTreesService.handleDoGetTrees(request, id);
  }

  // For Swagger schemas
  private static class MetadataResponse extends ResponseBody<String, Object> {}

  // For Swagger schemas
  private static class TreeResponse extends ResponseBody<RequestData, Object> {}
}

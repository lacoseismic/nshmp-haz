package gov.usgs.earthquake.nshmp.www;

import gov.usgs.earthquake.nshmp.www.services.SourceLogicTreesService;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
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
      responseCode = "200")
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
      responseCode = "200")
  @Get(uri = "/{id}")
  public HttpResponse<String> doGetTrees(HttpRequest<?> request, @PathVariable int id) {
    return SourceLogicTreesService.handleDoGetTrees(request, id);
  }
}

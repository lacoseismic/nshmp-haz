package gov.usgs.earthquake.nshmp.www.source;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.source.SourceServices.ResponseData;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

/**
 * Micronaut web service controller to return metadata on the current installed
 * model.
 *
 * <p>See src/main/resources/application.yml nshmp-haz.model-path for installed
 * model.
 *
 * <p>To run the Micronaut jar file with a model: java -jar
 * path/to/nshmp-haz.jar --model=<path/to/model>
 *
 * @author U.S. Geological Survey
 */
@Tag(name = "Source Model")
@Controller("/source")
public class SourceController {

  @Inject
  private NshmpMicronautServlet servlet;

  /**
   * GET method to return the source model usage
   *
   * @param request The HTTP request
   */
  @Operation(
      summary = "Returns the metadata about the current installed NSHM",
      description = "Returns the install National Hazard Model with supported:\n" +
          "* Intensity measure types (IMT)\n * VS30\n * Region bounds\n * Return period",
      operationId = "source_doGetUsage")
  @ApiResponse(
      description = "Installed source model",
      responseCode = "200",
      content = @Content(
          schema = @Schema(
              implementation = MetadataResponse.class)))
  @Get(produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetUsage(HttpRequest<?> request) {
    return SourceServices.handleDoGetUsage(request);
  }

  // For Swagger schemas
  private static class MetadataResponse extends ResponseBody<String, ResponseData> {}
}

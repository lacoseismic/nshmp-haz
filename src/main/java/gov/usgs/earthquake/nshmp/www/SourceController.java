package gov.usgs.earthquake.nshmp.www;

import javax.inject.Inject;

import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.services.SourceServices;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Source model service to return the current installed model.
 * 
 * <p> See src/main/resources/applicaiton.yml nshmp-haz.installed-model for
 * default model used
 * 
 * <p> To run the Micronaut jar file with a model: java -jar
 * path/to/nshmp-haz-v2-all.jar -model=<{@code Model}>
 * 
 * 
 * @author U.S. Geological Survey
 */
@Tag(name = "Source Model")
@Controller("/source")
public class SourceController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Value("${nshmp-haz.installed-model}")
  private Model model;

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
      responseCode = "200")
  @Get(produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetUsage(HttpRequest<?> request) {
    var urlHelper = servlet.urlHelper(request);
    return SourceServices.handleDoGetUsage(model, urlHelper);
  }

}

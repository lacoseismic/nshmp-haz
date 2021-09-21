package gov.usgs.earthquake.nshmp.www;

import javax.inject.Inject;

import gov.usgs.earthquake.nshmp.www.services.SourceTreesService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
  name = "Source Model Trees",
  description = ""
)
@Controller("/trees")
public class SourceTreesController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation()
  @ApiResponse()
  @Get
  public HttpResponse<String> doGetMetadata(HttpRequest<?> request) {
    return SourceTreesService.handleDoGetMetadata(request);
  }

}

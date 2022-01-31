package gov.usgs.earthquake.nshmp.www;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.google.common.io.Resources;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

/**
 * Expose OpenAPI YAML file.
 *
 * @author U.S. Geological Survey
 */
@Tag(
    name = "Swagger",
    description = "Swagger OpenAPI YAML")
@Hidden
@Controller("/swagger")
public class SwaggerController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Get(produces = MediaType.TEXT_EVENT_STREAM)
  public HttpResponse<String> doGet(HttpRequest<?> request) {
    try {
      var url = Resources.getResource("META-INF/swagger/nshmp-haz.yml");
      var yml = Resources.readLines(url, StandardCharsets.UTF_8)
          .stream()
          .collect(Collectors.joining("\n"));
      return HttpResponse.ok(yml);
    } catch (Exception e) {
      return ServicesUtil.handleError(e, "Swagger", request.getUri().getPath());
    }
  }
}

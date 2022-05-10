package gov.usgs.earthquake.nshmp.www.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.SwaggerUtils;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
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
      var model = ServletUtil.model();
      var openApi = getOpenAPI(request, model);
      return HttpResponse.ok(Yaml.pretty(openApi));
    } catch (Exception e) {
      return ServletUtil.error(
          LoggerFactory.getLogger("Swagger"),
          e, "Swagger", request.getUri().toString());
    }
  }

  private OpenAPI getOpenAPI(
      HttpRequest<?> request,
      HazardModel model) {
    var openApi = new OpenAPIV3Parser().read("META-INF/swagger/nshmp-haz.yml");
    // TODO: Get min and max boundaries
    // SwaggerUtils.addLocationBounds
    var components = openApi.getComponents();
    var schemas = components.getSchemas();
    SwaggerUtils.siteClassSchema(schemas, List.copyOf(model.siteClasses().keySet()));
    SwaggerUtils.imtSchema(schemas,
        model.config().hazard.imts.stream().collect(Collectors.toList()));
    openApi.servers(null);

    openApi.getInfo().setTitle(model.name() + " Web Services");
    openApi.getInfo().setDescription(
        "National Seismic Hazard Model (NSHM) hazard calculations and queries for the " +
            model.name() + " hazard model.");

    return openApi;
  }
}

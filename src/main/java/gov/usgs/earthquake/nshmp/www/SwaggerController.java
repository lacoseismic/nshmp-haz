package gov.usgs.earthquake.nshmp.www;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.model.HazardModel;

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
    var bounds = new Bounds(model.bounds());
    SwaggerUtils.addLocationBounds(openApi, bounds.min, bounds.max);
    var components = openApi.getComponents();
    var schemas = components.getSchemas();
    SwaggerUtils.siteClassSchema(schemas, List.copyOf(model.siteClasses().keySet()));
    SwaggerUtils.imtSchema(schemas, List.copyOf(model.config().hazard.imts));
    openApi.servers(null);

    openApi.getInfo().setTitle(model.name() + " Web Services");
    openApi.getInfo().setDescription(
        "National Seismic Hazard Model (NSHM) hazard calculations and queries for the " +
            model.name() + " hazard model.");

    return openApi;
  }

  private static class Bounds {
    final Location min;
    final Location max;

    Bounds(Map<String, Double> bounds) {
      var log = Logger.getAnonymousLogger();

      bounds.entrySet().forEach(entry -> {
        log.info(entry.getKey() + ", " + entry.getValue());
      });
      min = Location.create(bounds.get("min-longitude"), bounds.get("min-latitude"));
      max = Location.create(bounds.get("max-longitude"), bounds.get("max-latitude"));
    }
  }
}

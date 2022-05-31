package gov.usgs.earthquake.nshmp.www.source;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.source.RateService.Metadata;
import gov.usgs.earthquake.nshmp.www.source.RateService.Request;
import gov.usgs.earthquake.nshmp.www.source.RateService.Response;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
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
 * Micronaut web service controller for rate calcuations.
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
    name = "Earthquake Rate Calculation",
    description = "USGS NSHM earthquake rate calculation service")
@Controller("/rate")
public class RateController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Earthquake rate calculation model and service metadata",
      description = "Returns details of the installed model and service request parameters",
      operationId = "rate-metadata")
  @ApiResponse(
      description = "Rate service metadata",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = RateMetadata.class)))
  @Get(produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetRateMetadata(HttpRequest<?> http) {
    try {
      return RateService.getRateMetadata(http);
    } catch (Exception e) {
      return ServletUtil.error(
          RateService.LOG, e,
          RateService.NAME_RATE,
          http.getUri().toString());
    }
  }

  /**
   * @param longitude Longitude in decimal degrees in the range
   * @param latitude Latitude in decimal degrees in the range
   * @param distance Cutoff distance in the range [0.01, 1000] km.
   */
  @Operation(
      summary = "Compute annual earthquake rates",
      description = "Compute incremental annual earthquake rates at a location",
      operationId = "rate-calc")
  @ApiResponse(
      description = "Earthquake annual rate calculation response",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = RateResponse.class)))
  @Get(
      uri = "/{longitude}/{latitude}/{distance}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetRate(
      HttpRequest<?> http,
      @PathVariable double longitude,
      @PathVariable double latitude,
      @Schema(
          minimum = "0.01",
          maximum = "1000") @PathVariable double distance) {
    try {
      RateService.Request request = new RateService.Request(
          http, longitude, latitude, distance);
      return RateService.getRate(request);
    } catch (Exception e) {
      return ServletUtil.error(
          RateService.LOG, e,
          RateService.NAME_RATE,
          http.getUri().toString());
    }
  }

  // Swagger schema
  private static class RateResponse extends ResponseBody<Request, Response> {}

  // Swagger schema
  private static class RateMetadata extends ResponseBody<String, Metadata> {};
}

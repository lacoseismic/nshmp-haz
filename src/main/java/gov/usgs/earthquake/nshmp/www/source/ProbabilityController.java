package gov.usgs.earthquake.nshmp.www.source;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.source.RateService.ProbMetadata;
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
 * Micronaut web service controller for probability calcuations.
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
    name = RateService.NAME_PROBABILITY,
    description = "USGS NSHM earthquake probability calculation service")
@Controller("/probability")
public class ProbabilityController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Earthquake probability calculation model and service metadata",
      description = "Returns details of the installed model and service request parameters",
      operationId = "probability-metadata")
  @ApiResponse(
      description = "Probability service metadata",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = ProbabilityMetadata.class)))
  @Get(produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetProbabilityMetadata(HttpRequest<?> http) {
    try {
      return RateService.getProbMetadata(http);
    } catch (Exception e) {
      return ServletUtil.error(
          RateService.LOG, e,
          RateService.NAME_PROBABILITY,
          http.getUri().toString());
    }
  }

  /**
   * @param longitude Longitude in decimal degrees in the range
   * @param latitude Latitude in decimal degrees in the range
   * @param distance Cutoff distance in the range [0.01..1000] km.
   * @param timespan Forecast time span in the range [1..10000] years.
   */
  @Operation(
      summary = "Compute earthquake probabilities",
      description = "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
      operationId = "probability-calc")
  @ApiResponse(
      description = "Earthquake probability calculation response",
      responseCode = "200",
      content = @Content(
          schema = @Schema(
              implementation = ProbabilityResponse.class)))
  @Get(
      uri = "/{longitude}/{latitude}/{distance}/{timespan}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetProbability(
      HttpRequest<?> http,
      @PathVariable double longitude,
      @PathVariable double latitude,
      @Schema(
          minimum = "0.01",
          maximum = "1000") @PathVariable double distance,
      @Schema(
          minimum = "1",
          maximum = "10000") @PathVariable double timespan) {
    try {
      RateService.ProbRequest request = new RateService.ProbRequest(
          http, longitude, latitude, distance, timespan);
      return RateService.getProbability(request);
    } catch (Exception e) {
      return ServletUtil.error(
          RateService.LOG, e,
          RateService.NAME_PROBABILITY,
          http.getUri().toString());
    }
  }

  // Swagger schema
  private static class ProbabilityResponse extends ResponseBody<Request, Response> {}

  // Swagger schema
  private static class ProbabilityMetadata extends ResponseBody<String, ProbMetadata> {};
}

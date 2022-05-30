package gov.usgs.earthquake.nshmp.www.source;

import java.util.Optional;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.source.RateService.ProbabilityParameters;
import gov.usgs.earthquake.nshmp.www.source.RateService.Query;
import gov.usgs.earthquake.nshmp.www.source.RateService.RateParameters;
import gov.usgs.earthquake.nshmp.www.source.RateService.RequestData;
import gov.usgs.earthquake.nshmp.www.source.RateService.ResponseData;
import gov.usgs.earthquake.nshmp.www.source.RateService.Service;
import gov.usgs.earthquake.nshmp.www.source.RateService.Usage;
import io.micronaut.core.annotation.Nullable;
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
 * Micronaut web service controller for rate and probability calcuations.
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
    name = "Rate Calculation",
    description = "USGS NSHM hazard calculation service")
@Controller("/")
public class RateController {

  @Inject
  private NshmpMicronautServlet servlet;

  /**
   * GET method to compute annual-rate, slash based
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees)
   * @param latitude Latitude (in decimal degrees)
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   */
  @Operation(
      summary = "Compute earthquake annual-rates",
      description = "Compute incremental earthquake annual-rates at a location",
      operationId = "rate_doGetRateSlash",
      tags = { "Rate Service" })
  @ApiResponse(
      description = "Earthquake annual-rates calculation response",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = CalcResponse.class)))
  @Get(
      uri = "/rate{/longitude}{/latitude}{/distance}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetRateSlash(
      HttpRequest<?> request,
      @Schema(
          required = true) @PathVariable @Nullable Double longitude,
      @Schema(
          required = true) @PathVariable @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @PathVariable @Nullable Double distance) {
    var service = Service.RATE;
    var query = new Query(service, longitude, latitude, distance, Optional.empty());
    return RateService.handleDoGetCalc(request, query);
  }

  /**
   * GET method to compute probability, slash based
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees)
   * @param latitude Latitude (in decimal degrees)
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   * @param timespan Forecast time span (in years) ([1, 10000])
   */
  @Operation(
      summary = "Compute earthquake probabilities",
      description = "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
      operationId = "probability_doGetProbabilitySlash",
      tags = { "Probability Service" })
  @ApiResponse(
      description = "Earthquake probabilities calculation response",
      responseCode = "200",
      content = @Content(
          schema = @Schema(
              implementation = CalcResponse.class)))
  @Get(
      uri = "/probability{/longitude}{/latitude}{/distance}{/timespan}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetProbabilitySlash(
      HttpRequest<?> request,
      @Schema(
          required = true) @PathVariable @Nullable Double longitude,
      @Schema(
          required = true) @PathVariable @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @PathVariable @Nullable Double distance,
      @Schema(
          required = true,
          minimum = "1",
          maximum = "10000") @PathVariable @Nullable Double timespan) {
    var service = Service.PROBABILITY;
    var query = new Query(service, longitude, latitude, distance, Optional.ofNullable(timespan));
    return RateService.handleDoGetCalc(request, query);
  }

  // Swagger schemas
  private static class CalcResponse extends ResponseBody<RequestData, ResponseData> {}

  private static class RateMetadataResponse extends ResponseBody<String, Usage<RateParameters>> {};

  private static class ProbMetadataResponse extends
      ResponseBody<String, Usage<ProbabilityParameters>> {};
}

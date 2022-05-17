package gov.usgs.earthquake.nshmp.www.services;

import java.util.Optional;

import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.services.RateService.ProbabilityParameters;
import gov.usgs.earthquake.nshmp.www.services.RateService.Query;
import gov.usgs.earthquake.nshmp.www.services.RateService.RateParameters;
import gov.usgs.earthquake.nshmp.www.services.RateService.RequestData;
import gov.usgs.earthquake.nshmp.www.services.RateService.ResponseData;
import gov.usgs.earthquake.nshmp.www.services.RateService.Service;
import gov.usgs.earthquake.nshmp.www.services.RateService.Usage;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;

/**
 * Micronaut controller for rate and probability services.
 *
 * @see RateService
 *
 * @author U.S. Geological Survey
 */
@Controller("/")
public class RateController {

  @Inject
  private NshmpMicronautServlet servlet;

  /**
   * GET method to compute annual-rate, query based.
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees)
   * @param latitude Latitude (in decimal degrees)
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   */
  @Operation(
      summary = "Compute earthquake annual-rates",
      description = "Compute incremental earthquake annual-rates at a location",
      operationId = "rate_doGetRate",
      tags = { "Rate Service" })
  @ApiResponse(
      description = "Earthquake annual-rates service metadata",
      responseCode = "20x",
      content = {
          @Content(
              schema = @Schema(implementation = RateMetadataResponse.class))
      })
  @ApiResponse(
      description = "Earthquake annual-rates calculation response",
      responseCode = "200",
      content = {
          @Content(
              schema = @Schema(implementation = CalcResponse.class))
      })
  @Get(
      uri = "/rate{?longitude,latitude,distance}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetRate(
      HttpRequest<?> request,
      @Schema(
          required = true) @QueryValue @Nullable Double longitude,
      @Schema(
          required = true) @QueryValue @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @QueryValue @Nullable Double distance) {
    var service = Service.RATE;
    var query = new Query(service, longitude, latitude, distance, Optional.empty());
    return RateService.handleDoGetCalc(request, query);
  }

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
   * GET method to compute probability, query based.
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees)
   * @param latitude Latitude (in decimal degrees)
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   * @param timespan Forcast time span (in years) ([1, 10000])
   */
  @Operation(
      summary = "Compute earthquake probabilities",
      description = "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
      operationId = "probability_doGetProbability",
      tags = { "Probability Service" })
  @ApiResponse(
      description = "Earthquake probabilities service metadata",
      responseCode = "20x",
      content = {
          @Content(
              schema = @Schema(implementation = ProbMetadataResponse.class))
      })
  @ApiResponse(
      description = "Earthquake probabilities calculation response",
      responseCode = "200",
      content = {
          @Content(
              schema = @Schema(
                  implementation = CalcResponse.class))
      })
  @Get(
      uri = "/probability{?longitude,latitude,distance,timespan}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetProbability(
      HttpRequest<?> request,
      @Schema(
          required = true) @QueryValue @Nullable Double longitude,
      @Schema(
          required = true) @QueryValue @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @QueryValue @Nullable Double distance,
      @Schema(
          required = true,
          minimum = "1",
          maximum = "10000") @QueryValue @Nullable Double timespan) {
    var service = Service.PROBABILITY;
    var query = new Query(service, longitude, latitude, distance, Optional.ofNullable(timespan));
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

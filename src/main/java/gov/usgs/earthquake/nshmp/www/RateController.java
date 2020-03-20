package gov.usgs.earthquake.nshmp.www;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;

import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.services.RateService;
import gov.usgs.earthquake.nshmp.www.services.RateService.Query;
import gov.usgs.earthquake.nshmp.www.services.RateService.Service;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
   * GET method to return the usage information on the rate service.
   * 
   * @param request The HTTP request
   */
  @Operation(
      summary = "Returns the earthquake rate service usage",
      description = "Returns the supported:\n * Cutoff distance\n * Longitude\n * Latitude",
      operationId = "rate_doGetUsageRate",
      tags = { "Rate Service" })
  @ApiResponse(
      description = "Earthquake rate usage",
      responseCode = "200")
  @Get(uri = "/rate/usage", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetUsageRate(HttpRequest<?> request) {
    var urlHelper = servlet.urlHelper(request);
    return RateService.handleDoGetUsage(Service.RATE, urlHelper);
  }

  /**
   * GET method to compute annual-rate, query based.
   * 
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   */
  @Operation(
      summary = "Compute earthquake annual-rates",
      description = "Compute incremental earthquake annual-rates at a location",
      operationId = "rate_doGetRate",
      tags = { "Rate Service" })
  @ApiResponse(
      description = "Earthquake annual-rates",
      responseCode = "200")
  @Get(
      uri = "/rate{?longitude,latitude,distance}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetRate(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @QueryValue @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @QueryValue @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @QueryValue @Nullable Double distance) {
    var service = Service.RATE;
    var urlHelper = servlet.urlHelper(request);
    var query = new Query(service, longitude, latitude, distance, Optional.empty());
    return RateService.handleDoGetCalc(query, urlHelper);
  }

  /**
   * GET method to compute annual-rate, slash based
   * 
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   */
  @Operation(
      summary = "Compute earthquake annual-rates",
      description = "Compute incremental earthquake annual-rates at a location",
      operationId = "rate_doGetRateSlash",
      tags = { "Rate Service" })
  @ApiResponse(
      description = "Earthquake annual-rates",
      responseCode = "200")
  @Get(
      uri = "/rate{/longitude}{/latitude}{/distance}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetRateSlash(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @PathVariable @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @PathVariable @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @PathVariable @Nullable Double distance) {
    var service = Service.RATE;
    var urlHelper = servlet.urlHelper(request);
    var query = new Query(service, longitude, latitude, distance, Optional.empty());
    return RateService.handleDoGetCalc(query, urlHelper);
  }

  /**
   * GET method to return the usage information on the probability service.
   * 
   * @param request The HTTP request
   */
  @Operation(
      summary = "Returns the earthquake probability service usage",
      description = "Returns the supported:\n " +
          "* Timespan\n * Cutoff distance\n * Longitude\n * Latitude",
      operationId = "probability_doGetProbabilityRate",
      tags = { "Probability Service" })
  @ApiResponse(
      description = "Earthquake probability usage",
      responseCode = "200")
  @Get(uri = "/probability/usage", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetUsageProbability(HttpRequest<?> request) {
    var urlHelper = servlet.urlHelper(request);
    return RateService.handleDoGetUsage(Service.PROBABILITY, urlHelper);
  }

  /**
   * GET method to compute probability, query based.
   * 
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   * @param timespan Forcast time span (in years) ([1, 10000])
   */
  @Operation(
      summary = "Compute earthquake probabilities",
      description = "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
      operationId = "probability_doGetProbability",
      tags = { "Probability Service" })
  @ApiResponse(
      description = "Earthquake probabilities",
      responseCode = "200")
  @Get(
      uri = "/probability{?longitude,latitude,distance,timespan}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetProbability(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @QueryValue @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @QueryValue @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @QueryValue @Nullable Double distance,
      @Schema(
          required = true,
          minimum = "1",
          maximum = "10000") @QueryValue @Nullable Double timespan) {
    var service = Service.PROBABILITY;
    var urlHelper = servlet.urlHelper(request);
    var query = new Query(service, longitude, latitude, distance, Optional.ofNullable(timespan));
    return RateService.handleDoGetCalc(query, urlHelper);
  }

  /**
   * GET method to compute probability, slash based
   * 
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param distance Cutoff distance (in km) ([0.01, 1000])
   * @param timespan Forecast time span (in years) ([1, 10000])
   */
  @Operation(
      summary = "Compute earthquake probabilities",
      description = "Compute cumulative earthquake probabilities P(M ≥ x) at a location",
      operationId = "probability_doGetProbabilitySlash",
      tags = { "Probability Service" })
  @ApiResponse(
      description = "Earthquake probabilities",
      responseCode = "200")
  @Get(
      uri = "/probability{/longitude}{/latitude}{/distance}{/timespan}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetProbabilitySlash(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @PathVariable @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @PathVariable @Nullable Double latitude,
      @Schema(
          required = true,
          minimum = "0.01",
          maximum = "1000") @PathVariable @Nullable Double distance,
      @Schema(
          required = true,
          minimum = "1",
          maximum = "10000") @PathVariable @Nullable Double timespan) {
    var service = Service.PROBABILITY;
    var urlHelper = servlet.urlHelper(request);
    var query = new Query(service, longitude, latitude, distance, Optional.ofNullable(timespan));
    return RateService.handleDoGetCalc(query, urlHelper);
  }

}

package gov.usgs.earthquake.nshmp.www;

import javax.annotation.Nullable;
import javax.inject.Inject;

import gov.usgs.earthquake.nshmp.internal.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.services.HazardService;
import gov.usgs.earthquake.nshmp.www.services.HazardService.Query;

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
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Micronaut controller for probabilistic seismic hazard calculations.
 *
 * @see HazardService
 *
 * @author U.S. Geological Survey
 */
@Tag(
    name = "Hazard Service",
    description = "USGS NSHMP Hazard Curve Calculator")
@Controller("/hazard")
public class HazardController {

  @Inject
  private NshmpMicronautServlet servlet;

  /**
   * GET method to return the hazard usage.
   *
   * @param request The HTTP request
   */
  @Operation(
      summary = "Returns the hazard service usage",
      description = "Returns the installed model and supported:\n" +
          "* Region bounds\n * Return periods\n * Vs30s",
      operationId = "hazard_doGetUsage")
  @ApiResponse(
      description = "Hazard usage",
      responseCode = "200")
  @Get(uri = "/usage", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetUsage(HttpRequest<?> request) {
    var urlHelper = servlet.urlHelper(request);
    return HazardService.handleDoGetUsage(urlHelper);
  }

  /**
   * GET method to return usage or hazard curves, query based.
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param vs30 The site soil class
   */
  @Operation(
      summary = "Compute hazards",
      description = "Compute hazard curves given longitude, latitude, and Vs30",
      operationId = "hazard_doGetHazard")
  @ApiResponse(
      description = "Hazard curves",
      responseCode = "200")
  @Get(uri = "{?longitude,latitude,vs30}")
  public HttpResponse<String> doGetHazard(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @QueryValue @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @QueryValue @Nullable Double latitude,
      @Schema(required = true) @QueryValue @Nullable Integer vs30) {
    var urlHelper = servlet.urlHelper(request);
    var query = new Query(longitude, latitude, vs30);
    return HazardService.handleDoGetHazard(query, urlHelper);
  }

  /**
   * GET method to return usage or hazard curves, slash based.
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param vs30 The site soil class
   */
  @Operation(
      summary = "Compute hazards",
      description = "Compute hazard curves given longitude, latitude, and Vs30",
      operationId = "hazard_doGetHazardSlash")
  @ApiResponse(
      description = "Hazard curves",
      responseCode = "200")
  @Get(uri = "{/longitude}{/latitude}{/vs30}")
  public HttpResponse<String> doGetHazardSlash(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @PathVariable @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @PathVariable @Nullable Double latitude,
      @Schema(required = true) @PathVariable @Nullable Integer vs30) {
    var urlHelper = servlet.urlHelper(request);
    var query = new Query(longitude, latitude, vs30);
    return HazardService.handleDoGetHazard(query, urlHelper);
  }

}

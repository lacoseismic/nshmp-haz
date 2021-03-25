package gov.usgs.earthquake.nshmp.www;

import javax.inject.Inject;

import gov.usgs.earthquake.nshmp.www.services.HazardService;
import gov.usgs.earthquake.nshmp.www.services.HazardService.QueryParameters;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
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
 * @author U.S. Geological Survey
 */
@Tag(
    name = "Hazard",
    description = "USGS NSHMP hazard calculation service")
@Controller("/hazard")
public class HazardController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Hazard model and service metadata",
      description = "Returns details of the installed model and service request parameters",
      operationId = "hazard_doGetMetadata")
  @ApiResponse(
      description = "Hazard service metadata",
      responseCode = "200")
  @Get
  public HttpResponse<String> doGetMetadata(HttpRequest<?> request) {
    return HazardService.handleDoGetMetadata(request);
  }

  /**
   * @param longitude Longitude in decimal degrees [-360..360]
   * @param latitude Latitude in decimal degrees [-90..90]
   * @param vs30 Site Vs30 value in m/s [150..3000]
   * @param truncate Truncate curves at return periods below ~10,000 years
   * @param maxdir Apply max-direction scaling
   */
  @Operation(
      summary = "Compute probabilisitic hazard at a site",
      description = "Returns hazard curves computed from the installed model",
      operationId = "hazard_doGetHazard")
  @ApiResponse(
      description = "Hazard curves",
      responseCode = "200")
  @Get(uri = "/{longitude}/{latitude}/{vs30}{?truncate,maxdir}")
  public HttpResponse<String> doGetHazard(
      HttpRequest<?> request,
      @Schema(minimum = "-360", maximum = "360") @PathVariable double longitude,
      @Schema(minimum = "-90", maximum = "90") @PathVariable double latitude,
      @Schema(minimum = "150", maximum = "3000") @PathVariable int vs30,
      @QueryValue(defaultValue = "false") @Nullable Boolean truncate,
      @QueryValue(defaultValue = "false") @Nullable Boolean maxdir) {

    /*
     * @Schema annotation parameter constraints only affect Swagger service
     * index page behavior; still need to validate against model. TODO
     */

    var query = new QueryParameters(longitude, latitude, vs30, truncate, maxdir);
    return HazardService.handleDoGetHazard(request, query);
  }
}

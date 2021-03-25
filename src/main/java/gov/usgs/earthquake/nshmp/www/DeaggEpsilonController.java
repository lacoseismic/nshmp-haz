package gov.usgs.earthquake.nshmp.www;

import java.util.EnumMap;

import javax.inject.Inject;

import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.www.services.DeaggEpsilonService;
import gov.usgs.earthquake.nshmp.www.services.DeaggEpsilonService.Query;
import gov.usgs.earthquake.nshmp.www.services.HazardService;

import io.micronaut.core.annotation.Nullable;
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

@Tag(name = "Epsilon Deaggregation Service (experimental)")
@Controller("/deagg-epsilon")
public class DeaggEpsilonController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Get(uri = "/usage", produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetUsage(HttpRequest<?> request) {
    return HazardService.handleDoGetMetadata(request);
  }

  /**
   * GET method to return usage or hazard curves, query based.
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param vs30 The site soil class
   * @param basin Whether to use basin service
   */
  @Operation(
      summary = "Compute epsilon deaggregation",
      description = "Compute epsilon deaggregation given longitude, latitude, Vs30 and IMT-IML map",
      operationId = "deaggEpsilon_doGetDeaggEpsilon")
  @ApiResponse(
      description = "Epsilon deaggregations",
      responseCode = "200")
  @Get(uri = "{?longitude,latitude,vs30,basin}")
  public HttpResponse<String> doGetDeaggEpsilon(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @QueryValue @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @QueryValue @Nullable Double latitude,
      @Schema(required = true) @QueryValue @Nullable Integer vs30,
      @Schema(defaultValue = "false") @QueryValue @Nullable Boolean basin,
      @Schema(
          defaultValue = "{\"SA0P01\": 0.01}",
          required = true) @QueryValue @Nullable EnumMap<Imt, Double> imtImls) {
    var query = new Query(request, longitude, latitude, vs30, basin);
    return DeaggEpsilonService.handleDoGetDeaggEpsilon(request, query);
  }

  /**
   * GET method to return usage or hazard curves, slash based.
   *
   * @param request The HTTP request
   * @param longitude Longitude (in decimal degrees) ([-360, 360])
   * @param latitude Latitude (in decimal degrees) ([-90, 90])
   * @param vs30 The site soil class
   * @param basin Whether to use basin service
   */
  @Operation(
      summary = "Compute epsilon deaggregation",
      description = "Compute epsilon deaggregation given longitude, latitude, Vs30 and IMT-IML map",
      operationId = "deaggEpsilon_doGetDeaggEpsilonSlash")
  @ApiResponse(
      description = "Epsilon deaggregations",
      responseCode = "200")
  @Get(uri = "{/longitude}{/latitude}{/vs30}{/basin}")
  public HttpResponse<String> doGetDeaggEpsilonSlash(
      HttpRequest<?> request,
      @Schema(
          required = true,
          minimum = "-360",
          maximum = "360") @PathVariable @Nullable Double longitude,
      @Schema(
          required = true,
          minimum = "-90",
          maximum = "90") @PathVariable @Nullable Double latitude,
      @Schema(required = true) @PathVariable @Nullable Integer vs30,
      @Schema(defaultValue = "false") @PathVariable @Nullable Boolean basin,
      @Schema(
          defaultValue = "{\"SA0P01\": 0.01}",
          required = true) @QueryValue @Nullable EnumMap<Imt, Double> imtImls) {
    return doGetDeaggEpsilon(request, longitude, latitude, vs30, basin, null);
  }

}

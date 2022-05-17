package gov.usgs.earthquake.nshmp.www.hazard;

import java.util.Set;

import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.hazard.HazardService.Metadata;
import gov.usgs.earthquake.nshmp.www.hazard.HazardService.Request;
import gov.usgs.earthquake.nshmp.www.hazard.HazardService.Response;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

/**
 * Micronaut web service controller for probabilistic seismic hazard
 * calculations.
 *
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
      operationId = "hazard-metadata")
  @ApiResponse(
      description = "Hazard service metadata",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = MetadataResponse.class)))
  @Get(produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetMetadata(HttpRequest<?> http) {
    try {
      return HazardService.getMetadata(http);
    } catch (Exception e) {
      return ServletUtil.error(
          HazardService.LOG, e,
          HazardService.NAME,
          http.getUri().toString());
    }
  }

  /**
   * @param longitude Longitude in decimal degrees
   * @param latitude Latitude in decimal degrees
   * @param vs30 Site Vs30 value in m/s [150..3000]
   * @param truncate Truncate curves at return periods below ~10,000 years
   * @param maxdir Apply max-direction scaling
   * @param imt Optional IMTs at which to compute hazard. If none are supplied,
   *        then the supported set for the installed model is used. Responses
   *        for numerous IMT's are quite large, on the order of MB.
   */
  @Operation(
      summary = "Compute probabilisitic hazard at a site",
      description = "Returns hazard curves computed from the installed model")
  @ApiResponse(
      description = "Hazard curves",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = HazardResponse.class)))
  @Get(
      uri = "/{longitude}/{latitude}/{vs30}{?truncate,maxdir,imt}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetHazard(
      HttpRequest<?> http,
      @PathVariable double longitude,
      @PathVariable double latitude,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable int vs30,
      @QueryValue(
          defaultValue = "false") @Nullable Boolean truncate,
      @QueryValue(
          defaultValue = "false") @Nullable Boolean maxdir,
      @QueryValue @Nullable Set<Imt> imt) {
    try {
      Set<Imt> imts = HazardService.readImts(http);
      HazardService.Request request = new HazardService.Request(
          http,
          longitude, latitude, vs30,
          imts,
          truncate, maxdir);
      return HazardService.getHazard(request);
    } catch (Exception e) {
      return ServletUtil.error(
          HazardService.LOG, e,
          HazardService.NAME,
          http.getUri().toString());
    }
  }

  // For Swagger schemas
  private static class HazardResponse extends ResponseBody<Request, Response> {}

  // For Swagger schemas
  private static class MetadataResponse extends ResponseBody<String, Metadata> {};
}

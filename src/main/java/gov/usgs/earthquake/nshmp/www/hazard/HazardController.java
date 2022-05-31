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
 * <p>See src/main/resources/application.yml nshmp-haz.model-path for installed
 * model.
 *
 * <p>To run the Micronaut jar file with a model: java -jar
 * path/to/nshmp-haz.jar --model=<path/to/model>
 *
 * @author U.S. Geological Survey
 */
@Tag(
    name = "Hazard Calculation",
    description = "USGS NSHM hazard calculation service")
@Controller("/hazard")
public class HazardController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Hazard calculation model and service metadata",
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
   * @param longitude Longitude in decimal degrees in the range
   * @param latitude Latitude in decimal degrees in the range
   * @param vs30 Site Vs30 value in the range [150..3000] m/s
   * @param truncate Truncate curves at return periods below ~10,000 years.
   * @param maxdir Apply max-direction scaling.
   * @param imt Optional IMTs at which to compute hazard. If none are supplied,
   *        then the supported set for the installed model is used. Responses
   *        for numerous IMT's are quite large, on the order of MB.
   */
  @Operation(
      summary = "Compute probabilisitic hazard at a site",
      description = "Returns hazard curves computed from the installed model",
      operationId = "hazard-calc")
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
          http, longitude, latitude, vs30, imts, truncate, maxdir);
      return HazardService.getHazard(request);
    } catch (Exception e) {
      return ServletUtil.error(
          HazardService.LOG, e,
          HazardService.NAME,
          http.getUri().toString());
    }
  }

  // Swagger schema
  private static class HazardResponse extends ResponseBody<Request, Response> {}

  // Swagger schema
  private static class MetadataResponse extends ResponseBody<String, Metadata> {};
}

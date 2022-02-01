package gov.usgs.earthquake.nshmp.www.hazard;

import java.util.Set;

import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
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
      description = "Returns details of the installed model and service request parameters")
  @ApiResponse(
      description = "Hazard service metadata",
      responseCode = "200")
  @Get
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
   * @param longitude Longitude in decimal degrees [-360..360]
   * @param latitude Latitude in decimal degrees [-90..90]
   * @param vs30 Site Vs30 value in m/s [150..3000]
   * @param truncate Truncate curves at return periods below ~10,000 years
   * @param maxdir Apply max-direction scaling
   * @param imt Optional IMTs at which to compute hazard. If none are supplied
   *        then the default set of supported IMTs for the installed model is
   *        used. Note that a model may not support all the values listed below
   *        (see disagreggation metadata). Responses for numerous IMT's are
   *        quite large, on the order of MB. Multiple IMTs may be comma
   *        delimited, e.g. @code{?imt=PGA,SA0p2,SA1P0}.
   *
   */
  @Operation(
      summary = "Compute probabilisitic hazard at a site",
      description = "Returns hazard curves computed from the installed model")
  @ApiResponse(
      description = "Hazard curves",
      responseCode = "200")
  @Get(uri = "/{longitude}/{latitude}/{vs30}{?truncate,maxdir,imt}")
  public HttpResponse<String> doGetHazard(
      HttpRequest<?> http,
      @Schema(
          minimum = "-360",
          maximum = "360") @PathVariable double longitude,
      @Schema(
          minimum = "-90",
          maximum = "90") @PathVariable double latitude,
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
          truncate, maxdir,
          imts);
      return HazardService.getHazard(request);
    } catch (Exception e) {
      return ServletUtil.error(
          HazardService.LOG, e,
          HazardService.NAME,
          http.getUri().toString());
    }
  }
}

package gov.usgs.earthquake.nshmp.www.hazard;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;

import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ServletUtil;

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
import jakarta.inject.Inject;

/**
 * Micronaut web service controller for disaggregation of probabilistic seismic
 * hazard.
 *
 * @author U.S. Geological Survey
 */
@Tag(
    name = "Disaggregation",
    description = "USGS NSHMP hazard disaggregation service")
@Controller("/disagg")
public class DisaggController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Disaggregation model and service metadata",
      description = "Returns details of the installed model and service request parameters")
  @ApiResponse(
      description = "Disaggregation service metadata",
      responseCode = "200")
  @Get(produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetMetadata(HttpRequest<?> http) {
    try {
      return DisaggService.getMetadata(http);
    } catch (Exception e) {
      return ServletUtil.error(
          DisaggService.LOG, e,
          DisaggService.NAME,
          http.getUri().toString());
    }
  }

  /**
   * @param longitude Longitude in the range [-360..360]°.
   * @param latitude Latitude in the range [-90..90]°.
   * @param vs30 Site Vs30 value in the range [150..3000] m/s.
   * @param returnPeriod The return period of the target ground motion, or
   *        intensity measure level (IML), in the range [1..20000] years.
   * @param imt Optional IMTs at which to compute hazard. If none are supplied,
   *        then the supported set for the installed model is used. Note that a
   *        model may not support all the values listed below (see
   *        disagreggation metadata). Responses for numerous IMT's are quite
   *        large, on the order of MB. Multiple IMTs may be comma delimited,
   *        e.g. ?imt=PGA,SA0p2,SA1P0.
   */
  @Operation(
      summary = "Disaggregate hazard at a specified return period",
      description = "Returns a hazard disaggregation computed from the installed model")
  @ApiResponse(
      description = "Disaggregation",
      responseCode = "200")
  @Get(
      uri = "rp/{longitude}/{latitude}/{vs30}/{returnPeriod}{?imt}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetDisaggReturnPeriod(
      HttpRequest<?> http,
      @Schema(
          minimum = "-360",
          maximum = "360") @PathVariable double longitude,
      @Schema(
          minimum = "-90",
          maximum = "90") @PathVariable double latitude,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable double vs30,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable double returnPeriod,
      @Schema() @QueryValue @Nullable Set<Imt> imt) {
    try {
      Set<Imt> imts = HazardService.readImts(http);
      DisaggService.RequestRp request = new DisaggService.RequestRp(
          http,
          longitude, latitude, vs30,
          returnPeriod,
          imts);
      return DisaggService.getDisaggRp(request);
    } catch (Exception e) {
      return ServletUtil.error(
          DisaggService.LOG, e,
          DisaggService.NAME,
          http.getUri().toString());
    }
  }

  /**
   * @param longitude Longitude in the range [-360..360]°.
   * @param latitude Latitude in decimal degrees [-90..90]°.
   * @param vs30 Site Vs30 value in the range [150..3000] m/s.
   */
  @Operation(
      summary = "Disaggregate hazard at specified IMLs",
      description = "Returns a hazard disaggregation computed from the installed model")
  @ApiResponse(
      description = "Disaggregation",
      responseCode = "200")
  @Get(
      uri = "iml/{longitude}/{latitude}/{vs30}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetDisaggIml(
      HttpRequest<?> http,
      @Schema(
          minimum = "-360",
          maximum = "360") @PathVariable double longitude,
      @Schema(
          minimum = "-90",
          maximum = "90") @PathVariable double latitude,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable double vs30) {

    /*
     * Developer notes:
     *
     * It is awkward to support IMT=#; numerous unique keys that may or may not
     * be present yields a clunky swagger interface. The disagg-iml endpoint
     * requires one or more IMT=# query arguments. Documented in example.
     */

    try {
      Map<Imt, Double> imtImlMap = http.getParameters().asMap(Imt.class, Double.class);
      checkArgument(!imtImlMap.isEmpty(), "No IMLs supplied");
      DisaggService.RequestIml request = new DisaggService.RequestIml(
          http,
          longitude, latitude, vs30,
          imtImlMap);
      return DisaggService.getDisaggIml(request);
    } catch (Exception e) {
      return ServletUtil.error(
          DisaggService.LOG, e,
          DisaggService.NAME,
          http.getUri().toString());
    }
  }
}

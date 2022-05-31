package gov.usgs.earthquake.nshmp.www.hazard;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;

import gov.usgs.earthquake.nshmp.calc.DataType;
import gov.usgs.earthquake.nshmp.gmm.Imt;
import gov.usgs.earthquake.nshmp.www.NshmpMicronautServlet;
import gov.usgs.earthquake.nshmp.www.ResponseBody;
import gov.usgs.earthquake.nshmp.www.ServletUtil;
import gov.usgs.earthquake.nshmp.www.hazard.DisaggService.DisaggDataType;
import gov.usgs.earthquake.nshmp.www.hazard.DisaggService.RequestIml;
import gov.usgs.earthquake.nshmp.www.hazard.DisaggService.RequestRp;
import gov.usgs.earthquake.nshmp.www.hazard.DisaggService.Response;
import gov.usgs.earthquake.nshmp.www.hazard.HazardService.Metadata;

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
 * Micronaut web service controller for disaggregation of probabilistic seismic
 * hazard.
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
    name = "Hazard Disaggregation",
    description = "USGS NSHM hazard disaggregation service")
@Controller("/disagg")
public class DisaggController {

  @Inject
  private NshmpMicronautServlet servlet;

  @Operation(
      summary = "Disaggregation model and service metadata",
      description = "Returns details of the installed model and service request parameters",
      operationId = "disagg-metadata")
  @ApiResponse(
      description = "Disaggregation service metadata",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = MetadataResponse.class)))
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
   * @param longitude Longitude in decimal degrees in the range
   * @param latitude Latitude in decimal degrees in the range
   * @param vs30 Site Vs30 value in the range [150..3000] m/s
   * @param returnPeriod The return period of the target ground motion, or
   *        intensity measure level (IML), in the range [1..20000] years.
   * @param imt Optional IMTs at which to compute hazard. If none are supplied,
   *        then the supported set for the installed model is used. Responses
   *        for numerous IMT's are quite large, on the order of MB.
   * @param out The data types to output
   */
  @Operation(
      summary = "Disaggregate hazard at a specified return period",
      description = "Returns a hazard disaggregation computed from the installed model",
      operationId = "disagg-calc-rp")
  @ApiResponse(
      description = "Disaggregation",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = DisaggResponseReturnPeriod.class)))
  @Get(
      uri = "{longitude}/{latitude}/{vs30}/{returnPeriod}{?imt}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetDisaggReturnPeriod(
      HttpRequest<?> http,
      @PathVariable double longitude,
      @PathVariable double latitude,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable double vs30,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable double returnPeriod,
      @QueryValue @Nullable Set<Imt> imt,
      @QueryValue @Nullable Set<DisaggDataType> out) {
    try {
      Set<Imt> imts = HazardService.readImts(http);
      Set<DataType> dataTypes = HazardService.readDataTypes(http);
      DisaggService.RequestRp request = new DisaggService.RequestRp(
          http, longitude, latitude, vs30, imts, returnPeriod, dataTypes);
      return DisaggService.getDisaggRp(request);
    } catch (Exception e) {
      return ServletUtil.error(
          DisaggService.LOG, e,
          DisaggService.NAME,
          http.getUri().toString());
    }
  }

  /**
   * @param longitude Longitude in decimal degrees in the range
   * @param latitude Latitude in decimal degrees in the range
   * @param vs30 Site Vs30 value in the range [150..3000] m/s
   * @param imls Mapping of IMTs to disaggregation intensity measure levels
   * @param out The data types to output
   */
  @Operation(
      summary = "Disaggregate hazard at specified IMLs",
      description = "Returns a hazard disaggregation computed from the installed model",
      operationId = "disagg-calc-iml")
  @ApiResponse(
      description = "Disaggregation",
      responseCode = "200",
      content = @Content(
          schema = @Schema(implementation = DisaggResponseIml.class)))
  @Get(
      uri = "{longitude}/{latitude}/{vs30}",
      produces = MediaType.APPLICATION_JSON)
  public HttpResponse<String> doGetDisaggIml(
      HttpRequest<?> http,
      @PathVariable double longitude,
      @PathVariable double latitude,
      @Schema(
          minimum = "150",
          maximum = "3000") @PathVariable double vs30,
      @Schema(
          example = "{\"PGA\": 0, \"SA0P2\": 0, \"SA1P0\": 0, \"SA2P0\": 0}") @QueryValue @Nullable Map<Imt, Double> imls,
      @QueryValue @Nullable Set<DisaggDataType> out) {
    try {
      Map<Imt, Double> imtImlMap = http.getParameters().asMap(Imt.class, Double.class);
      checkArgument(!imtImlMap.isEmpty(), "No IMLs supplied");
      Set<DataType> dataTypes = HazardService.readDataTypes(http);
      DisaggService.RequestIml request = new DisaggService.RequestIml(
          http, longitude, latitude, vs30, imtImlMap, dataTypes);
      return DisaggService.getDisaggIml(request);
    } catch (Exception e) {
      return ServletUtil.error(
          DisaggService.LOG, e,
          DisaggService.NAME,
          http.getUri().toString());
    }
  }

  // Swagger schema
  private static class DisaggResponseIml extends ResponseBody<RequestIml, Response> {}

  // Swagger schema
  private static class DisaggResponseReturnPeriod extends ResponseBody<RequestRp, Response> {}

  // Swagger schema
  private static class MetadataResponse extends ResponseBody<String, Metadata> {};
}

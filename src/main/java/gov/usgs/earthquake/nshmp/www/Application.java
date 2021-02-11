package gov.usgs.earthquake.nshmp.www;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "USGS NSHM Services",
        description = "National Seismic Hazard Model (NSHM) hazard calculations and queries."))
public class Application {

  public static void main(String[] args) {
    Micronaut.build(args)
        .mainClass(Application.class)
        .start();
  }

}

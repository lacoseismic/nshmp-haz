package gov.usgs.earthquake.nshmp.www;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
        title = "NSHMP NSHM Services",
        description = "### Services related to National Seismic Hazard Models:\n" +
            "* Source Model: Get the metadata about current installed NSHM"))
public class Application {

  public static void main(String[] args) {
    Micronaut.build(args)
        .mainClass(Application.class)
        .start();
  }

}

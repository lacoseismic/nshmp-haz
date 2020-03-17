package gov.usgs.earthquake.nshmp.www;

import io.micronaut.runtime.Micronaut;

public class Applicaiton {

  public static void main(String[] args) {
    Micronaut.build(args)
        .mainClass(Applicaiton.class)
        .start();
  }

}

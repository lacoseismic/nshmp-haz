package gov.usgs.earthquake.nshmp.www.meta;

@SuppressWarnings("unused")
public class Parameter {

  private final String display;
  private final String value;

  public Parameter(String display, String value) {
    this.display = display;
    this.value = value;
  }
}

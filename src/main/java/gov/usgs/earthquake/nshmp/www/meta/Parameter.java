package gov.usgs.earthquake.nshmp.www.meta;

public class Parameter {

  private final String display;
  private final String value;

  public Parameter(String display, String value) {
    this.display = display;
    this.value = value;
  }

  public String getDisplay() {
    return display;
  }

  public String getValue() {
    return value;
  }
}

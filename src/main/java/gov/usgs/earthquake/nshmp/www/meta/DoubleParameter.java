package gov.usgs.earthquake.nshmp.www.meta;

public final class DoubleParameter {

  private final String name;
  private final String units;
  private final double min;
  private final double max;

  public DoubleParameter(String name, String units, double min, double max) {
    this.name = name;
    this.units = units;
    this.min = min;
    this.max = max;
  }

  public String getName() {
    return name;
  }

  public String getUnits() {
    return units;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }
}

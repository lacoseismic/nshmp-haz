package gov.usgs.earthquake.nshmp.www.meta;

public enum Region {

  AK(
      "Alaska",
      new double[] { 48.0, 72.0 },
      new double[] { -200.0, -125.0 },
      new double[] { 48.0, 72.0 },
      new double[] { -200.0, -125.0 }),

  CONUS(
      "Conterminous US",
      new double[] { 24.6, 50.0 },
      new double[] { -125.0, -65.0 },
      new double[] { 24.6, 50.0 },
      new double[] { -125.0, -65.0 }),

  HI(
      "Hawaii",
      new double[] { 18.0, 23.0 },
      new double[] { -161.0, -154.0 },
      new double[] { 18.0, 23.0 },
      new double[] { -161.0, -154.0 });

  public final String label;

  public final double minlatitude;
  public final double maxlatitude;
  public final double minlongitude;
  public final double maxlongitude;

  public final double uiminlatitude;
  public final double uimaxlatitude;
  public final double uiminlongitude;
  public final double uimaxlongitude;

  private Region(
      String label,
      double[] latRange,
      double[] lonRange,
      double[] uiLatRange,
      double[] uiLonRange) {

    this.label = label;

    this.minlatitude = latRange[0];
    this.maxlatitude = latRange[1];
    this.minlongitude = lonRange[0];
    this.maxlongitude = lonRange[1];

    this.uiminlatitude = uiLatRange[0];
    this.uimaxlatitude = uiLatRange[1];
    this.uiminlongitude = uiLonRange[0];
    this.uimaxlongitude = uiLonRange[1];
  }

  @Override
  public String toString() {
    return label;
  }

}

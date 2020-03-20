package gov.usgs.earthquake.nshmp.www;

import java.util.Set;

import gov.usgs.earthquake.nshmp.www.meta.Region;

public enum Model {

  AK_2007(Set.of(BaseModel.AK_2007)),

  CONUS_2008(Set.of(BaseModel.CEUS_2008, BaseModel.WUS_2008)),

  CONUS_2014(Set.of(BaseModel.CEUS_2014, BaseModel.WUS_2014)),

  CONUS_2014B(Set.of(BaseModel.CEUS_2014, BaseModel.WUS_2014B)),

  CONUS_2018(Set.of(BaseModel.CEUS_2018, BaseModel.WUS_2018)),

  HI_2020(Set.of(BaseModel.HI_2020));

  private final String label;
  private final String year;
  private final Set<BaseModel> models;
  private final Region region;

  private Model(Set<BaseModel> models) {
    year = name().substring(name().lastIndexOf("_") + 1);
    region = deriveRegion(name());
    label = String.format("%s %s Hazard Model", year, region.label);
    this.models = models;
  }

  public String label() {
    return label;
  }

  public Set<BaseModel> models() {
    return Set.copyOf(models);
  }

  public Region region() {
    return region;
  }

  public String year() {
    return year;
  }

  private Region deriveRegion(String region) {
    return region.startsWith("AK") ? Region.AK
        : region.startsWith("HI") ? Region.HI : Region.CONUS;
  }

}

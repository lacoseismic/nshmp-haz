package gov.usgs.earthquake.nshmp.www;

import java.util.Set;

public enum Model {

  AK_2007(Set.of(BaseModel.AK_2007)),
  CONUS_2008(Set.of(BaseModel.CEUS_2008, BaseModel.WUS_2008)),
  CONUS_2014(Set.of(BaseModel.CEUS_2014, BaseModel.WUS_2014)),
  CONUS_2014b(Set.of(BaseModel.CEUS_2014, BaseModel.WUS_2014B)),
  CONUS_2018(Set.of(BaseModel.CEUS_2018, BaseModel.WUS_2018)),
  HI_2020(Set.of(BaseModel.HI_2020));

  private Set<BaseModel> models;

  private Model(Set<BaseModel> models) {
    this.models = models;
  }

  public Set<BaseModel> models() {
    return Set.copyOf(models);
  }

}

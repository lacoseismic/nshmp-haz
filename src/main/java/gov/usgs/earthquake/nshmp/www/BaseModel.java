package gov.usgs.earthquake.nshmp.www;

import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_1150;
import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_180;
import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_2000;
import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_259;
import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_360;
import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_537;
import static gov.usgs.earthquake.nshmp.calc.Vs30.VS_760;
import static gov.usgs.earthquake.nshmp.gmm.Imt.PGA;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P1;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P2;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P3;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P5;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P75;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA1P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA2P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA3P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA4P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA5P0;

import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.Sets;

import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.gmm.Imt;

public enum BaseModel {

  AK_2007(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA1P0, SA2P0),
      EnumSet.of(VS_760)),

  CEUS_2008(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA1P0, SA2P0),
      EnumSet.of(VS_760, VS_2000)),

  WUS_2008(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA0P75, SA1P0, SA2P0, SA3P0),
      EnumSet.of(VS_1150, VS_760, VS_537, VS_360, VS_259, VS_180)),

  CEUS_2014(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA1P0, SA2P0),
      EnumSet.of(VS_760, VS_2000)),

  WUS_2014(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA0P75, SA1P0, SA2P0, SA3P0),
      EnumSet.of(VS_1150, VS_760, VS_537, VS_360, VS_259, VS_180)),

  WUS_2014B(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA0P75, SA1P0, SA2P0, SA3P0, SA4P0, SA5P0),
      EnumSet.of(VS_1150, VS_760, VS_537, VS_360, VS_259, VS_180)),

  CEUS_2018(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA0P75, SA1P0, SA2P0, SA3P0, SA4P0, SA5P0),
      EnumSet.of(VS_1150, VS_760, VS_537, VS_360, VS_259, VS_180)),

  WUS_2018(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA0P75, SA1P0, SA2P0, SA3P0, SA4P0, SA5P0),
      EnumSet.of(VS_1150, VS_760, VS_537, VS_360, VS_259, VS_180)),

  HI_2020(
      EnumSet.of(PGA, SA0P1, SA0P2, SA0P3, SA0P5, SA0P75, SA1P0, SA2P0, SA3P0, SA5P0),
      EnumSet.of(VS_1150, VS_760, VS_537, VS_360, VS_259, VS_180));

  private static final String MODEL_DIR = "models";

  public final Set<Imt> imts;
  public final Set<Vs30> vs30s;

  public final String path;
  public final String year;

  private BaseModel(Set<Imt> imts, Set<Vs30> vs30s) {
    this.imts = Sets.immutableEnumSet(imts);
    this.vs30s = Sets.immutableEnumSet(vs30s);
    var region = deriveRegion(name());
    year = name().substring(name().lastIndexOf('_') + 1);
    path = Paths.get(MODEL_DIR)
        .resolve(region.toLowerCase())
        .resolve(year.toLowerCase())
        .toString();
  }

  private static String deriveRegion(String s) {
    return s.startsWith("AK") ? "AK" : s.startsWith("WUS") ? "WUS"
        : s.startsWith("HI") ? "HI" : "CEUS";
  }
}

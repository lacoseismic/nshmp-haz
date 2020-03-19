package gov.usgs.earthquake.nshmp.www.meta;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import gov.usgs.earthquake.nshmp.calc.Vs30;
import gov.usgs.earthquake.nshmp.gmm.Imt;

@SuppressWarnings("unused")
class EditionConstraints implements Constraints {

  private final List<String> region;
  private final List<String> imt;
  private final List<String> vs30;

  EditionConstraints(Set<Region> regions, Set<Imt> imts) {
    // converting to Strings here, otherwise EnumSerializer will be used
    // and we want a compact list of (possible modified) enum.name()s
    this.region = MetaUtil.enumsToNameList(regions);
    this.imt = MetaUtil.enumsToNameList(imts);
    Set<Vs30> vs30s = EnumSet.noneOf(Vs30.class);
    for (Region region : regions) {
      vs30s.addAll(region.vs30s);
    }
    this.vs30 = MetaUtil.enumsToStringList(vs30s, vs30 -> vs30.name().substring(3));
  }
}

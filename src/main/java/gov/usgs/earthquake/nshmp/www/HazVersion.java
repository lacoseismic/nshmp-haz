package gov.usgs.earthquake.nshmp.www;

import com.google.common.io.Resources;

import gov.usgs.earthquake.nshmp.internal.AppVersion;

public class HazVersion implements AppVersion {

  public VersionInfo getVersionInfo() {
    var resource = Resources.getResource("version/nshmp-haz-version.json");
    return AppVersion.versionInfo(resource);
  }
}

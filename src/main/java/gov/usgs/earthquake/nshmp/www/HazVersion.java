package gov.usgs.earthquake.nshmp.www;

import com.google.common.io.Resources;

import gov.usgs.earthquake.nshmp.internal.AppVersion;
import gov.usgs.earthquake.nshmp.internal.LibVersion;

public class HazVersion implements AppVersion {

  public static VersionInfo[] appVersions() {
    VersionInfo[] versions = {
        new HazVersion().getVersionInfo(),
        new LibVersion().getVersionInfo(),
        new WsUtilsVersion().getVersionInfo(),
    };
    return versions;
  }

  public VersionInfo getVersionInfo() {
    var resource = Resources.getResource("version/nshmp-haz-version.json");
    return AppVersion.versionInfo(resource);
  }
}

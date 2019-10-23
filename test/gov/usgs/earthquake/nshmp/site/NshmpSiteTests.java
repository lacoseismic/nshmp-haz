package gov.usgs.earthquake.nshmp.site;

import static gov.usgs.earthquake.nshmp.site.NshmpSite.ADAK_AK;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.BOISE_ID;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.BRADSHAW_AIRFIELD_HI;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.CENTURY_CITY_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.CHARLESTON_SC;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.CHICAGO_IL;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.CONCORD_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.DIABLO_CANYON_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.EVERETT_WA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.HANFORD_SITE_WA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.IDAHO_NATIONAL_LAB_ID;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.IRVINE_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.LAS_VEGAS_NV;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.LONG_BEACH_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.LOS_ALAMOS_NATIONAL_LAB_NM;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.LOS_ANGELES_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.MCGRATH_AK;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.MEMPHIS_TN;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.MONTEREY_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.NEW_YORK_NY;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.NORTHRIDGE_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.OAKLAND_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.PALO_VERDE_AZ;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.PORTLAND_OR;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.PUUWAI_HI;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.RENO_NV;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.RIVERSIDE_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SACRAMENTO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SALT_LAKE_CITY_UT;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SANTA_BARBARA_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SANTA_CRUZ_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SANTA_ROSA_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_BERNARDINO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_DIEGO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_FRANCISCO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_JOSE_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_LUIS_OBISPO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_MATEO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SAN_ONOFRE_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.SEATTLE_WA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.ST_LOUIS_MO;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.TACOMA_WA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.VALLEJO_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.VENTURA_CA;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.WASHINGTON_DC;
import static gov.usgs.earthquake.nshmp.site.NshmpSite.YAKUTAT_AK;
import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Predicate;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.site.NshmpSite.StateComparator;

@SuppressWarnings("javadoc")
public class NshmpSiteTests {

  @Test
  public final void methodsTest() {
    NshmpSite s = WASHINGTON_DC;
    assertEquals(s.state(), UsRegion.DC);
    assertEquals(s.location(), Location.create(38.90, -77.05));
    assertEquals(s.toString(), "Washington DC");

    s = MCGRATH_AK;
    assertEquals(s.toString(), "McGrath AK");
  }

  @Test
  public final void groupsTest() {

    /* WUS */
    assertEquals(
        NshmpSite.wus(),
        Sets.newEnumSet(Iterables.filter(
            EnumSet.allOf(NshmpSite.class),
            new Predicate<NshmpSite>() {
              @Override
              public boolean test(NshmpSite site) {
                return site.location().longitude <= -100.0 && site.location().longitude >= -125.0;
              }
            }::test), NshmpSite.class));

    /* CEUS */
    assertEquals(
        NshmpSite.ceus(),
        Sets.newEnumSet(Iterables.filter(
            EnumSet.allOf(NshmpSite.class),
            new Predicate<NshmpSite>() {
              @Override
              public boolean test(NshmpSite site) {
                return site.location().longitude >= -115.0;
              }
            }::test), NshmpSite.class));

    /* Alaska */
    assertEquals(
        NshmpSite.alaska(),
        EnumSet.range(ADAK_AK, YAKUTAT_AK));

    /* Hawaii */
    assertEquals(
        NshmpSite.hawaii(),
        EnumSet.range(BRADSHAW_AIRFIELD_HI, PUUWAI_HI));

    /* NRC comparision sites */
    assertEquals(
        NshmpSite.nrc(),
        Sets.newEnumSet(Iterables.filter(
            EnumSet.allOf(NshmpSite.class),
            new Predicate<NshmpSite>() {
              @Override
              public boolean test(NshmpSite site) {
                return site.location().longitude >= -105.5;
              }
            }::test), NshmpSite.class));

    /* DOE facilities */
    assertEquals(
        NshmpSite.facilities(),
        EnumSet.of(
            DIABLO_CANYON_CA,
            SAN_ONOFRE_CA,
            PALO_VERDE_AZ,
            HANFORD_SITE_WA,
            IDAHO_NATIONAL_LAB_ID,
            LOS_ALAMOS_NATIONAL_LAB_NM));

    /* NEHRP test cites */
    assertEquals(
        NshmpSite.nehrp(),
        EnumSet.of(
            // SoCal
            LOS_ANGELES_CA,
            CENTURY_CITY_CA,
            NORTHRIDGE_CA,
            LONG_BEACH_CA,
            IRVINE_CA,
            RIVERSIDE_CA,
            SAN_BERNARDINO_CA,
            SAN_LUIS_OBISPO_CA,
            SAN_DIEGO_CA,
            SANTA_BARBARA_CA,
            VENTURA_CA,
            // NoCal
            OAKLAND_CA,
            CONCORD_CA,
            MONTEREY_CA,
            SACRAMENTO_CA,
            SAN_FRANCISCO_CA,
            SAN_MATEO_CA,
            SAN_JOSE_CA,
            SANTA_CRUZ_CA,
            VALLEJO_CA,
            SANTA_ROSA_CA,
            // PNW
            SEATTLE_WA,
            TACOMA_WA,
            EVERETT_WA,
            PORTLAND_OR,
            // B&R
            SALT_LAKE_CITY_UT,
            BOISE_ID,
            RENO_NV,
            LAS_VEGAS_NV,
            // CEUS
            ST_LOUIS_MO,
            MEMPHIS_TN,
            CHARLESTON_SC,
            CHICAGO_IL,
            NEW_YORK_NY));
  }

  @Test
  public final void comparatorTest() {
    Comparator<NshmpSite> c = new StateComparator();
    assertEquals(c.compare(CHICAGO_IL, LOS_ANGELES_CA), 1);
    assertEquals(c.compare(LOS_ANGELES_CA, SAN_FRANCISCO_CA), -1);
  }

}

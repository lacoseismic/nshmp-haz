package gov.usgs.earthquake.nshmp.www.hazard;

import static gov.usgs.earthquake.nshmp.gmm.Imt.PGA;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P01;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P02;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P03;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P05;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P075;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P1;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P15;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P2;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P25;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P3;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P4;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P5;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P75;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA10P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA1P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA1P5;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA2P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA3P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA4P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA5P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA7P5;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import gov.usgs.earthquake.nshmp.gmm.Imt;

/*
 * Max direction map derived from Shahi and Baker (2012) and used in the BSSC.
 */
class MaxDirection {

  static final Map<Imt, Double> FACTORS;

  static {
    Map<Imt, Double> map = new EnumMap<>(Imt.class);
    map.put(PGA, 1.0);
    map.put(SA0P01, 1.200);
    map.put(SA0P02, 1.200);
    map.put(SA0P03, 1.200);
    map.put(SA0P05, 1.200);
    map.put(SA0P075, 1.200);
    map.put(SA0P1, 1.200);
    map.put(SA0P15, 1.200);
    map.put(SA0P2, 1.200);
    map.put(SA0P25, 1.203);
    map.put(SA0P3, 1.206);
    map.put(SA0P4, 1.213);
    map.put(SA0P5, 1.219);
    map.put(SA0P75, 1.234);
    map.put(SA1P0, 1.250);
    map.put(SA1P5, 1.253);
    map.put(SA2P0, 1.256);
    map.put(SA3P0, 1.261);
    map.put(SA4P0, 1.267);
    map.put(SA5P0, 1.272);
    map.put(SA7P5, 1.286);
    map.put(SA10P0, 1.300);
    FACTORS = Collections.unmodifiableMap(map);
  }
}

# Rupture-Scaling Relations

Rupture scaling models describe relationships between rupture geometry and magnitude. Such models
are used in a NSHM to:

* Compute an expected magnitude from a rupture geometry.
* Compute the size (length or area) of a rupture from a magnitude.
* Compute point-source distance corrections (optimization for unknown strike)

Rupture scaling model implementations typically impose restrictions on rupture aspect ratio.

## Magnitude-Scaling Relationships

| Region             | Reference                  | Tectonic Setting | Type         |
|:------------------ |:-------------------------  |:------------ |:---------------- |
| WellsCoppersmith-L | Wells & Coppersmith (1994) | active crust | magnitude-length |
| EllsworthB         | WGCEP (2002)               | active crust | magnitude-area   |
| EllsworthB-SqrtL¹  | Shaw (2013b)               | active crust | magnitude-area   |
| HanksBakun-08      | Hanks & Bakun (2008)       | active crust | magnitude-area   |
| Shaw09mod          | Shaw (2013a, 2013b)        | active crust | magnitude-area   |
| Shaw09mod-CSD¹     | Shaw (2013a, 2013b)        | active crust | magnitude-area   |
| Somerville-01      | Somerville et al. (2001)   | stable crust | magnitude-area   |
| Strasser-10        | Strasser et al. (2010)     | subduction   | magnitude-length |
| Murotani-08        | Murotani et al. (2008)     | subduction   | magnitude-length |
| Papazachos-04      | Papazachos et al. (2004)   | subduction   | magnitude-length |
| Youngs-97          | Youngs et al. (1997)       | subduction   | magnitude-length |

¹ UCERF3 uses rupture scaling relationships to also balance slip rate when computing rupture
rates. These models consider alternative slip-length scaling relations relative to the default
computed from rupture area and moment; see Field et al. (2014) for details.

² Also referred to as the 'Geomatrix' relation

## References

Hanks TC, and Bakun WH (2008) M- log A observations of recent large earthquakes. Bulletin of the
Seismological Society of America 98(1): 490–494.

Murotani S, Miyake H, Koketsu K (2008) Scaling of characterized slip models for plate-boundary
earthquakes. Earth, Planets, and Space 60(?): 987–981.

Papazachos BC, Scordilis EM, Panagiotopoulos DG, Papazachos CB, and Karakaisis GF (2004) Global
relations between seismic fault parameters and moment magnitudes of earthquakes. Bulletin of the
Geological Society of Greece 36(?): 1482–1489.

Shaw BE (2013a) Earthquake surface slip-length data is fit by constant stress drop and is useful
for seismic hazard analysis, Bulletin of the Seismological Society of America 103(2A): 876-893.

Shaw BE (2013b) Appendix E: Evaluation of magnitude-scaling relationships and depth of rupture:
Recommendation for UCERF3, U.S. Geol. Surv. Open-File Rept. 2013-1165-E, and California Geol.
Surv. Special Rept. 228-E.

Somerville P, Collins N, Abrahamson NA, Graves R, and Saikia C (2001) Ground motion attenuation
relations for the Central and Eastern United States—Final report, June 30, 2001: Report to U.S.
Geological Survey for award 99HQGR0098, 38 p.

Strasser FO, Arango MC, and Bommer JJ (2010) Scaling of the source dimensions of interface and
intraslab subduction-zone earthquakes with moment magnitude: Seismological Research Letters
81(?): 941–950.

Wells DL and Coppersmith KJ (1994) New empirical relationships among magnitude, rupture length,
rupture width, and surface displacements: Bulletin of the Seismological Society of America,
84(?): 974–1002.

Working Group on California Earthquake Probabilities (WGCEP) (2003). Earthquake probabilities in
the San Francisco Bay region: 2002– 2031, U.S. Geol. Surv. Open-File Report 2003-214.

Youngs RR, Chiou B-SJ, Silva WJ, and Humphrey JR (1997) Strong ground motion attenuation
relationships for subduction zone earthquakes. Seismological Research Letters 68(?): 58–73.

---

[**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

# Hazard Calculation Code Versions

The static datasets of USGS NSHMs prior to 2014 were computed using Fortran (see
[nshmp-haz-fortran](https://github.com/usgs/nshmp-haz-fortran]). The static datasets for the
2014 Conterminous U.S. NSHM were computed using the Fortran codes and
[OpenSHA](https://opensha.org/) for the California portion of the model. The dynamic versions
of the 2008 and 2014 Conterminous U.S. models were then implemented in the 1st version of
[nshmp-haz](https://github.com/usgs/nshmp-haz) (on GitHub). This updated Java codebase uses XML
source models and supports the web services behind the dynamic calculations of the [Unified Hazard
Hazard Tool](https://earthquake.usgs.gov/hazards/interactive/) (UHT).

The 2nd version of nshmp-haz (this repository) supercedes prior codebases. The development of this
version involved a significant refactoring of both the computational code and source model format.
The source models are now defined using JSON, GeoJSON, and CSV files to better reflect the
underlying logic trees and support uncertainty analysis.

## Transitioning from _nshmp-haz_ v1 to v2

NSHMs are very detailed and migrating from one format to another is not trivial and prone to error.
Moreover, approximations (e.g. using 3.1415 for Pi rather than the the value built into most
languages) can yield different results. When multiple such small changes exist, deciphering what
si giving rise to differences in results can be challenging.

To document the transition from nshmp-haz v1 to v2, we here attach comparison maps at four return
periods (475, 975, 2475, and 10,000 year) for the 2018 Conterminous U.S. model. Maps are included
for PGA and 3 spectral periods ( 0.2 s, 1 s, and 5 s). There are no differences in the Central &
Eastern U.S. and differences in the WUS are <<1%. The difference in hazard in the vicinity of
Salt Lake City arises from the cluster models in nshmp-haz v1 not being able to consider the
additional epistemic uncertainty added to the the NGA-West2 ground motion models.

We continue to investigate the cause of other differences but they are small enough that we are
comfortable moving forward deploying nshmp-haz v2 codes and models to our public web services and
applications.

## Example map

![v1 to v2 differences and ratios](./images/comp_JSON_vs_XML_0p2-grid-20220216-BC.pdf)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

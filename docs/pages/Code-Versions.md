# Code Versions

The static datasets of USGS NSHMs prior to 2014 were computed using Fortran (see
[_nshmp-haz-fortran_](https://github.com/usgs/nshmp-haz-fortran])). The static datasets for the
2014 Conterminous U.S. NSHM were computed using the Fortran codes and
[OpenSHA](https://opensha.org/) (for the California portion of the model). The dynamic versions
of the 2008 and 2014 Conterminous U.S. models were then implemented in the 1st version of
[_nshmp-haz_](https://github.com/usgs/nshmp-haz) (on GitHub). This updated Java codebase uses XML
source models and supports the web services behind the dynamic calculations of the [Unified Hazard
Hazard Tool](https://earthquake.usgs.gov/hazards/interactive/) (UHT).

The 2nd version of _nshmp-haz_ (this repository) supercedes prior codebases. The development of this
version involved a significant refactoring of both the computational code and source model format.
The source models are now defined using JSON, GeoJSON, and CSV files to better reflect the
underlying logic trees and support uncertainty analysis.

## Transitioning from _nshmp-haz_ v1 to v2

NSHMs are very detailed and migrating from one format to another is not trivial and prone to error.
Moreover, approximations (e.g. using 3.1415 for Pi rather than the the value built into most
languages) can yield different results. When multiple such small changes exist, deciphering what
is giving rise to differences in results can be challenging.

To document the transition from _nshmp-haz_ v1 to v2, we here attach comparison maps at four return
periods (475, 975, 2475, and 10,000 year) for the 2018 Conterminous U.S. model. Maps are included
for PGA and 3 spectral periods ( 0.2 s, 1 s, and 5 s). There are no differences in the Central &
Eastern U.S. and differences in the WUS are <<1%. The difference in hazard in the vicinity of
Salt Lake City arises from the cluster models in _nshmp-haz_ v1 not being able to consider the
additional epistemic uncertainty added to the the NGA-West2 ground motion models.

We continue to investigate the cause of other differences but they are small enough that we are
comfortable moving forward deploying _nshmp-haz_ v2 codes and models to our public web services and
applications. This repository includes end-to-end tests for supported NSHMs that may be run
on demand.

[Download v1 to v2 difference and ratio maps](https://code.usgs.gov/ghsc/users/pmpowers/nshmp-haz/-/raw/code-version-doc-506/docs/pages/images/comp_JSON_vs_XML_0p2-grid-20220216-BC.pdf?inline=false)

## Example map: 0.2 s, 2475-yr

![0.2 s SA, 2475-yr](./images/JSON_vs_XML-SA0P2-2475.jpg)

---

## Related Pages

* [USGS Models](./USGS-Models.md#usgs-models)
  * [Model Editions](./Model-Editions.md#model-editions)
  * [Logic Trees & Uncertainty](./Logic-Trees-&-Uncertainty.md#logic-trees-&-uncertainty)
  * [Code Versions](./Code-Versions.md#code-versions)
* [**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

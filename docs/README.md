# Documentation: nshmp-haz

***nshmp-haz*** is a USGS developed software stack that supports probabilistic seismic hazard
(PSHA) and related analyses. It is maintained by the National Seismic Hazard Model Project
([NSHMP](https://earthquake.usgs.gov/hazards/)) within the U.S. Geological Survey's
([USGS](https://www.usgs.gov)) earthquake hazards program ([EHP](http://earthquake.usgs.gov)).

*nshmp-haz* supports high performance seismic hazard calculations required to generate detailed
maps over large areas and supports a variety of USGS web services and applications related to
seismic hazards research and the dissemination of hazard data. This documentation explains how
to use *nshmp-haz* as well as underlying model implementation details.

## Table of Contents

* [About the NSHMP](pages/About-the-NSHMP.md)
* [Building & Running](pages/Building-&-Running.md)
  * [Developer Basics](pages/Developer-Basics.md)
  * [Calculation Configuration](pages/Calculation-Configuration.md)
  * [Site Specification](pages/Site-Specification.md)
  * [Examples](/ghsc/nshmp/nshmp-haz/-/tree/master/etc/examples)
* [Hazard Model](pages/Hazard-Model.md)
  * [Model Structure](pages/Model-Structure.md)
  * [Model Files](pages/Model-Files.md)
  * [Source Types](pages/Source-Types.md)
  * [Magnitude Frequency Distributions (MFDs)](pages/Magnitude-Frequency-Distributions.md)
  * [Rupture Scaling Relations](pages/Rupture-Scaling-Relations.md)
  * [Ground Motion Models (GMMs)](pages/Ground-Motion-Models.md)
* [USGS Models](pages/USGS-Models.md)
  * [Model Editions](pages/Model-Editions.md)
  * [Logic Trees & Uncertainty](pages/Logic-Trees-&-Uncertainty.md)

## Other Pages & References

* [nshmp-lib](/ghsc/nshmp/nshmp-lib): USGS hazard modeling library
* [Functional PSHA](pages/Functional-PSHA.md)
* [Probabilistic Seismic Hazard Analysis, a Primer
  [PDF]](http://www.opensha.org/sites/opensha.org/files/PSHA_Primer_v2_0.pdf)
  by Edward Field  
* [An Introduction to Probabilistic Seismic Hazard Analysis
  [PDF]](http://web.stanford.edu/~bakerjw/Publications/Baker_(2015)_Intro_to_PSHA.pdf)
  by Jack Baker  
* [License](../LICENSE.md)
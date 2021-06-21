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

* [About the NSHMP](pages/about-the-nshmp)
* [Building & Running](building-&-running)
  * [Developer Basics](developer-basics)
  * [Calculation Configuration](calculation-configuration)
  * [Site Specification](site-specification)
  * [Examples](/ghsc/nshmp/nshmp-haz/-/tree/master/etc/examples)
* [Hazard Model](hazard-model)
  * [Model Structure](model-structure)
  * [Model Files](model-files)
  * [Source Types](source-types)
  * [Magnitude Frequency Distributions (MFDs)](magnitude-frequency-distributions-mfds)
  * [Rupture Scaling Relations](rupture-scaling-relations)
  * [Ground Motion Models (GMMs)](ground-motion-models-gmms)
* [USGS Models](usgs-models)
  * [Model Editions](model-editions)
  * [Logic Trees & Uncertainty](logic-trees-&-uncertainty)

## Other Pages & References

* [nshmp-lib](/ghsc/nshmp/nshmp-lib): USGS hazard modeling library
* [Functional PSHA](functional-psha)
* [Probabilistic Seismic Hazard Analysis, a Primer
  [PDF]](http://www.opensha.org/sites/opensha.org/files/PSHA_Primer_v2_0.pdf)
  by Edward Field  
* [An Introduction to Probabilistic Seismic Hazard Analysis
  [PDF]](http://web.stanford.edu/~bakerjw/Publications/Baker_(2015)_Intro_to_PSHA.pdf)
  by Jack Baker  
* [License](../LICENSE.md)

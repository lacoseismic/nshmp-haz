# Documentation: nshmp-haz

***nshmp-haz*** is a U.S. Geological Survey ([USGS](https://www.usgs.gov)) developed software stack
that supports probabilistic seismic hazard (PSHA) and related analyses. It is maintained by the
National Seismic Hazard Model Project ([NSHMP](https://earthquake.usgs.gov/hazards/)) within the
USGS's earthquake hazards program ([EHP](http://earthquake.usgs.gov)).

*nshmp-haz* supports high performance seismic hazard calculations required to generate detailed
maps over large areas and supports a variety of web services and applications related to
seismic hazards research and the dissemination of hazard data (see the
[NSHM Hazard Tool](https://earthquake.usgs.gov/nshmp/)). This documentation explains how to
use *nshmp-haz* as well as underlying model implementation details.

## Table of Contents

* [About the NSHMP](./pages/About-the-NSHMP.md)
* [Building & Running](./pages/Building-&-Running.md)
  * [Developer Basics](./pages/Developer-Basics.md)
  * [Calculation Configuration](./pages/Calculation-Configuration.md)
  * [Site Specification](./pages/Site-Specification.md)
  * [Examples](../../etc/examples) (or
    [on GitLab](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples))
* [Hazard Model](./pages/Hazard-Model.md)
  * [Model Structure](./pages/Model-Structure.md)
  * [Model Files](./pages/Model-Files.md)
  * [Source Types](./pages/Source-Types.md)
  * [Magnitude Frequency Distributions (MFDs)](./pages/Magnitude-Frequency-Distributions.md)
  * [Rupture Scaling Relations](./pages/Rupture-Scaling-Relations.md)
  * [Ground Motion Models (GMMs)](./pages/Ground-Motion-Models.md)
* [Implementation Details](./pages/Implementation-Details.md)
* [USGS Models](./pages/USGS-Models.md)
  * [Model Editions](./pages/Model-Editions.md)
  * [Logic Trees & Uncertainty](./pages/Logic-Trees-&-Uncertainty.md)

## Related Information

* [nshmp-lib](https://code.usgs.gov/ghsc/nshmp/nshmp-lib): USGS hazard modeling library
  * [nshmp-lib JavaDocs](https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/)
* [*nshmp-haz* License](../LICENSE.md)

## References

* [Functional PSHA](./pages/Functional-PSHA.md)
* [Probabilistic Seismic Hazard Analysis, a Primer
  [PDF]](https://opensha.org/resources/PSHA_Primer_v2_0.pdf)
  by Edward Field  
* [An Introduction to Probabilistic Seismic Hazard Analysis
  [PDF]](http://web.stanford.edu/~bakerjw/Publications/Baker_(2015)_Intro_to_PSHA.pdf)
  by Jack Baker  

---
![USGS logo](./pages/images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

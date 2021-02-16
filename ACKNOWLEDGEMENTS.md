# Acknowledgements

This software was developed to meet the U.S. Geological Survey's need for a
high-performance probabilistic seismic hazard (PSHA) engine capable of supporting
increasingly complex earthquake hazard models via web-services. It leverages and
benefits from many design and performance features found in
[OpenSHA](http://www.opensha.org) and the legacy [USGS fortran codes](/usgs/nshmp-haz-fortran),
both of which have been supported by the USGS and others over many years. In particular,
the Southern California Earthquake Center ([SCEC](https://www.scec.org)) has been instrumental
in the continued development of OpenSHA, which is in use worldwide.

This software is not a replacement for OpenSHA and does not provide a public API for PSHA
development. In the near future, `nshmp-haz` will likely be added as a dependency in OpenSHA
so that it's users may gain access to the most current USGS hazard models.

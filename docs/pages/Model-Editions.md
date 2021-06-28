# USGS Models: Editions

USGS hazard models are created and updated in response to user needs. In some cases a model is
updated in response to cyclic updates to the U.S. Building Code. In other cases, enough time has
passed that there have been significant advancements in the science, availability of relevant
data, or modeling procedures that warrant a model update. Every model the USGS produces is
associated with a USGS Open-File report or refereed journal article and supporting datasets.
However, layered on top of any given model release are bug-fixes and model improvements.

With the update to a new codebase, [nshmp-haz](https://github.com/usgs/nshmp-haz), the NSHMP has
adopted [semantic versioning](http://semver.org) to keep track of different hazard models. For
any given model region, the first number (or major version) corresponds to a particular release
or update year. For example, we consider the initial
[1996 conterminous U.S. NSHM](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#1996)
to be v1.0.0. The second number (or minor version) reflects updates to a particular release that
likely causes small changes to hazard values. The third number reflects non-hazard altering
changes; for instance, a change to the model file format that has no consequence on computed
hazard.

The USGS NSHMP is committed to supporting current and prior model releases in any given region
via web services. The table below provides a summary of all NSHM releases and their corresponding
version numbers.

Region | Year | Version | Static | Dynamic | Notes |
-------|:----:|:-------:|:------:|:-------:|-------|
Conterminous U.S. | [2014](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2014) | [v4.2.0](https://github.com/usgs/nshmp-model-cous-2014/releases/tag/v4.2.0)<sup>†</sup> | |:small_blue_diamond:| |
Conterminous U.S. | [2014](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2014) | [v4.1.4](https://github.com/usgs/nshmp-model-cous-2014/releases/tag/v4.1.4)<sup>†</sup> | |:small_blue_diamond:| |
Conterminous U.S. | [2014](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2014) | [v4.0.0](https://github.com/usgs/nshmp-haz-fortran/releases/tag/nshm2014r1) |:small_blue_diamond:| | ASCE7-16 |
Conterminous U.S. | [2008](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2008) | v3.3.3 | |:small_blue_diamond:| |
Conterminous U.S. | [2008](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2008) | [v3.2.0](https://github.com/usgs/nshmp-haz-fortran/releases/tag/nshm2008r3) |:small_blue_diamond:| | |
Conterminous U.S. | [2008](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2008) | v3.1.0 |:small_blue_diamond:| | ASCE7-10 |
Conterminous U.S. | [2008](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2008) | v3.0.0 | | | |
Conterminous U.S. | [2002](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2002) | v2.0.0 | | | |
Conterminous U.S. | [1996](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#1996) | v1.0.0 | | | |
Alaska            | [2007](https://earthquake.usgs.gov/hazards/hazmaps/ak/index.php#2007) | v2.1.0 | |:small_blue_diamond:| |
Alaska            | [2007](https://earthquake.usgs.gov/hazards/hazmaps/ak/index.php#2007) | v2.0.0 |:small_blue_diamond:| | ASCE7-10 |
Alaska            | [1999](https://earthquake.usgs.gov/hazards/hazmaps/ak/index.php#1999) | v1.0.0 | | | |
American Samoa    | [2012](https://earthquake.usgs.gov/hazards/hazmaps/islands.php#samoapacific) | v1.0.0 | | | |
Guam              | [2012](https://pubs.usgs.gov/of/2012/1015/) | v1.0.0 | | | |
Hawaii            | [2018](https://earthquake.usgs.gov/hazards/hazmaps/islands.php#hi) | v2.0.0 | | TBD | |
Hawaii            | [1998](https://earthquake.usgs.gov/hazards/hazmaps/islands.php#hi) | v1.1.0 | | TBD | |
Hawaii            | [1998](https://earthquake.usgs.gov/hazards/hazmaps/islands.php#hi) | v1.0.0 |:small_blue_diamond:| | ASCE7-10 |
Puerto Rico & <br/> U.S. Virgin Islands | [2003](https://earthquake.usgs.gov/hazards/hazmaps/islands.php#prvi) | v1.0.0 | | | |

<sup>†</sup> __Note on the 2014 Conterminous U.S. NSHM:__ Initial publication of the
[2014 model](https://earthquake.usgs.gov/hazards/hazmaps/conterminous/index.php#2014) included
data to support updates to the U.S. Building Code, specifically hazard curves for peak ground
acceleration (PGA), and 0.2 and 1.0 second spectral accelerations, all at a BC boundary site
class with Vs30 = 760 m/s. Some time later, the model was deployed to the Unified Hazard Tool
(UHT) and included support in the Wester U.S. for calculations at sites other than Vs30 = 760 m/s,
consistent with dynamic calculations using the 2008 model. Subsequently, we updated the 2014
model with [addional periods and site classes](https://pubs.er.usgs.gov/publication/ofr20181111).
Doing so required dropping several ground motion models (GMMs) and a redistribution of logic-tree
weights. Specifically, the Idriss (2014) model is inappropriate for use at soft soil sites and
the Atkinson & Boore (2003) model does not support long periods (see the
[open-file report](https://pubs.er.usgs.gov/publication/ofr20181111) for more information).
Moving forward, we will continue to include the original dynamic version of the 2014 model
(v4.1.4) in the UHT. However, we recommend that users consider the updated model (4.2.0).

## Static vs. Dynamic

Historically, the USGS NSHMP has produced static datasets of hazard curves that accompany the
'official' release or update to a model. In the context of providing interactive web services,
such static datasets can be quickly retreived and provide most users with the data they seek.
More complex analyses, such as deaggregations, require that a complete hazard calculation be
performed on demand. Historically, USGS deaggregation services were provided for particular model
years and regions, each located at a unique web address and supported by a unique codebase.
However, it has proven too difficult to maintain numerous isolated services, and we therefore
developed a single codebase that supports all current and prior models.

Moreover, as time goes by, there may be more customization options we want to expose to users.
However, with each additional level of customization, it quickly becomes too difficult to produce
and version corresponding static datasets. We therefore identify model versions that support
deaggregations and other calculations as 'dynamic'. At present, only the most current versions
of a particular model region and year are supported via 'dynamic' calculations.

In practice, this leads to results produced by 'dynamic' caluculations being somewhat different
than those stored in static datasets of prior model minor versions, although usually not by much.
The release notes for each model version detail the changes that give rise to changes in hazard
between between versions. There are also differences that arise from different modeling
assumptions between past and current codebases that are detailed below.

It is important for users to know which edition they should be using. For instance if one is
bound to use those values adopted by the U.S. building code, one of the editions marked `ASCE7-*`
is probably most appropriate. However, if one is bound to use the most up to date source model,
one of the dynamic editions is likely better.

Dynamic editions are supported through web-services provided by the `nshmp-haz-ws` library
(this repository). Static editions are supported via a separate set of services. Both are
documented on the [web services](web-services) page.

## Region specific changes

Changes between editions in model regions are documented in the release notes of the individual
model repositories.

TODO: Update model links

* [Conterminous US (2014)](/usgs/nshmp-model-cous-2014/wiki)  
* [Conterminous US (2008)](/usgs/nshmp-model-cous-2008/wiki)  
* [Alaska (2007)](/usgs/nshmp-model-ak-2007/wiki)  

---

[**Documentation Index**](docs/README.md)

---
![USGS logo](images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

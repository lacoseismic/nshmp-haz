# Source Types

Earthquake source representations take a variety of forms in a NSHM. They may be well-defined
finite fault surfaces or distributed grids of more general geometry with uniform or spatially
varying rates, as are used to represent earthquakes of unknown location. Both classes of geometry
are used in all tectonic settings. For instance, finite source geometries are used for crustal
faults in active and stable continental regions, as well as to define the ruptures surfaces of
possible subduction interface earthquakes. Distributed grids of sources are commonly used to
represent models of smoothed seismicity in both crustal and subduction intraslab settings but
may also be used for fault zones where there is a history of large earthquakes but the fault
geometry itself is unknown or very poorly defined.

[[_TOC_]]

Source models for use with *nshmp-haz* are defined using [JSON](https://www.json.org) and
[GeoJSON](https://geojson.org). *nshmp-haz* makes determinations about how to represent a source
based on a GeoJSON geometry type in conjunction with supporting JSON configuration files. Example
source configuration files, `*-config.json`, are provided with each source type description.
Configuration files must be fully specified with `null` JSON member values used to specify 'do
nothing' where appropriate. Any configuration member value in ALL_CAPS indicates the value is one
of a fixed number of options commonly referred to as an enum.

__Note on Coordinates:__ *nshmp-haz* supports longitude and latitude values in the closed ranges
`[-360°‥360°]` and `[-90°‥90°]`. Note, however, that mixing site and/or source coordinates across
the antimeridian (the -180° to 180° transition) will yield unexpected results. For Pacific models
and calculations, always use positive or negative longitudes exclusively.

## Grid Sources

Grid, or smoothed seismicity, sources are used to represent earthquakes that may occur but that
are not, as yet, associated with a known active fault. Such source models are typically developed
through the declustering and smoothing of earthquake catalogs given that past earthquakes are good
indicators of future activity. Declustering removes spatial bias from aftershock sequences.

Grid sources are modeled as uniformely dsitributed grids of points where pseudo faults are used
for rupture geometry. Truncated Gutenberg-Richter MFDs are used to model grid source rupture
magnitudes and rates with logic trees of varying a-value, b-value and maximum magnitude. For grid
sources the relative rate at each grid node is defined using a spatial PDF (see notes on
spatial PDFs, [below](#spatial-pdfs)). When realizing each source the spatial PDF value is scaled
by each regional rate in a rate-tree.

Grid sources are represented in a model using a logic tree with a `rupture-sets.json` defining the
ruptures on each branch.  Because gridded seismicity models may be governed by regionally
varying MFD properties (e.g. `mMax`), rupture sets for grids are defined in a JSON array.

**rupture-sets.json**: Defines an array of one or more rupture sets. Multiple rupture sets are
used to model regional differences in MFD properties such as maximum magnitude. The `feature`
member points to the ID of a geojson feature (in the `grid-sources/features` directory) that
defines the bounds of the gridded seismicity source. A grid rupture set `mfd-tree` is never
defined inline and always points to a tree in a
[`mfd-map`](./Magnitude-Frequency-Distributions.md#mfd-construction).

```json
[
  {
    "name": "Summit Grid Source",
    "id": 5111,
    "feature": 5100,
    "mfd-tree": "summit-deep-r85",
    "spatial-pdf": "summit-deep-r85-pdf.csv"
  }
]
```

**grid-config.json**: A `grid-depth-map` defines a mapping of magnitude ranges to logic trees of
depth distributions. The map can use arbitrary names as keys, but the magnitude ranges defined by
each member must be non-overlapping. The magnitude ranges are interpreted as closed (inclusive) –
open (exclusive), e.g. [mMin..mMax). `maxDepth` constrains the maximum depth of any pseudo fault
rupture representations.

```json
{
  "grid-spacing": 0.1,
  "point-source-type": "FINITE",
  "rupture-scaling": "NSHM_POINT_WC94_LENGTH",
  "max-depth": 22.0,
  "focal-mech-tree": [
    { "id": "STRIKE_SLIP", "weight": 1.0 }
  ],
  "grid-depth-map": {
    "small-magnitude": {
      "mMin": 4.5,
      "mMax": 6.5,
      "depth-tree": [
        { "id": "5 km", "weight": 1.0, "value": 5.0 }
      ]
    },
    "large-magnitude": {
      "mMin": 6.5,
      "mMax": 9.5,
      "depth-tree": [
        { "id": "1 km", "weight": 1.0, "value": 1.0 }
      ]
    }
  }
}
```

### Spatial PDFs

Grid source spatial PDFs are stored in `grid-sources/grid-data/`. The PDFs are stored in
comma-delimited files that are usually sorted by increasing longitude then latitude (lower-left
to upper-right). While most gridded rate files contain columns of longitude, latitude, and pdf,
some may contain depth values (intraslab sources), maximum magnitude caps, or other values. Scaled
spatial PDFs are the preferred approach to modeling regional rate variations, however it is also
possible to define explicit MFDs at each grid node. To do so, the `spatial-pdf` member
of a **rupture-sets.json** is replaced with `grid-mfds`. See
`2018 CONUS NSHM > active-crust > grid-sources` for examples of both approaches.

## Zone Sources

Zone (or area) sources are similar to [Grid Sources](#grid-sources) except that a single rate
applies over a polygonal region with rate of individual point sources proportionally scaled to the
relative area represented by each grid node. Zone sources are represented using GeoJSONs
directly.

```json
{
  "type": "Feature",
  "id": 720,
  "geometry": {
    "type": "Polygon",
    "coordinates": [[
      [-86.72464, 39.34245],
      [-87.87724, 37.67532],
      [-88.10853, 37.69013],
      [-88.25009, 37.67544],
      [-88.42465, 37.70361],
      [-88.68714, 37.75289],
      [-87.42964, 39.67601],
      [-86.72464, 39.34245]
    ]]
  },
  "properties": {
    "name": "Fault Zone Name",
    "state": ["IL", "IN"],
    "rate": "wabash.csv",
    "strike": 26.639674,
    "mfd-tree": [
      { "id": "M1", "weight": 0.05, "value": { "type": "SINGLE", "properties": { "m": 6.75, "rate": 0.001 }}},
      { "id": "M2", "weight": 0.25, "value": { "type": "SINGLE", "properties": { "m": 7.00, "rate": 0.001 }}},
      { "id": "M3", "weight": 0.35, "value": { "type": "SINGLE", "properties": { "m": 7.25, "rate": 0.001 }}},
      { "id": "M4", "weight": 0.35, "value": { "type": "SINGLE", "properties": { "m": 7.50, "rate": 0.001 }}}
    ]
  }
}
```

**zone-config.json:** Zone source model configuration is identical to a grid source configuration.

```json
{
  "grid-spacing": 0.1,
  "point-source-type": "FIXED_STRIKE",
  "rupture-scaling": "NSHM_POINT_WC94_LENGTH",
  "max-depth": 22.0,
  "focal-mech-tree": [
    { "id": "STRIKE_SLIP", "weight": 1.0 }
  ],
  "grid-depth-map": {
    "all": {
      "mMin": 4.5,
      "mMax": 9.5,
      "depth-tree": [
        { "id": "5 km", "weight": 1.0, "value": 5.0 }
      ]
    }
  }
}
```

## Fault Sources

Finite fault source representation. The geometry, properties and rupture MFDs of a fault source
are defined by one or more GeoJSONs and associated configuration. Depending on the complexity of
the source, it may be represented using a single GeoJSON or result from stitching together
multiple GeoJSONs (see note on fault section stitching, [below](#fault-section-stitching)). If a
fault source is represented with a logic tree then a `rupture-set.json` defines the ruptures for
each branch. Depending on the MFDs and scaling relations used to determine a rupture size, some
ruptures may fill the entire source model while smaller events are modeled as 'floating' ruptures;
they occur in multiple locations on the fault surface with appropriately scaled rates. MFDs
associated with finite fault models may be explicitly defined or derived from slip rates.
Fault rupture rates may be modeled using explicitly defined MFDs or logic trees of slip rate.

**fault-source.geojson**: Defines the geometry and properties of a single source. In the example
below the presence of a `rate-map` property indicates MFDs should be constructed from the supplied
slip rates and using the weights defined in a `rate-tree.json`.

```json
{
  "type": "Feature",
  "id": 2529,
  "geometry": {
    "type": "LineString",
    "coordinates": [
      [-122.9297, 45.72241],
      [-122.86196, 45.65791],
      [-122.77095, 45.58227],
      [-122.69764, 45.52423],
      [-122.67841, 45.51561],
      [-122.62029, 45.43556],
      [-122.58902, 45.40475],
      [-122.57195, 45.39478],
      [-122.56611, 45.38561],
      [-122.5405, 45.37375]
    ]
  },
  "properties": {
    "name": "Portland Hills",
    "state": "OR",
    "upper-depth": 0.0,
    "lower-depth": 15.0,
    "dip": 60.0,
    "rake": 90.0,
    "rate-type": "VERTICAL_SLIP",
    "length": 49.72484,
    "rate-map": {
      "BIRD": {
        "rate": 0.07
      },
      "GEO": {
        "rate": 0.1
      },
      "ZENG": {
        "rate": 0.15
      }
    }
  }
}
```

**fault-config.json**: Controls the point spacing on a gridded surface used to realize the fault
geometry as well as define the models to use for magnitude scaling and rupture floating. Dip
variations and an associated slip-rate scaling model are also supported for normal faults.

```json
{
  "surface-spacing": 1.0,
  "rupture-scaling": "NSHM_FAULT_WC94_LENGTH",
  "rupture-floating": "NSHM",
  "dip-slip-model": "FIXED",
  "normal-fault-dip-tree": null
}
```

**rupture-set.json**: When a fault source is represented with a logic tree a
`rupture-set.json` defines the ruptures for each branch. A rupture set _may_ also define custom
properties and _may_ also contain a `sections` member that defines the fault sections for the
rupture set (see note on fault section stitching, [below](#fault-section-stitching)).

```json
{
  "name": "New Madrid - USGS (center)",
  "id": 3023,
  "sections": [3020, 3021, 3022],
  "mfd-tree": "usgs-hi-mag",
  "properties": {
    "state": "MO",
    "width": 15.0
  }
}
```

### Fault Section Stitching

When multiple sections are defined for a rupture, the ruptures must be defined in an order that
preserves the U.S. structural geology right-hand-rule. When stitched together, repeated locations
at the enpoints of adjacent sections, if present, are removed. The properties of the first section
govern the properties of the stitched fault, however, a rupture-set _may_ include a properties
member, the contents of which will override the properties of the first stitched section. Although
it would be better to have geometric properties of stitched sections be calculated dynamically,
the current approach preserves support for past models. A rupture-set _may_ also include a
`coordinates` member that can be used to represent a smoothed trace geometry where stitched
sections do not share common endpoints.

### Fault Cluster Sources

These specialized fault sources are composed of two or more fault sources that rupture
independently but very closely spaced in time. Ground motions from cluster sources are modeled
as the joint probability of exceeding ground motions from each independent event. A source in
a cluster may only have an mfd-tree composed of `Mfd.Type.SINGLE` MFDs and the mfd-trees must
match across all sources in a cluster (i.e. each mfd-tree has the same IDs and weights).

**cluster-set.json** A specialized type of rupture set, this file defines the array of fault
rupture sets that make up a 'cluster'. As with fault sources, the nested rupture sets in a cluster
set _may_ define `properties` and `sections` members.

```json
{
  "name": "New Madrid - USGS (center, center-south)",
  "id": 3025,
  "rupture-sets": [
    {
      "name": "USGS (center, center)",
      "id": 3021,
      "mfd-tree": "usgs-hi-mag",
      "properties": {
        "width": 15.0
      }
    },
    {
      "name": "USGS (center, south)",
      "id": 3022,
      "mfd-tree": "usgs-hi-mag",
      "width": 15.0
    }
  ]
}
```

### Fault System Sources

This specialized fault source type supports inversion based rupture rate estimates, or solutions,
on a fault network such as that used for UCERF3 in the 2014 and 2018 NSHMs for the conterminous
U.S. Fault system source sets require three files: `rupture_set.json`, `sections.geojson`, and
`ruptures.csv` that are placed together within folders defining branches of a fault system
logic tree. Note that system sources _may_ have complementary gridded seismicity source models
with matching logic trees.

**rupture-set.json**: Provides identifying information for the ruptures defined in the adjacant
sections and ruptures files.

**sections.geojson**: defines a feature collection of the fault sections in a fault network.
Because fault sections are derived by dividing larger faults into subsections, section features
contain several properties that differ from standalone fault section source models (e.g.
`dip-direction`).

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "id": 310000,
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [-117.74953, 35.74054],
          [-117.76365, 35.81038]
        ]
      },
      "properties": {
        "name": "Airport Lake (0)",
        "state": "CA",
        "index": 0,
        "parent-id": 1,
        "upper-depth": 0.0,
        "lower-depth": 13.0,
        "dip": 50.0,
        "dip-direction": 89.459,
        "aseismicity": 0.1
      }
    },
    ...
  ]
}
```

**ruptures.csv**: Defines the properties of every rupture. The last column in a rupture file
defines the ordered array of participating fault section IDs using the shorthand
`1127:1131-2411:2412`. Colons denote continous ranges of sections and hyphens denote breaks.

## Subduction Interface Sources

Subduction interface sources are currently specified the same way as
[fault sources](#fault-sources) in crustal tectonic settings. Source model properties are defined
using an `interface-config.json` file.

## Subduction Intraslab Sources

Subduction intraslab sources are currently specified the same way as
[grid sources](#grid-sources) in curstal tectonic settings. Source model properties are defined
using an `slab-config.json` file.

---

## Related Pages

* [Hazard Model](./Hazard-Model.md#hazard-model)
  * [Model Structure](./Model-Structure.md#model-structure)
  * [Model Files](./Model-Files.md#model-files)
  * [Source Types](./Source-Types.md#source-types)
  * [Magnitude Frequency Distributions (MFDs)](./Magnitude-Frequency-Distributions.md#magnitude-frequency-distributions)
  * [Rupture Scaling Relations](./Rupture-Scaling-Relations.md#rupture-scaling-relations)
  * [Ground Motion Models (GMMs)](./Ground-Motion-Models.md#ground-motion-models)
* [**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

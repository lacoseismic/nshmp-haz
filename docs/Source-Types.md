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

Example source
configuration files are provided with each [source type](source-types) description. Configuration
files must be fully specified with `null` JSON member values used to specify 'do nothing' where
appropriate.

Source models for use with *nshmp-haz* are defined using [JSON](https://www.json.org) and [GeoJSON](https://geojson.org). *nshmp-haz* makes determinations about how to represent a source based on a GeoJSON geometry type in conjunction with supporting JSON configuration files.

Values in source model files in ALL_CAPS generally map to enum types, [for example](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/gmm/Gmm.html).

__Note on Coordinates:__ *nshmp-haz* supports longitude and latitude values in the closed ranges `[-360°‥360°]` and `[-90°‥90°]`. Note, however, that mixing site and/or source coordinates across the antimeridian (the -180° to 180° transition) will yield unexpected results. For Pacific models and calculations, always use positive or negative longitudes exclusively.

### Model Initialization Parameters

Model initialization parameters *must* be supplied; there are no default values. In addition, these parameters may *not* be overridden once a model has been initialized in memory. However, one can configure parts of a model differently, [for example](/usgs/nshmp-model-cous-2014/blob/master/Western%20US/Interface/config.json).

Parameter | Type | Notes |
--------- | ---- | ----- |
__`model`__ |
&nbsp;&nbsp;&nbsp;`.name`               |`String`  |
&nbsp;&nbsp;&nbsp;`.surfaceSpacing`     |`Double`  |(in km)
&nbsp;&nbsp;&nbsp;`.ruptureFloating`    |`String`  |[RuptureFloating](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/eq/fault/surface/RuptureFloating.html)
&nbsp;&nbsp;&nbsp;`.ruptureVariability` |`Boolean` |
&nbsp;&nbsp;&nbsp;`.pointSourceType`    |`String`  |[PointSourceType](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/eq/model/PointSourceType.html)
&nbsp;&nbsp;&nbsp;`.areaGridScaling`    |`String`  |[AreaSource.GridScaling](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/eq/model/AreaSource.GridScaling.html)


### Outline

* [Area Sources](#area-sources)
* [Cluster Sources](#cluster-sources)
* [Fault Sources](#fault-sources)
* [Grid Sources](#grid-sources)
* [Interface Sources](#interface-sources)
* [Slab Sources](#slab-sources)
* [System Sources](#system-sources)

* Basic source types
* Fault Modeling Approaches
  * cluster models are a source model specialization; currently used in both cratonic and active crustal environments.
  * system

### Zone Sources

Zone (or area) sources are similar to [Grid Sources](#grid-sources) except that a single MFD
applies to an entire area with rates proportionally scaled to the relative area represented by
each grid node.

**zone-config.json:** Source model properties

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

### Cluster Sources

Cluster sources are composed of two or more fault sources that rupture independently but very
closely spaced in time. Ground motions from cluster sources are modeled as the joint probability
of exceeding ground motions from each independent event.

A source in a cluster may only have an mfd-tree composed of `Mfd.Type.SINGLE` MFDs and the
mfd-trees must matching across all sources in a cluster (i.e. each mfd-tree has the same IDs
and weights).

```xml
<!-- deprecated XML format -->
<?xml version="1.0" encoding="UTF-8"?>
<ClusterSourceSet name="Source Set Name" weight="1.0">

    <!-- (optional) Settings block for any data that applies to all
         sources. -->
    <Settings>

        <!-- (optional) The reference MFD to use. Note: Cluster sources
             only support SINGLE MFDs at this time. -->
        <DefaultMfds>
            <IncrementalMfd type="SINGLE" weight="1.0" id="name"
                rate="0.002" floats="false" m="0.0" />
        </DefaultMfds>

        <!-- Although not used, a rupture scaling model is required
             to initalize the fault sources nested in each cluster. --> 
        <SourceProperties ruptureScaling="NSHM_FAULT_WC94_LENGTH" />

    </Settings>

    <!-- Sources must follow Settings ... -->
    <Cluster name="Cluster Source Name" id="0" weight="0.2">
        <Source name="Fault Source Name" id="0">

            <!-- Specify MFDs; only SINGLE is supported -->
            <IncrementalMfd type="SINGLE" weight="0.2" id="name" m="6.6" />
            <IncrementalMfd type="SINGLE" weight="0.5" id="name" m="6.9" />
            <IncrementalMfd type="SINGLE" weight="0.3" id="name" m="7.3" />

            <!-- Then geometry ... -->
            <Geometry dip="45.0" rake="0.0" width="14.0">

                <!-- Trace must follow right-hand rule. -->
                <!-- Individual locations specified by whitespace
                     separated tuples of longitude,latitude,depth
                     (NO SPACES); same as KML <coordintes/> format. -->
                <Trace>
                     -117.0,34.0,0.0
                     -117.1,34.1,0.0
                     -117.3,34.2,0.0
                     ...
                </Trace>
            </Geometry>
        </Source>
    </Cluster>

    <!-- Add more sources ... -->
    <Cluster />
    ...
</ClusterSourceSet>
```

### Finite Fault Sources

Fault source representation. This class wraps a model of a fault geometry and
a list of magnitude frequency distributions that characterize how the fault
might rupture (e.g. as one, single geometry-filling event, or as multiple
smaller events) during earthquakes. Smaller events are modeled as 'floating'
ruptures; they occur in multiple locations on the fault surface with
appropriately scaled rates.

Many fault-based earthquake sources are strightforward to model using a single geojson file that 

```json
{
  ...TODO
}
```

MFDs associated with finite fault models may be explicitely defined or or derived from slip rates.

```json
"magScalingTree": [
    { "id": "WC94_LENGTH", "weight": 1.0 }
],
"dipVariants": [
    {
      "dipTree": {
        "type": "THREE_POINT",
        "applyTo" : "NORMAL",
        "label": "δ", --> label e.g. δ35 δ50 δ65
        "offset": 15.0
      }
    }
]
```

##### Geodetic slip variants

```json
"rateModels": [
    {
      "id": "BIRD",
      "rake": -90.0,
      "type": "SLIP",
      "value": 0.03
    },
    {
      "id": "GEO",
      "rake": -90.0,
      "type": "SLIP",
      "value": 0.018
    },
    {
      "id": "ZENG",
      "rake": -90.0,
      "type": "SLIP",
      "value": 0.04
    }
]

"rateModels": [
    {
      "id": "A_PRIORI",
      "rake": -90.0,
      "type": "PROBABILITY_OF_ACTIVITY",
      "value": 0.04
    }
]
```

##### `fault-config.json`

```json
{
  "surface-spacing": 1.0,
  "rupture-scaling": "NSHM_FAULT_WC94_LENGTH",
  "rupture-floating": "NSHM",
  "normal-fault-dip-tree": [
    { "id": "δ+15°", "weight": 0.2, "value": 15.0 },
    { "id": "δ", "weight": 0.6, "value": 0.0 },
    { "id": "δ-15°", "weight": 0.2, "value": -15.0 }
  ]
}
```


### Fault Zone (Area) Sources

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

### Fault sources with complex logic trees

It is not uncommon for rupture models to be defined by complex logic trees. In this instance, a `rupture

```xml
<!-- deprecated XML format -->
<?xml version="1.0" encoding="UTF-8"?>
<FaultSourceSet name="Source Set Name" weight="1.0">

    <!-- (optional) Settings block for any data that applies to all
         sources. -->
    <Settings>

        <!-- (optional) The reference MFDs to use. -->
        <DefaultMfds>
            <IncrementalMfd type="SINGLE" weight="0.6667" id="name"
                rate="0.0" floats="false" m="0.0" />
            <IncrementalMfd type="GR" weight="0.3333" id="name"
                a="0.0" b="0.8" dMag="0.1" mMin="5.0" mMax="7.0" />
            ...
        </DefaultMfds>

        <!-- (optional) A magnitude uncertainty model that will be
             applied to every source:
               - <Epistemic/> varies mMax and scales variant rates by
                 the supplied weights; it is only ever applied to SINGLE
                 and GR MFDs.
               - 'cutoff' is magnitude below which uncertainty will be
                 disabled.
               - <Aleatory/> applies a (possibly moment-balanced) ±2σ
                 Gaussian distribution to mMax; it is only ever applied
                 to SINGLE MFDs (possibly in conjunction with epistemic).
               - 'count' is the number of magnitude bins spanned by
                 the distribution.
               - <Aleatory/> or '<Epistemic/>', or the entire block
                 may be omitted. -->
        <MagUncertainty>
            <Epistemic cutoff="6.5" 
                deltas="[-0.2, 0.0, 0.2]" weights="[0.2, 0.6, 0.2]" />
            <Aleatory cutoff="6.5" 
                moBalance="true" sigma="0.12" count="11" />
        </MagUncertainty>

        <SourceProperties ruptureScaling="NSHM_FAULT_WC94_LENGTH" />

    </Settings>

    <!-- Sources must follow Settings ... -->
    <Source name="Fault Source Name" id="0">

        <!-- Specify MFDs ... 
               - at a minimum, 'type' must be defined, assuming
                 reference MFDs are present. -->
        <IncrementalMfd type="SINGLE" rate="1.0" m="7.4" />
        <IncrementalMfd type="GR" a="1.0e-2" dMag="0.1" mMax="7.4" />

        <!-- Then geometry ... -->
        <Geometry depth="1.0" dip="45.0" rake="0.0" width="14.0">

            <!-- Trace must follow right-hand rule. -->
            <!-- Individual locations specified by whitespace separated
                 tuples of longitude,latitude,depth (NO SPACES); same
                 as KML <coordintes/> format. -->
            <Trace>
                -117.0,34.0,0.0
                -117.1,34.1,0.0
                -117.3,34.2,0.0
                ...
            </Trace>
        </Geometry>
    </Source>

    <!-- Add more sources ... -->
    <Source />
    ...
</FaultSourceSet>
```

### Grid Sources

expectation that all MFDs are some flavor of GR*

Always floats when used for a fault source; never floats for grid sources. 'a' is the incremental log10(number of M=0 events).

grid polygons generally offset (expanded) due to includes() operations on grid nodes.

grid sources must define a rate-tree, even if it is a singleton.

ANy grid sources/rates defined outside grid polygon are skipped.

No `mfd-config` for grid, slab, or zone sources at this time. (implies we don't use epi or aleatory variability in grid-based source MFDs)

...tapered GR  Currently only used for grid sources.

```xml
<!-- deprecated XML format -->
<?xml version="1.0" encoding="UTF-8"?>
<GridSourceSet name="Source Set Name" weight="1.0" id="0">

    <!-- (optional) Settings block for any data that applies to all
         sources. -->
    <Settings>

        <!-- (optional) The reference MFDs to use; although optional,
             using reference MFDs greatly reduces grid source file
             sizes. -->
        <DefaultMfds>
            <IncrementalMfd type="GR" weight="1.0" id="name"
                a="0.0" b="0.8" dMag="0.1" mMax="7.0" mMin="5.0" />
            <IncrementalMfd type="INCR" weight="1.0" id="name"
                mags="[5.05, 5.25, 5.45, 5.65, 5.85, 6.05, 6.25, 6.45]"
                rates="[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]" />
            ...
        </DefaultMfds>

        <!-- Grid sources require attitional information about the 
             distribution of focal mechanisms and depths to use:
               - 'magDepthMap' is a ';' separated list cutoff magnitudes
                 mapped to depths and associated weights. In the example
                 below events of M<6.5 are located at a depth of 5 km,
                 with a full weight of 1. The [depth:weight] mapping may
                 contain multiple, ',' separated values, e.g.
                 [6.5::[5.0:0.8,1.0:0.2], ...].
               - 'maxDepth' constrains the maximum depth of any finite
                 point source representations.
               - 'focalMechMap' is a ',' separated list of focal
                 mechanism identifiers and associated wieghts.
               - In both maps above, weights must sum to 1.
               - Use 'NaN' for unknown strike. Note that if a strike
                 value is defined, sources will be implementated as
                 FIXED_STRIKE and any configuration settings will be
                 ignored. -->
        <SourceProperties 
            magDepthMap="[6.5::[5.0:1.0]; 10.0::[1.0:1.0]]"
            maxDepth="14.0"
            focalMechMap="[STRIKE_SLIP:0.5,NORMAL:0.0,REVERSE:0.5]"
            ruptureScaling="NSHM_POINT_WC94_LENGTH"
            strike="120.0" />
    </Settings>

    <!-- Nodes are specialized <IncrementalMfd/> elements that specify
         the location of individual grid sources and have the necessary
         attributes to define the MFD for the source. -->
    <Nodes>
        <Node type="GR" a="0.0823" mMax="7.2">-119.0,34.0,0.0</Node>
        <Node type="GR" a="0.0823" mMax="7.1">-119.1,34.0,0.0</Node>
        <Node type="GR" a="0.0823" mMax="6.8">-119.2,34.0,0.0</Node>
        <Node type="GR" a="0.0823" mMax="7.1">-119.3,34.0,0.0</Node>
        <Node type="SINGLE" rates="[1.0e-2, ...]">-119.4,34.0,0.0</Node>
        <Node type="SINGLE" rates="[1.0e-2, ...]">-119.5,34.0,0.0</Node>
        <Node type="GR" a="0.0823" mMax="6.9">-119.3,34.0,0.0</Node>
        ...
    </Nodes>
</GridSourceSet>
```

##### `grid-config.json`

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

A `grid-depth-map` defines a mapping of magnitude ranges to logic trees of depth distributions. The map can use arbitrary names as keys, but the magnitude ranges defined by each member must be non-overlapping. The magnitude ranges are interpreted as closed (inclusive) – open (exclusive), e.g. [mMin..mMax).

## Grid Data Files (*.csv)

TODO move to grid source type??

Gridded data are stored in comma-delimited files that are usually sorted by increasing longitude
then latitude (lower-left to upper-right). While most gridded rate files contain columns of
longitude, latitude, and rate, some may contain depth values (intraslab sources), maximum
magnitude caps, or other values.


### Subduction Interface Sources

```xml
<!-- deprecated XML format -->
<?xml version="1.0" encoding="UTF-8"?>
<SubductionSourceSet name="Source Set Name" weight="1.0">

    <!--  See Fault Sources for 'Settings' examples. -->
    <Settings />

    <!-- Sources must follow Settings ... -->
    <Source name="Subduction Source Name" id="0">

        <!-- Specify MFDs ... -->
        <IncrementalMfd type="SINGLE" weight="1.0" id="name"
            rate="1.0" m="8.2" />

        <!-- Then geometry ... -->
        <Geometry rake="90.0">

            <!-- As with Fault Sources, trace must follow right-hand
                 rule. -->
            <!-- Individual locations specified by whitespace separated 
                 tuples of longitude,latitude,depth (NO SPACES); same as
                 KML <coordintes/> format. -->
            <Trace>
                -124.7,41.0,0.0
                -124.6,44.0,0.0
                -124.5,47.0,0.0
                ...
            </Trace>

            <!-- (optional) Subduction sources may specify a lower trace,
                 also following the right-hand-rule. If a lower trace
                 is NOT defined, the parent geometry element must include
                 'depth', 'dip', and 'width' attributes in the same manner
                 as a fault source. -->
            <LowerTrace>
                -124.5,41.0,0.0
                -124.4,44.0,0.0
                -124.3,47.0,0.0
                ...
            </LowerTrace>
        </Geometry>
    </Source>

    <!-- Add more sources ... -->
    <SubductionSource />
    ...

</SubductionSourceSet>
```

##### `interface-config.json`

```json
{
  "surface-spacing": 5.0,
  "rupture-scaling": "NSHM_SUB_GEOMAT_LENGTH",
  "rupture-floating": "STRIKE_ONLY"
}
```

### Subduction Intraslab Sources

Subduction intraslab sources are currently specified the same way as [Grid Sources](#grid-sources).

##### `slab-config.json`

```json
{
  "grid-spacing": 0.1,
  "point-source-type": "FINITE",
  "rupture-scaling": "NSHM_SUB_GEOMAT_LENGTH",
  "focal-mech-tree": [
    { "id": "STRIKE_SLIP", "weight": 1.0 }
  ]
}
```

### System Sources

Fault system source sets require three files:

* `fault_sections.xml`
* `fault_ruptures.xml`
* `grid_sources.xml`

that are placed together within a _source group_ folder. Fault system source sets represent a single logic-tree branch or an average over a group of branches and has a gridded (or smoothed seismicity) source component that is coupled with the fault-based rates in the model.

`fault_sections.xml` defines the geometry of a fault network as a set of indexed fault sections:

```xml
<!-- deprecated XML format -->
<?xml version="1.0" encoding="UTF-8"?>
<SystemFaultSections name="Source Set Name">

    <!-- Specify section 'index' and 'name' -->
    <Section index="0" name="Section Name">

        <!-- Specify section geometry -->
        <Geometry aseis="0.1" dip="50.0" dipDir="89.459" 
                  lowerDepth="13.0" upperDepth="0.0">

            <!-- Unlike Fault Sources, trace does not need to follow
                 right-hand rule as 'dipDir' is supplied above. -->
            <!-- Individual locations specified by whitespace separated
                 tuples of longitude,latitude,depth (NO SPACES); same as 
                 KML <coordintes/> format. -->
            <Trace>
                -117.75,35.74,0.00
                -117.76,35.81,0.00
            </Trace>
        </Geometry>
    </Section>

    <!-- Add more sections ... -->
    <Section />
</IndexedFaultSections>
```

`fault_ruptures.xml` defines the geometry of fault sources, referencing fault sections by index:

```xml
<!-- deprecated XML format -->
<?xml version="1.0" encoding="UTF-8"?>
<SystemSourceSet name="Source Set Name" weight="1.0">

    <!-- <Settings/> block may be included; see Fault Sources and
         Grid Sources for examples. -->

    <!-- Sources must follow Settings ...
            - indexed fault sources do not require a name.-->
    <Source>

        <!-- Specify MFDs ... -->
        <IncrementalMfd rate="1.0e-05" floats="false" m="6.58" type="SINGLE"
                        weight="1.0" />

        <!-- Then geometry ... 
                - 'indices' is an array of index ranges, ordered from
                  one end of the source to the other -->
        <Geometry indices="[[0:5],[13:22],[104:106]" rake="0.0" />

    </Source>

    <!-- Add more sources ... -->
    <Source />
    ...

</IndexedFaultSourceSet>
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

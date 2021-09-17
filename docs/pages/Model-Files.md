# Model Files

A variety of logic tree, configuration, and data files that are common to all models and source
types are described below and on related pages for specific model components, for example, ground
motion models (GMMs) or magnitude-frequency distributions (MFDs).

[[_TOC_]]

## Model and Calculation Configuration

**model-info.json:** Required model identifying metadata. Currently the model name and
definitions of Vs30 values for each NEHRP site class that were used when computing design ground
motions from the model.

```json
{
  "name": "NSHM Conterminous U.S. 2018",
  "site-class-vs30": {
    "AB": 1500,
    "B": 1080,
    "BC": 760,
    "C": 530,
    "CD": 365,
    "D": 260,
    "DE": 185,
    "E": 150
  }
}
```

**calc-config.json:** Optional calculation configuration file specifies the default
calculation settings used for a NSHM. This file overrides any built in default values. See
the [calculation configuration](./Calculation-Configuration.md) page for more details.

## Logic Trees

Any file or JSON member name containing `-tree` implies that the file contents or member value
will be an array of logic tree branches. Branches are defined with an `id`, `weight`, and `value`.
Depending on context, value can be a number, string, or object. For example:

 ```json
 [
  {
    "id": "string",
    "value": "number | string | object",
    "weight": 0.4
  }, {
    "id": "string",
    "value": "number | string | object",
    "weight": 0.6
  }
]
```

If the `value` member is absent, then the branch `id` is also the `value`. Logic tree weights must
sum to one. Examples of logic trees present in a hazard model are described below. An `id` or
`value` member value in ALL_CAPS indicates the value is one of a fixed number of options commonly
referred to as an enum.

### Source Model Logic Trees

**source-tree.json:** Defines a single logic tree branching point where each branch `value` points
to a subdirectory of nested branches. Source-tree weights are used to scale hazard curves when
computing mean hazard and for source event-set selection. When loading a model, a source-tree
governs all subsequent processing of nested directories. Only those resources required by the tree
or its children will be processed; any standalone sources will be ignored. For example:

```json
[
  {
    "id": "segmented",
    "weight": 0.5
  },
  {
    "id": "unsegmented",
    "weight": 0.5
  }
]
```

**source-group.json:** A specialized form of logic tree that describes model branches that are
additive and therefore does not include weights. Examples from the NSHM for the conterminous U.S.
NSHM include the Cascadia segmented partial-rupture models and the New Madrid 1500-yr cluster
branches. The branch objects in a source group _may_ include an optional `scale` member that can
be used to impose a probability of occurrence or other scaling requred by a NSHM. If absent, the
`scale` value is one.

```json
[
  {
    "id": "all",
    "scale": 0.2
  },
  {
    "id": "center-south",
    "scale": 1.5
  }
]
```

**tree-info.json:** Top level source trees and groups must be accompanied by a file that contains
a unique integer ID for the logic tree. This file _may_ also include a `name` field that, if
present, will be used instead of the enclosing directory name, but it is not required.

 ```json
{
  "id": 3199,
  "name": "Cascadia Subduction Zone"
}
```

### GMM Logic Trees

**gmm-tree.json:** Defines the logic tree of ground motion models to use in each tectonic setting.
For example:

```json
[
  { "id": "ASK_14", "weight": 0.25 },
  { "id": "BSSA_14", "weight": 0.25 },
  { "id": "CB_14", "weight": 0.25 },
  { "id": "CY_14", "weight": 0.25 }
]
```

See the [ground motion models](./Ground-Motion-Models.md) page for details on GMMs supported in
_nshmp-haz_ and the related `gmm-config.json` files that governs GMM behavior.

### MFD Logic Trees

MFD logic trees are common to all source types and are defined as JSON members nested in other
files. For example:

```json
"mfd-tree": [
  { "id": "M1", "weight": 0.3, "value": { "type": "SINGLE", "m": 6.8, "rate": 0.001 }},
  { "id": "M2", "weight": 0.3, "value": { "type": "SINGLE", "m": 7.0, "rate": 0.001 }},
  { "id": "M3", "weight": 0.3, "value": { "type": "SINGLE", "m": 7.2, "rate": 0.001 }},
  { "id": "M4", "weight": 0.1, "value": { "type": "SINGLE", "m": 7.4, "rate": 0.001 }}
]
```

An `mfd-tree` may be included as a `properties` member of a GeoJSON feature or as a member of a
`rupture-set.json` file. In both cases the tree may alternatively be identified with a string, in
which case the `mfd-tree` will be pulled from the collection of shared trees defined in a
`mfd-map.json` file. A `mfd-map.json` file is typicaly located high in the source tree heirarchy
and faciltates using the same MFDs on multiple branches of a source tree. The branches of a
mfd-tree commonly have the generic ID's: `[M1, M2, M3, ...]` to support mfd-tree matching across
source-tree branches.

How MFDs are intialized (or realized) depends on the presence and contents of `mfd-config.json` and
`rate-tree.json` files. See the
[magnitude frequency distributions](./Magnitude-Frequency-Distributions.md) page for details on
these files and the types of MFDs supported in _nshmp-haz_.

## Rupture Sets

**rupture-set.json**: A `rupture-set` is the terminal file of a source-tree branch and defines the
fault sections and MFD's required to intialize a source.
[Gridded seismicity](./Source-Types.md#grid-sources) sources use a similar **rupture-sets.json**
that defines an array of rupture sets that may be used to define zones of distinct MFD properties,
such as maximum magnitude.

```json
{
  "name": "New Madrid - USGS (center)",
  "id": 3023,
  "sections": [3020, 3021, 3022],
  "mfd-tree": "usgs-hi-mag"
}
```

In the example above, one set of New Madrid ruptures is assigned the specified numeric `id`. The
fault geometry of the rupture-set is constructed by stitching the specified fault `sections`
together, and earthquake magnitudes and rates are governed by the specified `mfd-tree`. In this
case, `mfd-tree` points to a named tree that will be present in an `mfd-map.json` accompanying
the source-tree. The `sections` member may be absent. In this case, the `id` of the rupture-set
is the same as the single, associated fault section.

**cluster-set.json**: A specialized form of a rupture set. Fault sources also support cluster
models where the total hazard is computed from the probability of exceeding some ground motion
level is conditioned on the occurrence of 2 or more, roughly contemporaneous events. A cluster-set
is composed of an array of rupture-sets.

```json
{
  "name": "New Madrid - USGS (center, center-south)",
  "id": 3025,
  "rupture-sets": [
    {
      "name": "USGS (center, center)",
      "id": 3021,
      "mfd-tree": "usgs-hi-mag"
    },
    {
      "name": "USGS (center, south)",
      "id": 3022,
      "mfd-tree": "usgs-hi-mag"
    }
  ]
}
```

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

# Magnitude-Frequency Distributions (MFDs)

An earthquake source requires a description of the sizes and rates of all earthquakes it is
capable of generating, otherwise known as a magnitude-frequency distribution (MFD). The different
types of MFDs supported in a hazard model are described below. Unless otherwise noted, all the
members listed in the JSON examples below are required.

[[_TOC_]]

## Single

A single MFD represents an earthquake of a specific magnitude size and rate. The `rate` member
is optional when a logic tree of rates is also present in a model. Example:

```json
  {
    "type": "SINGLE",
    "m": 7.0,
    "rate": 0.0001
  }
```

## Gutenberg–Richter

A [Gutenberg–Richter](http://en.wikipedia.org/wiki/Gutenberg–Richter_law) MFD represents a range
of evenly discretized magnitude events with systematically varying rates. Specifically, a
Gutneberg–Richter MFD is a doubly-truncated exponential distribution with limits at `mMin` and
`mMax`, a y-intercept of `a`, and a slope of `b`. The `a`-value member is optional when a logic
tree of rates for a source is also present in a model. Example:

```json
  {
    "type": "GR",
    "a": 1.0,
    "b": 0.8,
    "mMin": 6.55,
    "mMax": 6.95,
    "Δm": 0.1
  }
```

## Tapered Gutenberg–Richter

A [tapered Gutenberg-Richter](https://academic.oup.com/gji/article/148/3/520/822773) MFD is
similar to Gutenberg-Richter, above, but with an exponential taper applied with a corner magnitude
of `mCut`. The `a`-value member is optional when a logic tree of rates for a source is also
present in a model. Example:

```json
  {
    "type": "GR_TAPER",
    "a": 1.0,
    "b": 0.8,
    "mCut": 6.5,
    "mMin": 5.0,
    "mMax": 7.0,
    "Δm": 0.1
  }
```

## Incremental

A general purpose MFD that represents defined by explicit arrays of magnitudes and rates. Example:

```json
  {
    "type": "INCR",
    "magnitudes": [5.05, 5.15, ...],
    "rates": [1.0e-2, 0.9e-2, ...]
  }
```

An incremental MFD will ignore any uncertainty settings defined in a `mfd-config.json`.

## MFD Construction

Construction of MFDs from their declaration in a tree also depends on the following files:

**mfd-map.json:** A mfd-map defines multiple mfd-trees common to multiple branches of a
source-tree. If an `mfd-tree` value is a string, then that value must map to a logic tree
defined in a `mfd-map.json` file that is typically located high in the source model heirarchy.
For example:

```json
{
  "mfd-tree-1": [
    { "id": "M1","weight": 0.5, "value": { "type": "SINGLE", "m": 7.0}},
    { "id": "M2","weight": 0.5, "value": { "type": "SINGLE", "m": 7.5}}
  ],
  "mfd-tree-2": [
    { "id": "M1","weight": 0.5, "value": { "type": "SINGLE", "m": 7.3}},
    { "id": "M2","weight": 0.5, "value": { "type": "SINGLE", "m": 7.8}}
  ],
}
```

**mfd-config.json:** Additional uncertainty in MFDs is often considered when building hazard
models and is defined in a `mfd-config.json` file. Application of uncertainty models is MFD
type-dependent. The `epistemic-tree` member, if non-null, is used to create 3-branches for single
and Gutenberg-Richter MFDs. For a single MFD, a moment-balanced three-point distribution of
magnitudes (± 0.2 magnitude units) is created. For a Gutenberg-Richter MFD, three maximum magnitude
branches are created, also moment-balanced. The `aleatory-properties` member is only applicable
to single MFDs and may be applied in additiona to an epistemic-tree. In the example below,
`aleatory-properties` defines an eleven-point, moment-balanced normal distribution with a width
of ±2σ of magnitudes about a central magnitude. If no additional uncertainty model is desired,
`epistemic-tree` and `aleatory-properties` should be set to null.

```json
{
  "epistemic-tree": [
    { "id": "+epi", "weight": 0.2, "value": -0.2 },
    { "id": "----", "weight": 0.6, "value": 0.0 },
    { "id": "-epi", "weight": 0.2, "value": 0.2 }
  ],
  "aleatory-properties": {
    "count": 11,
    "momentBalanced": true,
    "σSize": 2,
    "σ": 0.12
  },
  "minimum-magnitude": 6.5
}
```

**rate-tree.json:** Single and Gutenberg-Richter MFDs that do not have their `rate` or `a`-value
members defined rely on the presence of a `rate-tree.json` file. A rate-tree defines each branch
`value` in years (recurrence or return period):

```json
[
  {
    "id": "R1",
    "weight": 0.2,
    "value" : 10000
  },
  {
    "id": "R2",
    "weight": 0.8,
    "value" : 2000
  }
]
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

# Magnitude-Frequency Distributions (MFDs)

An earthquake source requires a description of the sizes and rates of all earthquakes it is
capable of generating, otherwise known as a magnitude-frequency distribution (MFD). The different
types of MFDs supported in a hazard model are described below. Unless otherwise noted, all the
members listed in the JSON examples below are required.

[[_TOC_]]

MFD types:

* [Single](#single-magnitude-mfd)
* [Gutenberg-Richter](#gutenberg-richter-mfd)
* [Tapered Gutenberg-Richter](#tapered-gutenberg-richter-mfd)
* [Incremental](#incremental-mfd)

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

TODO: Need a new link for tapered GR MFD

A [tapered Gutenberg-Richter](http://scec.ess.ucla.edu/~ykagan/moms_index.html) MFD is similar to
Gutenberg-Richter, above, but with an exponential taper applied with a corner magnitude of `mCut`.
The `a`-value member is optional when a logic tree of rates for a source is also present in a
model. Example:

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

## MFD Construction

Construction of MFDs from their declaration in a tree _may_ also depend on the following files:

**mfd-map.json:** If an `mfd-tree` value is a string, then that value must map to an actual logic
tree in a `mfd-map.json` file that is typically located high in the source model heirarchy. For
example:

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

MFD confguration files:

* mfd-map.json
* mfd-config.json
* rate-tree.json
* Rate files (*.csv)

For instance, the final MFDs used in a hazard may be modified by an epistemic or aleatory
uncertainty model specified in `mfd-config.json`. Single and Gutenberg-Richter MFDs that do not
have their `rate` or `a`-value members defined rely on the presence of a `rate-tree.json` file.
A rate-tree defines a logic tree of rates or pointers to CSV rate files with spatially varying
rate data.

### `mfd-map.json`

A mfd-map defines multiple mfd-trees common to multiple branches of a source-tree.

### `mfd-config.json`

Additional uncertainty in MFDs is often considered when building hazard models and is defined
in a `mfd-config.json` file. Application of uncertainty models is MFD type-dependent.  The
`epistemic-tree` member, if non-null, is used to create 3-branches for single and Gutenberg-Richter
MFDs. For a single MFD, a moment-balanced three-point distribution of magnitudes (± 0.2 magnitude
units) is created. For a Gutenberg-Richter MFD, three maximum magnidue branches are created, also
moment-balanced. The `aleatory-properties` member is only applicable to single MFDs and may be
applied on top of an epistemic-tree. In the example below, `aleatory-properties` defines an
eleven-point, moment-balanced normal distribution with a width of ±2σ of magnitudes about a
central magnitude. If no additional uncertainty model is desired,  `epistemic-tree` and
`aleatory-properties` should be set to null.

TODO is aleatory uncertainty in MFD ALWAYS moment-balanced???

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

### `rate-tree.json`

A rate-tree defines each branch `value` in years (recurrence or return period):

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

or with pointers to rate files, in the case of grid sources:

```json
[
  {
    "id": "fixed",
    "weight": 0.6,
    "value" : "fixed.csv"
  },
  {
    "id": "adaptive",
    "weight": 0.4,
    "value" : "adaptive.csv"
  }
]
```

**mfd-config.json:** Controls properties of the MFD and possible additional epistemic or aleatory
uncertainty. For example:

```json
{
  "epistemic-tree": [
    { "id": "+uₑ", "weight": 0.2, "value": -0.2 },
    { "id": "~uₑ", "weight": 0.6, "value": 0.0 },
    { "id": "-uₑ", "weight": 0.2, "value": 0.2 }
  ],
  "aleatory-properties": {
    "size": 11,
    "nσ": 2,
    "σ": 0.12
  },
  "minimum-magnitude": 6.5,
  "nshm-bin-model": false
}
```

**rate-tree.json:** Defines each branch `value` in annual rate (1 / return period in years).
For example:

```json
[
  {
    "id": "R1",
    "weight": 0.2,
    "value" : 0.002
  },
  {
    "id": "R2",
    "weight": 0.8,
    "value" : 0.05
  }
]
```

From Model Files:

### Magnitude Frequency Distributions (MFDs)

`mfd-tree`, `mfd-map.json`, `mfd-config.json`, and `rate-tree.json`

A `mfd-tree` property is common to all source types and defines a logic tree of magnitude
frequency distributions (MFDs). The `mfd-tree` element may be an array of mfd branches defined
inline or a string reference to a top-level member of an `mfd-map.json` that contains one or
more mfd-trees shared across a source-tree. The branches of a mfd-tree commonly have the generic
ID's: `[M1, M2, M3, ...]` to support mfd-tree matching across source-tree branches.

```json
"mfd-tree": [
  { "id": "M1", "weight": 0.3, "value": { "type": "SINGLE", "m": 6.8, "rate": 0.001 }},
  { "id": "M2", "weight": 0.3, "value": { "type": "SINGLE", "m": 7.0, "rate": 0.001 }},
  { "id": "M3", "weight": 0.3, "value": { "type": "SINGLE", "m": 7.2, "rate": 0.001 }},
  { "id": "M4", "weight": 0.1, "value": { "type": "SINGLE", "m": 7.4, "rate": 0.001 }}
]
```

How MFDs are actually built depends on the settings in a `mfd-config.json` file and rates. For more
details on MFDs and their configuration see the
[magnitude frequency distributions](magnitude-frequency-distributions) section.

An `mfd-config.json` is currently only required for finite fault sources. It can be located
anywhere in the file heirarchy and may be overridden in nested directories.

Depending on the types of MFDs being modeled, a rate file may contain Gutenberg-Richter a-values
or magnitude-specific rates. The branches of a rate-tree commonly have the generic ID's:
`[R1, R2, R3, ...]` to support matching rate-trees across source-tree branches.

TODO: convert example to JSON format (or see `mfd-config.json` section above)

```xml
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
```

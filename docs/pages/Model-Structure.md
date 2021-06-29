# Model Structure

Earthquake source models are specified using [JSON](https://www.json.org),
[GeoJSON](https://geojson.org), and CSV files. The files in a model directory tree are largely
self describing and represent logic trees and other relationships between source model ingredients
(JSON), source geometry (GeoJSON features), and earthquake rate data (CSV). JSON is well-suited
for representing model data and relationships and is supported in most programming languages.

[[_TOC_]]

## Directory Structure

Earthquake source files are organized by tectonic setting: `active-crust`, `stable-crust`,
`subduction-interface`, `subduction-slab`, and `volcanic` with the two crustal and the volcanic
settings supporting the nested source types: `fault-sources`, `grid-sources`, and `zone-sources`.
The `volcanic` tectonic setting also supports `decollement-sources`.

The root of a model must include `model-info.json` and _may_ include a `calc-config.json` that
specifies any custom default [calculation configuration](calculation-configuration) settings
for the model. Top level tectonic setting directories must include `gmm-tree.json` and
`gmm-config.json` files. Source directories are loaded recursively, permitting configuration files
deeper in the heirarchy to override those defined higher in the heirarchy, as needed and as
specified for each source type. Nested directories support associations between groups of sources,
their configuration and initialization, and ground motion models. If there are a large number of
sources in a model, sources are typically further organized by region or other grouping. Example
model top-level directory tree:

```text
model-directory/
  ├─ model-info.json              (required)
  ├─ calc-config.json             (optional, overrides defaults)
  │
  ├─ active-crust/
  │   ├─ gmm-config.json          (required, can override)
  │   ├─ gmm-tree.json            (required, can override)
  │   │
  │   ├─ fault-sources/
  │   │   └─ ...
  │   │
  │   ├─ grid-sources/
  │   │   └─ ...
  │   │
  │   └─ zone-sources/
  │       └─ ...
  │
  ├─ stable-crust/...             Same structure as 'active-crust'
  │   └─ ...
  │
  ├─ subduction-interface/        Similar structure to 'fault-sources'
  │   └─ ...
  │
  └─ subduction-slab/             Similar structure to 'grid-sources'
      └─ ...
```

The following sections describe each source type, associated configuration and source definition
files, and any other requirements.

## Crustal Finite Fault Sources

Finite fault sources may either be defined as a single source (commonly with associated logic
trees of MFDs, slip rates, or dip variations) or a more complex logic tree of source model
variants. GeoJSON feature files define fault sections using a `LineString` representing the
surface trace of the section. The coordinate order of the trace must adhere to the U.S. structural
geology right-hand rule.

```text
fault-sources/
  ├─ fault-config.json            (required, can override)
  ├─ mfd-config.json              (required, can override)
  ├─ mfd-map.json                 (optional) Map of shared mfd-trees
  │
  ├─ single-fault-sources/
  │   ├─ source-1.geojson
  │   ├─ source-2.geojson
  │   └─ nested-sources/
  │       └─ ...
  │
  └─ tree-based-fault-source/     Nested directories may have any name
      ├─ source-tree.json         Source logic tree; when present, governs
      │                             subsequent processing; includes pointers
      │                             to branch directories below
      │
      ├─ features/                (required) Directory of fault features
      │   ├─ section-1.geojson
      │   ├─ section-2.geojson
      │   └─ ...
      │
      ├─ branch-1/
      │   └─ rupture-set.json     Fault source logic tree branching always
      │                             ends at a 'rupture-set'
      └─ branch-2/
          ├─ source-tree.json
          ├─ branch-3/
          └─ branch-4/
```

See also: [Finite Fault Source Type](source-types#finite-fault-sources)

## Crustal Grid Sources

TODO this isn't quite right, needs conus-2018 refactor for verification

Grid sources are based on smoothed seismicity or other spatially varying rate model and may be
defined as either single source features, each within its own directory, or as more complex logic
trees of source model variants. Grid sources are modeled as point sources of varying complexity.
Multiple GeoJSON `Polygon`s may be used to accomodate spatial variations in source properties.

```text
grid-sources/
  ├─ grid-config.json             (required, can override)
  ├─ mfd-map.json                 (optional) Map of shared mfd-trees
  ├─ features/                    (required) Directory of grid feature bounds
  ├─ grid-data/                   (required) Directory of all spatial PDFs
  │
  ├─ single-grid-source/
  │   ├─ grid-source.geojson
  │   └─ rate-tree.json           Optional tree of rates
  │
  └─ tree-based-grid-source/
      ├─ source-tree.json         Source logic tree
      │
      ├─ branch-1/
      │   └─ grid-source.geojson  Grid source logic tree branching always
      ├─ branch-2/                     ends at a *.geojson file
      │   └─ ...
      └─ ...
```

See also: [Grid Source Type](source-types#grid-sources)

## Crustal Zone (Area) Sources

Zone sources specify a single rate that is distributed over a GeoJSON `Polygon` using point source
rupture models. Presently, there is a 1:1 mapping of source zones to their associated rate files.
*__Note:__ The rate file approach will be discouraged and/or deprecated in the near future in favor
of dynamically computing rates over a zone from a single value or `rate-tree.json` when the loading
a model.*

```text
zone-sources/
  ├─ zone-config.json             (required, can override)
  ├─ mfd-config.json              (required)
  ├─ mfd-map.json                 (optional) Map of shared mfd-trees
  │
  ├─ single-zone-source/
  │   ├─ zone-source.geojson
  │   ├─ zone-source.csv          (required) File of rate data    (current)
  │   └─ [rate-tree.json]         (optional) Tree of rates        (future)
  │
  └─ tree-based-zone-source/
      ├─ source-tree.json         Source logic tree
      └─ branch-1/
          ├─ zone-source.geojson  Grid source branching ends at a *.geojson file
          ├─ zone-source.csv      (required) file of rate data    (current)
          └─ [rate-tree.json]     (optional) tree of rates        (future)
```

See also: [Zone Source Type](source-types#zone-sources)

## Subduction Interface Sources

Subduction interface sources are modeled in a similar manner as crustal fault sources; they may
be a single source or a more complex logic tree of source model variants. GeoJSON feature files
define interface sections using a `MultiLineString` of multple traces at increasing depths. The
coordinate order of each trace must adhere to the U.S. structural geology right-hand rule.

```text
subduction-interface
  ├─ gmm-config.json              (required)
  ├─ gmm-tree.json                (required)
  ├─ interface-config.json        (required)
  ├─ mfd-config.json              (required)
  └─ ...
```

See also: [Subduction Interface Source Type](source-types#subduction-interface-sources)

## Subduction Intraslab Sources

Subduction intraslab sources are modeled in a similar manner as crustal grid sources. Slab sources
typically have spatially varying rates, but their depths also vary. In contrast to grid sources,
rate files (`*.csv`) are stored adjacent to their corresponding feature file (`*.geojson`);
_this may change in a future release_.

```text
subduction-slab
  ├─ gmm-config.json              (required)
  ├─ gmm-tree.json                (required)
  ├─ slab-config.json             (required)
  ├─ mfd-config.json              (required)
  └─ ...
```

See also: [Subduction Intraslab Source Type](source-types#subduction-intraslab-sources)

---

[**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

# Implementation Details

## Logic Trees

The logic trees of epistemic uncertainty described in previous section are represented internally
using graphs of indexed nodes that connect a `root` node with one or more `leaf` nodes:

![image](images/tree-example.png "Example logic tree graph")

The different logic trees for sources, MFDs and GMMs have similar representations:

![image](images/tree-types.png "Primary logic tree types")

And each unique branch combination across all trees is considered in the hazard integral:

![image](images/tree-branches-combined.png "Logic tree branch combination")

---

[**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

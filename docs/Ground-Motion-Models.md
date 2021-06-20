# Ground Motion Models (GMMs)

Ground motion models (GMMs) forecast the range of ground motions that may occur conditioned on
the occurrence of various earthquakes. The following tables list the GMMs supported in NSHMs. It
is not uncommon for a GMM to have multiple concrete implementations. For instance, most subduction
GMMs, as published, support both interface and intraslab events.

[[_TOC_]]

* How to add links to javadocs? ...not possible in wiki (separate repo)? except with external
  http gitlab urls?
* include NSHM that used each model?
* save horizontal space in table by moving notes to table footnotes. this has to be done manually
  (see NGA-East model IDs), since markdown footnotes always appear at the bottom of the page

**gmm-config.json** Required adjacent to any `gmm-tree.json`. This file specifies the applicability
distance of the associated GMM's and any additional epistemic uncertainty model and properties to
apply to median ground motions derived from the GMM's. This uncertainty is distinct from the
built-in aleatory variability (standard deviation or sigma) of the GMM's themselves. Use `null`
values to indicate that no additional uncertainty model should be applied. Supported uncertainty
models are detailed in the [ground motion models](ground-motion-models) section. For example:

```json
{
  "max-distance": 300.0,
  "epistemic-model": null,
  "epistemic-tree": null
}
```

## GMM Configuration

A `gmm-config.json` file governs how GMMs are applied in a NSHM. It specifies a maximum distance
at which a GMM is applicable. It may also specify a model of additional epistemic uncertainty and
the logic tree used to apply it. If no such model is required, the `epistemic-model` and
`epistemic-tree` members must be `null`. See [Uncertainties in NSHMs](uncertainties-in-nshms) for
details on additional epistemic uncertainty in GMMs.

```json
{
  "max-distance": 300.0,
  "epistemic-model": "NGA_WEST2",
  "epistemic-tree": [
    { "id": "epi+", "weight": 0.185, "value": 1.0 },
    { "id": "none", "weight": 0.63, "value": 0.0 },
    { "id": "epi-", "weight": 0.185, "value": -1.0 }
  ]
}
```

## GMM Uncertainty Models

TODO

## GMM Post Processors

TODO

## Active Crust GMMs

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| **NGA-West 2** | | |
| [Abrahamson et al., 2014](http://dx.doi.org/10.1193/070913EQS198M) | ASK_14<br>ASK_14_BASIN | RotD50 |  |
| [Boore et al., 2014](http://dx.doi.org/10.1193/070113EQS184M) | BSSA_14<br>BSSA_14_BASIN | RotD50 |  |
| [Campbell & Bozorgnia, 2014](http://dx.doi.org/10.1193/062913EQS175M)| CB_14<br>CB_14_BASIN | RotD50 |  |
| [Chiou & Youngs, 2014](http://dx.doi.org/10.1193/072813EQS219M) | CY_14<br>CY_14_BASIN | RotD50 |  |
| [Idriss, 2014](http://dx.doi.org/10.1193/070613EQS195M) | IDRISS_14 | RotD50 |  |
| **NGA-West 1** | | | |
| [Boore & Anderson, 2008](http://dx.doi.org/10.1193/1.2830434) | BA_08 | GMRotI50 |  |
| [Campbell & Bozorgnia, 2008](http://dx.doi.org/10.1193/1.2857546) | CB_08 | GMRotI50 |  |
| [Chiou & Youngs, 2008](http://dx.doi.org/10.1193/1.2894832) | CY_08 | GMRotI50 |  |
| **Other** | | | |
| [Abrahamson & Silva, 1997](http://dx.doi.org/10.1785/gssrl.68.1.94) | AS_97 | Average horizontal | |
| [Boore et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.128)<br>[Boore, 2005](http://dx.doi.org/10.1785/gssrl.76.3.368) | BJF_97 | Random horizontal | Soft rock sites only (Vs30 760 m/s) |
| [Campbell, 1997](http://dx.doi.org/10.1785/gssrl.68.1.154)<br>[errata, 2000](http://dx.doi.org/10.1785/gssrl.71.3.352)<br>[errata, 2001](http://dx.doi.org/10.1785/gssrl.72.4.474) | CAMPBELL_97 | Geometric mean of two horizontal components | Soft rock sites only (Vs30 760 m/s) |
| [Campbell & Bozorgnia, 2003](http://dx.doi.org/10.1785/0120020029)<br>[errata, 2003](http://dx.doi.org/10.1785/0120030099)<br>[errata, 2003](http://dx.doi.org/10.1785/0120030143) | CB_03 | Average horizontal | Soft rock sites only (Vs30 760 m/s) |
| [McVerry et al., 2000](http://doi.org/10.5459/BNZSEE.39.1.1-58) | MCVERRY_00_CRUSTAL<br>MCVERRY_00_VOLCANIC | Max-horizontal implemented, model also supports geometric mean | New Zealand, does not correspond directly with US site class model |
| [Sadigh et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.180) | SADIGH_97 | Geometric mean of two horizontal components | Also used for interface sources in 2007 Alaska NSHM |
| [Zhao et al., 2016](http://dx.doi.org/10.1785/0120150063) | ZHAO_16_SHALLOW_CRUST<br>ZHAO_16_UPPER_MANTLE | Geometric mean of two randomly oriented horizontal components |  |

## Stable Crust GMMs

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| [Atkinson, 2008](http://dx.doi.org/10.1785/0120070199)<br>[Atkinson & Boore, 2011](http://dx.doi.org/10.1785/0120100270) | ATKINSON_08_PRIME | horizontal | Mean values clamped |
| [Atkinson & Boore, 2006](http://dx.doi.org/10.1785/0120050245) | AB_06_140BAR<br>AB_06_200BAR<br>AB_06_140BAR_AB<br>AB_06_200BAR_AB<br>AB_06_140BAR_J<br>AB_06_200BAR_J | horizontal | Mean values clamped |
| [Atkinson & Boore, 2006](http://dx.doi.org/10.1785/0120050245)<br>[Atkinson & Boore, 2011](http://dx.doi.org/10.1785/0120100270) | AB_06_PRIME | horizontal | Mean values clamped |
| [Campbell, 2003](http://dx.doi.org/10.1785/0120020002) | CAMPBELL_03<br>CAMPBELL_03_AB<br>CAMPBELL_03_J | Geometric mean of two horizontal components | Mean values clamped |
| [Frankel et al., 1996](https://pubs.usgs.gov/of/1996/532/) | FRANKEL_96<br>FRANKEL_96_AB<br>FRANKEL_96_J | not specified | Mean values clamped |
| [Graizer & Kalkan, 2015](http://dx.doi.org/10.3133/ofr20151009)<br>[Graizer & Kalkan, 2016](http://dx.doi.org/10.1785/0120150194) | GK_15 | Geometric mean of two randomly oriented horizontal components |  |
| NGA-East<br>[Goulet et al., 2017](https://peer.berkeley.edu/sites/default/files/christine-a-goulet-yousef-bozorgnia-2017_03_0.pdf) | NGA_EAST_USGS [:one:](#one-nga-east-median-model-ids)<br>NGA_EAST_USGS_SEEDS[:two:](#two-nga-east-seed-model-ids) | RotD50 (average horizontal) | Mean values are not clamped |
| [Pezeshk et al., 2011](http://dx.doi.org/10.1785/0120100144) | PEZESHK_11 | GMRotI50 | Mean values clamped |
| [Shahjouei and Pezeshk, 2016](http://dx.doi.org/10.1785/0120140367) | NGA_EAST_SEED_SP16 | RotD50 | NGA-East Seed |
| [Silva et al., 2002](http://www.pacificengineering.org/CEUS/Development%20of%20Regional%20Hard_ABC.pdf) | SILVA_02<br>SILVA_02_AB<br>SILVA_02_J | average horizontal component | Mean values clamped |
| [Somerville et al., 2001](https://earthquake.usgs.gov/static/lfs/nshm/conterminous/2008/99HQGR0098.pdf) | SOMERVILLE_01 | not specified | Mean values clamped |
| [Tavakoli & Pezeshk, 2005](http://dx.doi.org/10.1785/0120050030) | TP_05<br>TP_05_AB<br>TP_05_J | not specified | Mean values clamped |
| [Toro et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.41)<br>[Toro, 2002](http://www.ce.memphis.edu/7137/PDFs/attenuations/Toro_2001_(modification_1997).pdf) | TORO_97_MB<br>TORO_97_MW | not specified | Mean values clamped |

<a id="one-nga-east-median-model-ids"></a>
:one: NGA-East Median Model IDs: NGA_EAST_USGS_1, NGA_EAST_USGS_2, NGA_EAST_USGS_3, NGA_EAST_USGS_4, NGA_EAST_USGS_5, NGA_EAST_USGS_6, NGA_EAST_USGS_7, NGA_EAST_USGS_8, NGA_EAST_USGS_9, NGA_EAST_USGS_10, NGA_EAST_USGS_11, NGA_EAST_USGS_12, NGA_EAST_USGS_13, NGA_EAST_USGS_14, NGA_EAST_USGS_15, NGA_EAST_USGS_16, NGA_EAST_USGS_17

<a id="one-nga-east-seed-model-ids"></a>
:two: NGA-East Seed Model IDs: NGA_EAST_SEED_1CCSP, NGA_EAST_SEED_1CVSP, NGA_EAST_SEED_2CCSP, NGA_EAST_SEED_2CVSP, NGA_EAST_SEED_B_A04, NGA_EAST_SEED_B_AB14, NGA_EAST_SEED_B_AB95, NGA_EAST_SEED_B_BCA10D, NGA_EAST_SEED_B_BS11, NGA_EAST_SEED_B_SGD02, NGA_EAST_SEED_FRANKEL, NGA_EAST_SEED_GRAIZER, NGA_EAST_SEED_GRAIZER16, NGA_EAST_SEED_GRAIZER17, NGA_EAST_SEED_HA15, NGA_EAST_SEED_PEER_EX, NGA_EAST_SEED_PEER_GP, NGA_EAST_SEED_PZCT15_M1SS, NGA_EAST_SEED_PZCT15_M2ES, NGA_EAST_SEED_SP15, NGA_EAST_SEED_SP16, NGA_EAST_SEED_YA15

## Subduction GMMs  

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| [Atkinson & Boore, 2003](http://dx.doi.org/10.1785/0120020156) | AB_03_GLOBAL_INTERFACE<br>AB_03_GLOBAL_SLAB<br>AB_03_GLOBAL_SLAB_LOW_SAT<br>AB_03_CASCADIA_INTERFACE<br>AB_03_CASCADIA_SLAB<br>AB_03_CASCADIA_SLAB_LOW_SAT | horizontal |  |
| [Atkinson & Macias, 2009](http://dx.doi.org/10.1785/0120080147) | AM_09_INTERFACE<br>AM_09_INTERFACE_BASIN | Geometric mean of two horizontal components | Interface only |
| BC Hydro<br>[Abrahamson et al., 2016](http://dx.doi.org/10.1193/051712EQS188MR) | BCHYDRO_12_INTERFACE<br>BCHYDRO_12_INTERFACE_BACKARC<br>BCHYDRO_12_INTERFACE_BASIN<br>BCHYDRO_12_INTERFACE_BASIN_BACKARC<br>BCHYDRO_12_SLAB<br>BCHYDRO_12_SLAB_BACKARC<br>BCHYDRO_12_SLAB_BASIN<br>BCHYDRO_12_SLAB_BASIN_BACKARC | Geometric mean of two horizontal components |  |
| [McVerry et al., 2000](http://doi.org/10.5459/BNZSEE.39.1.1-58) | MCVERRY_00_INTERFACE<br>MCVERRY_00_SLAB<br>MCVERRY_00_VOLCANIC | Max-horizontal implemented, model also supports geometric mean | New Zealand, does not correspond directly with US site class model |
| NGA-Subduction<br>[Abrahamson et al., 2018](https://peer.berkeley.edu/sites/default/files/2018_02_abrahamson_9.10.18.pdf) | NGA_SUB_USGS_INTERFACE<br>NGA_SUB_USGS_INTERFACE_NO_EPI<br>NGA_SUB_USGS_SLAB<br>NGA_SUB_USGS_SLAB_NO_EPI | Geometric mean of two horizontal components | **Likely to be superseded by final EQS paper**<br>Calibrated for Cascadia use only |
| [Youngs et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.58) | YOUNGS_97_INTERFACE<br>YOUNGS_97_SLAB | Geometric mean of two horizontal components |  |
| [Zhao et al., 2006](http://dx.doi.org/10.1785/0120050122) | ZHAO_06_INTERFACE<br>ZHAO_06_INTERFACE_BASIN<br>ZHAO_06_SLAB<br>ZHAO_06_SLAB_BASIN | Geometric mean of two horizontal components |  |
| [Zhao et al., 2016](http://dx.doi.org/10.1785/0120150034)<br>[Zhao et al., 2016](http://dx.doi.org/10.1785/0120150056) | ZHAO_16_INTERFACE<br>ZHAO_16_SLAB<br>*ZHAO_16_UPPER_MANTLE* | Geometric mean of two randomly oriented horizontal components | Subduction Slab and Interface |

## Regional and Specialized GMMs

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| **Hawaii** | | | |
| [Atkinson, 2010](http://dx.doi.org/10.1785/0120090098) | ATKINSON_10 | geometric mean of two horizontal components |  |
| [Munson & Thurber, 1997](https://pubs.geoscienceworld.org/ssa/srl/article-abstract/68/1/41/142160/Model-of-Strong-Ground-Motions-from-Earthquakes-in) | MT_97 | Larger of two horizontal | PGA and 0.2 seconds, additional term applied for M > 7 |
| [Wong et al., 2015](http://doi.org/10.1193/012012EQS015M) | WONG_15 | average horizontal |  |
| **New Zealand** | | | |
| [McVerry et al., 2000](http://doi.org/10.5459/BNZSEE.39.1.1-58) | MCVERRY_00_CRUSTAL<br>MCVERRY_00_VOLCANIC<br>MCVERRY_00_INTERFACE<br>MCVERRY_00_SLAB | Max-horizontal implemented, model also supports geometric mean | New Zealand, does not correspond directly with US site class model. |
| **Induced Seismicity** | | | |
| [Atkinson, 2015](http://dx.doi.org/10.1785/0120140142) | ATKINSON_15 | orientation-independent horizontal |  |
<!--
## Hawaii GMMs

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| [Atkinson, 2010](http://dx.doi.org/10.1785/0120090098) | ATKINSON_10 | geometric mean of two horizontal components | Hawaii |
| [Munson & Thurber, 1997](https://pubs.geoscienceworld.org/ssa/srl/article-abstract/68/1/41/142160/Model-of-Strong-Ground-Motions-from-Earthquakes-in) | MT_97 | Larger of two horizontal | PGA and 0.2 seconds, additional term applied for M > 7 |
| [Wong et al., 2015](http://doi.org/10.1193/012012EQS015M) | WONG_15 | average horizontal |  |

## Induced Seismicity GMMs

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| [Atkinson, 2015](http://dx.doi.org/10.1785/0120140142) | ATKINSON_15 | orientation-independent horizontal |  |
-->

## Auxilliary Models

Auxilliary models are not used directly, they can be used by concrete implementations of GMMs to modify model output.

| Reference | Purpose | Component | Notes |
|:---------:|:-------:|:---------:|:------|
| [Rezaeian et al., 2014](http://dx.doi.org/10.1193/100512EQS298M) | Damping scaling factor | Average horizontal component | No effect if supplied damping ratio is 5% |
| USGS PGV | Conditional PGV for crustal earthquakes | Horizontal component | Conditional model for vertical component not yet implemented |

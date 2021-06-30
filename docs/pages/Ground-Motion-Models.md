# Ground Motion Models (GMMs)

Ground motion models (GMMs) forecast the range of ground motions that may occur conditioned on
the occurrence of various earthquakes. The following tables list the GMMs supported in NSHMs. It
is not uncommon for a GMM to have multiple concrete implementations. For instance, most subduction
GMMs, as published, support both interface and intraslab events.

[[_TOC_]]

## GMM Configuration

A **gmm-config.json** file governs how GMMs are applied in a NSHM and is required adjacent to any
`gmm-tree.json` file. It specifies a maximum distance at which associated GMMs are applicable. It
may also specify a model of additional epistemic uncertainty and the logic tree used to apply it to
median ground motions derived from the GMMs. If no such model is required, the `epistemic-model` and
`epistemic-tree` members must be `null`. This uncertainty is disctinct from the built-in aleatory
variability (standard deviation or sigma) of the GMMs themselves. See [GMM Uncertainty Models](#gmm-uncertainty-models)
for details on additional epistemic uncertainty in GMMs.

A sample `gmm-config.json` file that specifies no additional epistemic uncertainty model:

```json
{
  "max-distance": 300.0,
  "epistemic-model": null,
  "epistemic-tree": null
}
```

The following sample `gmm-config.json` file applies the NGA-West 2 epistemic uncertainty model:

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

*nshmp-haz-v2* supports additional epistemic uncertainty models derived from the PEER NGA-West 1
and PEER NGA-West 2 projects. These models both have factors for distance (`Rrup`) bins
Rrup < 10 km, 10 km <= Rrup, < 30 km, and 30 km <= Rrup, and for magnitude bins M < 6.0, 6.0 <=
M < 7.0, and 7.0 <= M. These models can be applied within the `gmm-config.json` file as shown in
the [GMM Uncertainty](#gmm-uncertainty) section above.

## GMM Post Processors

GMM post processing models are not used directly, they can be used by concrete implementations of
GMMs to modify model output.

| Reference | Purpose | Component | Notes |
|:---------:|:-------:|:---------:|:------|
| [Rezaeian et al., 2014](http://dx.doi.org/10.1193/100512EQS298M) | Damping scaling factor | Average horizontal component | No effect if supplied damping ratio is 5% |
| USGS PGV | Conditional PGV for crustal earthquakes | Horizontal component | Conditional model for vertical component not yet implemented |

## GMMs By Tectonic Setting

GMMs available in *nshmp-haz-v2* are tabulated by tectonic setting below. See the javadocs for the
[GMM Package](https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/gmm/package-summary.html)
for implementation details of each GMM and comprehensive lists of GMM IDs.

### Active Crust GMMs

Reference | ID | Component | Notes
:---------:|:--:|:---------:|:------
**NGA-West 2**
[Abrahamson et al., 2014](http://dx.doi.org/10.1193/070913EQS198M) | ASK_14<br>ASK_14_BASIN | RotD50 |
[Boore et al., 2014](http://dx.doi.org/10.1193/070113EQS184M) | BSSA_14<br>BSSA_14_BASIN | RotD50 |
[Campbell & Bozorgnia, 2014](http://dx.doi.org/10.1193/062913EQS175M)| CB_14<br>CB_14_BASIN | RotD50 |
[Chiou & Youngs, 2014](http://dx.doi.org/10.1193/072813EQS219M) | CY_14<br>CY_14_BASIN | RotD50 |
[Idriss, 2014](http://dx.doi.org/10.1193/070613EQS195M) | IDRISS_14 | RotD50 |
**NGA-West 1**
[Boore & Anderson, 2008](http://dx.doi.org/10.1193/1.2830434) | BA_08 | GMRotI50 |
[Campbell & Bozorgnia, 2008](http://dx.doi.org/10.1193/1.2857546) | CB_08 | GMRotI50 |
[Chiou & Youngs, 2008](http://dx.doi.org/10.1193/1.2894832) | CY_08 | GMRotI50 |
**Other**
[Abrahamson & Silva, 1997](http://dx.doi.org/10.1785/gssrl.68.1.94) | AS_97 | Average horizontal |
[Boore et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.128)<br>[Boore, 2005](http://dx.doi.org/10.1785/gssrl.76.3.368) | BJF_97 | Random horizontal | [1](#active-crust-gmm-notes)
[Campbell, 1997](http://dx.doi.org/10.1785/gssrl.68.1.154)<br>[errata, 2000](http://dx.doi.org/10.1785/gssrl.71.3.352)<br>[errata, 2001](http://dx.doi.org/10.1785/gssrl.72.4.474) | CAMPBELL_97 | Geometric mean of two horizontal components | [1](#active-crust-gmm-notes)
[Campbell & Bozorgnia, 2003](http://dx.doi.org/10.1785/0120020029)<br>[errata, 2003a](http://dx.doi.org/10.1785/0120030099)<br>[errata, 2003b](http://dx.doi.org/10.1785/0120030143) | CB_03 | Average horizontal | [1](#active-crust-gmm-notes)
[McVerry et al., 2000](http://doi.org/10.5459/BNZSEE.39.1.1-58) | MCVERRY_00_CRUSTAL<br>MCVERRY_00_VOLCANIC | Max-horizontal implemented, model supports geometric mean | [2](#active-crust-gmm-notes)
[Sadigh et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.180) | SADIGH_97 | Geometric mean of two horizontal components | [3](#active-crust-gmm-notes)
[Zhao et al., 2016](http://dx.doi.org/10.1785/0120150063) | ZHAO_16_SHALLOW_CRUST<br>ZHAO_16_UPPER_MANTLE | Geometric mean of two randomly oriented horizontal components |

#### Active Crust GMM Notes

1. Soft rock sites only (Vs30 760 m/s)
2. New Zealand model, does not correspond directly with US site class model
3. Also used for interface sources in 2007 Alaska NSHM

### Stable Crust GMMs

Reference | ID | Component | Notes
:---------:|:--:|:---------:|:------
**NGA-East**
NGA-East<br>[Goulet et al., 2017](https://peer.berkeley.edu/sites/default/files/christine-a-goulet-yousef-bozorgnia-2017_03_0.pdf) | NGA_EAST_USGS<sup>[1](#stable-crust-gmm-notes)</sup><br>NGA_EAST_USGS_SEEDS<sup>[2](#stable-crust-gmm-notes)</sup> | RotD50 (average horizontal) | [3](#stable-crust-gmm-notes)
[Shahjouei and Pezeshk, 2016](http://dx.doi.org/10.1785/0120140367) | NGA_EAST_SEED_SP16 | RotD50 | [4](#stable-crust-gmm-notes)
**Other**
[Atkinson, 2008](http://dx.doi.org/10.1785/0120070199)<br>[Atkinson & Boore, 2011](http://dx.doi.org/10.1785/0120100270) | ATKINSON_08_PRIME | horizontal | [5](#stable-crust-gmm-notes)
[Atkinson & Boore, 2006](http://dx.doi.org/10.1785/0120050245) | *AB_06_\*<br>140BAR\|200BAR<br>none\|_AB\|_J* | horizontal | [5](#stable-crust-gmm-notes)
[Atkinson & Boore, 2006](http://dx.doi.org/10.1785/0120050245)<br>[Atkinson & Boore, 2011](http://dx.doi.org/10.1785/0120100270) | AB_06_PRIME | horizontal | [5](#stable-crust-gmm-notes)
[Campbell, 2003](http://dx.doi.org/10.1785/0120020002) | CAMPBELL_03<br>CAMPBELL_03_AB<br>CAMPBELL_03_J | Geometric mean of two horizontal components | [5](#stable-crust-gmm-notes)
[Frankel et al., 1996](https://pubs.usgs.gov/of/1996/532/) | FRANKEL_96<br>FRANKEL_96_AB<br>FRANKEL_96_J | not specified | [5](#stable-crust-gmm-notes)
[Graizer & Kalkan, 2015](http://dx.doi.org/10.3133/ofr20151009)<br>[Graizer & Kalkan, 2016](http://dx.doi.org/10.1785/0120150194) | GK_15 | Geometric mean of two randomly oriented horizontal components |
[Pezeshk et al., 2011](http://dx.doi.org/10.1785/0120100144) | PEZESHK_11 | GMRotI50 | [5](#stable-crust-gmm-notes)
[Silva et al., 2002](http://www.pacificengineering.org/CEUS/Development%20of%20Regional%20Hard_ABC.pdf) | SILVA_02<br>SILVA_02_AB<br>SILVA_02_J | average horizontal component | [5](#stable-crust-gmm-notes)
[Somerville et al., 2001](https://earthquake.usgs.gov/static/lfs/nshm/conterminous/2008/99HQGR0098.pdf) | SOMERVILLE_01 | not specified | [5](#stable-crust-gmm-notes)
[Tavakoli & Pezeshk, 2005](http://dx.doi.org/10.1785/0120050030) | TP_05<br>TP_05_AB<br>TP_05_J | not specified | [5](#stable-crust-gmm-notes)
[Toro et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.41)<br>[Toro, 2002](http://www.ce.memphis.edu/7137/PDFs/attenuations/Toro_2001_(modification_1997).pdf) | TORO_97_MB<br>TORO_97_MW | not specified | [5](#stable-crust-gmm-notes)

#### Stable Crust GMM Notes

1. Individual NGA-East Median Model IDs are listed in the [Gmm javadocs](https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/gmm/Gmm.html)
2. Individual NGA-East Seed Model IDs are listed in the [Gmm javadocs](https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/gmm/Gmm.html)
3. Mean values are not clamped
4. Shahjouei and Pezeshk (2016) is a NGA-East seed model
5. Mean values clamped

### Subduction GMMs

*Note: See the [GMM javadocs](https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/gmm/Gmm.html)
for a comprehensive list of GMM IDs.*

Reference | ID | Component | Notes
:---------:|:--:|:---------:|:------
**NGA-Subduction**
[Abrahamson & Gülerce, 2020](https://peer.berkeley.edu/sites/default/files/2020_25.pdf) | *AG_20_\*<br>GLOBAL\|CASCADIA\|ALASKA<br>INTERFACE\|SLAB<br>no basin\|_BASIN* | Geometric mean of two horizontal components
[Kuehn et al., 2020](https://peer.berkeley.edu/sites/default/files/2020_04_kuehn_final.pdf) | *KBCG_20_\*<br>GLOBAL\|CASCADIA\|ALASKA<br>INTERFACE\|SLAB<br>no basin\|_BASIN* |
[Parker et al., 2020](https://peer.berkeley.edu/sites/default/files/2020_03_parker_final.pdf) | *PSHAB_20_\*<br>GLOBAL\|CASCADIA\|ALASKA<br>INTERFACE\|SLAB<br>no basin\|_BASIN* |
**Other**
[Atkinson & Boore, 2003](http://dx.doi.org/10.1785/0120020156) | *AB_03\*<br>GLOBAL\|CASCADIA<br>INTERFACE\|SLAB<br>none\|_LOW_SAT*| horizontal |
[Atkinson & Macias, 2009](http://dx.doi.org/10.1785/0120080147) | AM_09_INTERFACE<br>AM_09_INTERFACE_BASIN | Geometric mean of two horizontal components | [1](#subduction-gmm-notes)
BC Hydro<br>[Abrahamson et al., 2016](http://dx.doi.org/10.1193/051712EQS188MR) | *BCHYDRO_12_\*<br>INTERFACE\|SLAB<br>none\|_BASIN<br>none\|_BACKARC* | Geometric mean of two horizontal components |
BC Hydro NGA<br>[Abrahamson et al., 2018](https://peer.berkeley.edu/sites/default/files/2018_02_abrahamson_9.10.18.pdf)<sup>[2](#subduction-gmm-notes)</sup> | *BCHYDRO_18_NGA_\*<br>INTERFACE\|SLAB<br>none\|_NO_EPI* | Geometric mean of two horizontal components | [3](#subduction-gmm-notes)
[McVerry et al., 2000](http://doi.org/10.5459/BNZSEE.39.1.1-58) | MCVERRY_00_INTERFACE<br>MCVERRY_00_SLAB<br>MCVERRY_00_VOLCANIC | Max-horizontal implemented, model also supports geometric mean | [4](#subduction-gmm-notes)
[Youngs et al., 1997](http://dx.doi.org/10.1785/gssrl.68.1.58) | YOUNGS_97_INTERFACE<br>YOUNGS_97_SLAB | Geometric mean of two horizontal components |
[Zhao et al., 2006](http://dx.doi.org/10.1785/0120050122) | *ZHAO_06_\*<br>INTERFACE\|SLAB<br>none\|_BASIN* | Geometric mean of two horizontal components |
[Zhao et al., 2016](http://dx.doi.org/10.1785/0120150034)<br>[Zhao et al., 2016](http://dx.doi.org/10.1785/0120150056) | ZHAO_16_INTERFACE<br>ZHAO_16_SLAB<br>*ZHAO_16_UPPER_MANTLE* | Geometric mean of two randomly oriented horizontal components | [5](#subduction-gmm-notes)

#### Subduction GMM Notes

1. Interface only
2. Likely to be superseded by the final EQ Spectra paper
3. Calibrated for Cascadia use only
4. New Zealand model, does not correspond directly with US site class model
5. Subduction Slab and Interface

### Regional and Specialized GMMs

| Reference | ID | Component | Notes |
|:---------:|:--:|:---------:|:------|
| **Hawaii**
| [Atkinson, 2010](http://dx.doi.org/10.1785/0120090098) | ATKINSON_10 | geometric mean of two horizontal components |
| [Munson & Thurber, 1997](https://pubs.geoscienceworld.org/ssa/srl/article-abstract/68/1/41/142160/Model-of-Strong-Ground-Motions-from-Earthquakes-in) | MT_97 | Larger of two horizontal | [1](#regional-and-specialized-gmm-notes)
| [Wong et al., 2015](http://doi.org/10.1193/012012EQS015M) | WONG_15 | average horizontal |
| **New Zealand**
| [McVerry et al., 2000](http://doi.org/10.5459/BNZSEE.39.1.1-58) | MCVERRY_00_CRUSTAL<br>MCVERRY_00_VOLCANIC<br>MCVERRY_00_INTERFACE<br>MCVERRY_00_SLAB | Max-horizontal implemented, model also supports geometric mean | [1](#regional-and-specialized-gmm-notes)
| **Induced Seismicity**
| [Atkinson, 2015](http://dx.doi.org/10.1785/0120140142) | ATKINSON_15 | orientation-independent horizontal |

#### Regional and Specialized GMM Notes

1. Munson & Thurber (1997) supports PGA and 0.2 seconds, with an additional term applied for M > 7.
2. McVerry et al. (2000) is a New Zealand model and does not correspond directly with the U.S. site
   class model.

---

[**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

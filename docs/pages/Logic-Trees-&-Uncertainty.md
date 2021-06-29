# USGS Models: Logic Trees & Uncertainty

The following page details the logic trees of epistemic uncertainty considered in NSHMs supported
by *nshmp-haz-v2*. Logic trees are represented in a NSHM using files ending in `-tree.json`.

[[_TOC_]]

## Terminology

**Epistemic Uncertainty**: Uncertainty due to limited data and knowledge. Characterized by
alternative models.  
**Aleatory Variability**: Inherent uncertainty due to random variability.

## Stable Crust Ground Motion Models

| Description | 2008  | 2014 | 2018 |
|:----------- |:----- |:---- |:---- |
| CEUS   | __0.1__ : AB_06 (140 bar)<br />__0.1__ : AB_06 (200 bar)<br />__0.1__ : CAMPBELL_03<br />__0.1__ : FRANKEL_96<br />__0.1__ : SILVA_02<br />__0.2__ : SOMERVILLE_01<br />__0.2__ : TORO_97<br />__0.1__ : TP_05 | __0.22__ : AB_06'<br />__0.08__ : ATKINSON_08'<br />__0.11__ : CAMPBELL_03<br />__0.06__ : FRANKEL_96<br />__0.15__ : PEZESHK_11<br />__0.06__ : SILVA_02<br />__0.10__ : SOMERVILLE_01<br />__0.11__ : TORO_97<br />__0.11__ : TP_05 | __0.667__ : NGA_EAST_USGS (17)<br />__0.333__ : NGA_EAST_SEEDS (14)<br />(common aleatory variability) |
| Sigma Epistemic | _(none)_ | _(no change)_ | __0.2__ : USGS Panel<br />__0.8__ : EPRI |
| Site Aleatory | _(none)_ | _(no change)_ | __0.185, 0.63, 0.185__ : Site ± σ |

---

## Active Crustal Ground Motion Models

| Description | 2008  | 2014 | 2018 |
|:----------- |:----- |:---- |:---- |
| WUS | __0.3333__ : BA_08<br />__0.3333__ : CB_08<br />__0.3334__ : CY_08 | __0.22__ : ASK_14<br />__0.22__ : BSSA_14<br />__0.22__ : CB_14<br />__0.22__ : CY_14<br />__0.22__ : IDRISS_14 | __0.25__ : ASK_14<br />__0.25__ : BSSA_14<br />__0.25__ : CB_14<br />__0.25__ : CY_14 |
| Mean Epistemic | NGA-West1 (M,R)<br />__0.185__ : epi+<br />__0.630__ : off<br />__0.185__ : epi- | NGA-West2 (M,R)<br />__0.185__ : epi+<br />__0.630__ : off<br />__0.185__ : epi- | _(no change)_  |

---

## Subduction Ground Motion Models

| Description | 2008  | 2014 | 2018 |
|:----------- |:----- |:---- |:---- |
| Cascadia<br />(interface) | __0.25__ : AB_03 (global)<br />__0.25__ : YOUNGS_97<br />__0.50__ : ZHAO_06 | __0.1__ : AB_03 (global)<br />__0.3__ : AM_09<br />__0.3__ : BCHYDRO_12<br />__0.3__ : ZHAO_06 | __0.3333__ : AM_09<br />__0.3334__ : BCHYDRO_12<br />__0.3333__ : ZHAO_06 |
| Cascadia<br />(slab) | __0.25__ : AB_03 (global)<br />__0.25__ : AB_03 (cascadia)<br />__0.50__ : YOUNGS_97 | __0.1665__ : AB_03 (global, mod)<br />__0.1665__ : AB_03 (cascadia, mod)<br />__0.3330__ : BCHYDRO_12<br />__0.3340__ : ZHAO_06 | __0.5__ : BCHYDRO_12<br />__0.5__ : ZHAO_06 |

---

## Fault Source Model (CEUS)

| Model | Description | 2008 | 2014 | 2018 |
|:----- |:----------- |:---- |:---- |:---- |
| Deformation |  | __1.0__ : GEO | __0.1__ : BIRD<br />__0.8__ : GEO<br />__0.1__ : ZENG | _(no change)_  |
| Rupture |  | __0.5__ : Full<br /> __0.5__ : Partial | _(no change)_ | _(no change)_ |
| Magnitude Scaling |  | __1.0__ : Somerville-01 (area) | _(no change)_  | _(no change)_  |
| Maximum M¹ | Partial: epistemic | __0.2, 0.6, 0.2__ : M ± 0.2 | _(no change)_  | _(no change)_  |
|           | Full : epistemic   | __0.2, 0.6, 0.2__ : M ± 0.2 | _(no change)_  | _(no change)_  |
|           | Full : aleatory    |  M ± 0.24 (±2σ normal PDF)  | _(no change)_  | _(no change)_  |

¹ There are a very limited number of fault sources in CEUS

---

## Fault Source Model (WUS)

| Model | Description | 2008 | 2014 | 2018 |
|:----- |:----------- |:---- |:---- |:---- |
| Deformation |  | __1.0__ : GEO | __0.1__ : BIRD<br />__0.8__ : GEO<br />__0.1__ : ZENG | _(no change)_  |
| Rupture | Partial | __0.333__ : IMW, __0.5__ : PNW | _(no change)_ | _(no change)_ |
|         | Full    | __0.667__ : IMW, __0.5__ : PNW | _(no change)_ | _(no change)_ |
| Magnitude Scaling |  | __1.0__ : WC_94 (length) | _(no change)_  | _(no change)_  |
| Maximum M | Partial: epistemic | __0.2, 0.6, 0.2__ : M ± 0.2 | _(no change)_  | _(no change)_  |
|           | Full : epistemic   | __0.2, 0.6, 0.2__ : M ± 0.2 | _(no change)_  | _(no change)_  |
|           | Full : aleatory    |  M ± 0.24 (±2σ normal PDF)  | _(no change)_  | _(no change)_  |
| Dip | Reverse & Strike-Slip | __1.0__ : assigned | _(no change)_ | _(no change)_ |
|     | Normal | __0.2, 0.6, 0.2__ : 50 ± 10° | __0.2, 0.6, 0.2__ : 50±15° | _(no change)_ |

---

## Grid Source Model

| Model | Description | 2008 | 2014 | 2018 |
|:----- |:----------- |:---- |:---- |:---- |
| Maximum M | WUS (exceptions) | __1.0__ : 7.0 | __0.9__ : 7.5 (truncated)<br />__0.1__ : 8.0 (tapered) | _(no change)_  |
|           | CEUS (craton) | __0.1__ : 6.6<br />__0.2__ : 6.8<br />__0.5__ : 7.0<br />__0.2__ : 7.2 | __0.2__ : 6.5<br />__0.5__ : 7.0<br />__0.2__ : 7.5<br />__0.1__ : 8.0 | _(no change)_  |
| Smoothing |  | __1.0__ : Fixed |__0.4__ : Adaptive<br />__0.6__ : Fixed | _(no change)_  |
| Magnitude Scaling | CEUS & WUS | __1.0__ : WC_94 (length) | _(no change)_  | _(no change)_  |
| Focal Mechanisms | Spatially Varying | __1.0__ : assigned | _(no change)_  | _(no change)_  |
| Depth (zTor) | WUS, M < 6.5 | __1.0__ : 5.0 km | _(no change)_  | _(no change)_  |
|              | WUS, M ≥ 6.5 | __1.0__ : 1.0 km | _(no change)_  | _(no change)_  |
|              | CEUS, All M  | __1.0__ : 5.0 km | _(no change)_  | _(no change)_  |

---

## Fault Source Model (CA, UCERF3)

| Model | 2014 | 2018 |
|:----- |:---- |:---- |
| Fault | __0.5__ : FM3.1<br /> __0.5__ : FM3.2 | _(no change)_ |
| Deformation | __0.1__ : ABM<br />__0.3__ : BIRD<br />__0.3__ : GEO<br />__0.3__ : ZENG | _(no change)_  |
| Scaling Relationship<br />(mag-area & slip-length)| __0.2__ : ELLS_B<br />__0.2__ : ELLS_B (sqrt-L)<br />__0.2__ : HB_08<br />__0.2__ : SHAW_09<br />__0.2__ : SHAW_09 (csd) | _(no change)_  |
| Slip Distribution | __0.5__ : Tapered<br />__0.5__ : Boxcar | _(no change)_ |
| M ≥ 5 rate (yr⁻¹) | __0.1__ : 6.5<br />__0.6__ : 7.9<br />__0.3__ : 9.6 | _(no change)_  |
| Inversion Constraint | __1.0__ : UCERF2 (CH) | _(no change)_  |
| Fault Mo Rate | __1.0__ : off |  _(no change)_  |

---

## Grid Source Model (CA, UCERF3)

| Model | 2014 | 2018 |
|:----- |:---- |:---- |
| Grid: Maximum M | __0.1__ : 7.3<br />__0.8__ : 7.6<br />__0.1__ : 7.9 | _(no change)_  |
| Grid: Smoothing | __0.5__ : Adaptive<br />__0.5__ : Fixed | _(no change)_  |
| Grid: Focal Mechanisms | __1.0__ : assigned | _(no change)_  |
| Magnitude Scaling | __1.0__ : WC_94 (length) | _(no change)_  |

---

## Subduction seismic source model

| Model | 2008 | 2014 | 2018 |
|:----- |:---- |:---- |:---- |
| Rupture | __0.67__ : Full<br />__0.33__ : Partial | __1.0__ : Full<br />__0.5__ : Partial (segmented)<br />__0.5__ : Partial (unsegmented) | _(no change)_ |
| Magnitude Scaling | __1.0__ : Youngs et al. (length) | __0.334__ : Strasser et al. (2010)<br />__0.333__ : Murotani et al. (2008)<br />__0.333__ : Papazachos et al. (2004)  | _(no change)_  |
| Magnitude Uncertainty | __0.2, 0.6, 0.2__ : M±0.2 | _(none)_  | _(no change)_  |
| Depth | __0.5__ : Base<br />__0.2__ : Bottom<br />__0.2__ : Middle<br />__0.1__ : Top | __0.3__ : Bottom<br />__0.5__ : Middle<br />__0.2__ : Top | _(no change)_ |
| Slab: Maximum M | __1.0__ : 7.2 | __0.9__ : 7.5<br />__0.1__ : 8.0 | _(no change)_ |

* Full: a.k.a. 'characteristic'
* Partial: a.k.a. 'Gutenberg-Richter' or 'floating'

---

[**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

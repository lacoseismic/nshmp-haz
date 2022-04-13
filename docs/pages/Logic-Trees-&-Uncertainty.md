# USGS Models: Logic Trees & Uncertainty

The following page details the logic trees of epistemic uncertainty considered in NSHMs supported
by *nshmp-haz*. Logic trees are represented in a NSHM using files ending in `-tree.json`.

[[*TOC*]]

## Terminology

**Epistemic Uncertainty**: Uncertainty due to limited data and knowledge. Characterized by
alternative models.  
**Aleatory Variability**: Inherent uncertainty due to random variability.

## Stable Crust Ground Motion Models

| Description | 2008  | 2014 | 2018 |
|:----------- |:----- |:---- |:---- |
| CEUS   | **0.1** : AB_06 (140 bar)<br>**0.1** : AB_06 (200 bar)<br>**0.1** : CAMPBELL_03<br>**0.1** : FRANKEL_96<br>**0.1** : SILVA_02<br>**0.2** : SOMERVILLE_01<br>**0.2** : TORO_97<br>**0.1** : TP_05 | **0.22** : AB_06'<br>**0.08** : ATKINSON_08'<br>**0.11** : CAMPBELL_03<br>**0.06** : FRANKEL_96<br>**0.15** : PEZESHK_11<br>**0.06** : SILVA_02<br>**0.10** : SOMERVILLE_01<br>**0.11** : TORO_97<br>**0.11** : TP_05 | **0.667** : NGA_EAST_USGS (17)<br>**0.333** : NGA_EAST_SEEDS (14)<br>(common aleatory variability) |
| Sigma Epistemic | *(none)* | *(no change)* | **0.2** : USGS Panel<br>**0.8** : EPRI |
| Site Aleatory | *(none)* | *(no change)* | **0.185, 0.63, 0.185** : Site ± σ |

## Active Crustal Ground Motion Models

| Description | 2008  | 2014 | 2018 |
|:----------- |:----- |:---- |:---- |
| WUS | **0.3333** : BA_08<br>**0.3333** : CB_08<br>**0.3334** : CY_08 | **0.22** : ASK_14<br>**0.22** : BSSA_14<br>**0.22** : CB_14<br>**0.22** : CY_14<br>**0.22** : IDRISS_14 | **0.25** : ASK_14<br>**0.25** : BSSA_14<br>**0.25** : CB_14<br>**0.25** : CY_14 |
| Mean Epistemic | NGA-West1 (M,R)<br>**0.185** : epi+<br>**0.630** : off<br>**0.185** : epi- | NGA-West2 (M,R)<br>**0.185** : epi+<br>**0.630** : off<br>**0.185** : epi- | *(no change)*  |

## Subduction Ground Motion Models

| Description | 2008  | 2014 | 2018 |
|:----------- |:----- |:---- |:---- |
| Cascadia<br>(interface) | **0.25** : AB_03 (global)<br>**0.25** : YOUNGS_97<br>**0.50** : ZHAO_06 | **0.1** : AB_03 (global)<br>**0.3** : AM_09<br>**0.3** : BCHYDRO_12<br>**0.3** : ZHAO_06 | **0.3333** : AM_09<br>**0.3334** : BCHYDRO_12<br>**0.3333** : ZHAO_06 |
| Cascadia<br>(slab) | **0.25** : AB_03 (global)<br>**0.25** : AB_03 (cascadia)<br>**0.50** : YOUNGS_97 | **0.1665** : AB_03 (global, mod)<br>**0.1665** : AB_03 (cascadia, mod)<br>**0.3330** : BCHYDRO_12<br>**0.3340** : ZHAO_06 | **0.5** : BCHYDRO_12<br>**0.5** : ZHAO_06 |

## Fault Source Model (CEUS)

| Model | Description | 2008 | 2014 | 2018 |
|:----- |:----------- |:---- |:---- |:---- |
| Deformation |  | **1.0** : GEO | **0.1** : BIRD<br>**0.8** : GEO<br>**0.1** : ZENG | *(no change)*  |
| Rupture |  | **0.5** : Full<br> **0.5** : Partial | *(no change)* | *(no change)* |
| Magnitude Scaling |  | **1.0** : Somerville-01 (area) | *(no change)*  | *(no change)*  |
| Maximum M¹ | Partial: epistemic | **0.2, 0.6, 0.2** : M ± 0.2 | *(no change)*  | *(no change)*  |
|           | Full : epistemic   | **0.2, 0.6, 0.2** : M ± 0.2 | *(no change)*  | *(no change)*  |
|           | Full : aleatory    |  M ± 0.24 (±2σ normal PDF)  | *(no change)*  | *(no change)*  |

¹ There are a very limited number of fault sources in CEUS  

## Fault Source Model (WUS)

| Model | Description | 2008 | 2014 | 2018 |
|:----- |:----------- |:---- |:---- |:---- |
| Deformation |  | **1.0** : GEO | **0.1** : BIRD<br>**0.8** : GEO<br>**0.1** : ZENG | *(no change)*  |
| Rupture | Partial | **0.333** : IMW, **0.5** : PNW | *(no change)* | *(no change)* |
|         | Full    | **0.667** : IMW, **0.5** : PNW | *(no change)* | *(no change)* |
| Magnitude Scaling |  | **1.0** : WC_94 (length) | *(no change)*  | *(no change)*  |
| Maximum M | Partial: epistemic | **0.2, 0.6, 0.2** : M ± 0.2 | *(no change)*  | *(no change)*  |
|           | Full : epistemic   | **0.2, 0.6, 0.2** : M ± 0.2 | *(no change)*  | *(no change)*  |
|           | Full : aleatory    |  M ± 0.24 (±2σ normal PDF)  | *(no change)*  | *(no change)*  |
| Dip | Reverse & Strike-Slip | **1.0** : assigned | *(no change)* | *(no change)* |
|     | Normal | **0.2, 0.6, 0.2** : 50 ± 10° | **0.2, 0.6, 0.2** : 50±15° | *(no change)* |

## Grid Source Model

| Model | Description | 2008 | 2014 | 2018 |
|:----- |:----------- |:---- |:---- |:---- |
| Maximum M | WUS (exceptions) | **1.0** : 7.0 | **0.9** : 7.5 (truncated)<br>**0.1** : 8.0 (tapered) | *(no change)*  |
|           | CEUS (craton) | **0.1** : 6.6<br>**0.2** : 6.8<br>**0.5** : 7.0<br>**0.2** : 7.2 | **0.2** : 6.5<br>**0.5** : 7.0<br>**0.2** : 7.5<br>**0.1** : 8.0 | *(no change)*  |
| Smoothing |  | **1.0** : Fixed |**0.4** : Adaptive<br>**0.6** : Fixed | *(no change)*  |
| Magnitude Scaling | CEUS & WUS | **1.0** : WC_94 (length) | *(no change)*  | *(no change)*  |
| Focal Mechanisms | Spatially Varying | **1.0** : assigned | *(no change)*  | *(no change)*  |
| Depth (zTor) | WUS, M < 6.5 | **1.0** : 5.0 km | *(no change)*  | *(no change)*  |
|              | WUS, M ≥ 6.5 | **1.0** : 1.0 km | *(no change)*  | *(no change)*  |
|              | CEUS, All M  | **1.0** : 5.0 km | *(no change)*  | *(no change)*  |

## Fault Source Model (CA, UCERF3)

| Model | 2014 | 2018 |
|:----- |:---- |:---- |
| Fault | **0.5** : FM3.1<br> **0.5** : FM3.2 | *(no change)* |
| Deformation | **0.1** : ABM<br>**0.3** : BIRD<br>**0.3** : GEO<br>**0.3** : ZENG | *(no change)*  |
| Scaling Relationship<br>(mag-area & slip-length)| **0.2** : ELLS_B<br>**0.2** : ELLS_B (sqrt-L)<br>**0.2** : HB_08<br>**0.2** : SHAW_09<br>**0.2** : SHAW_09 (csd) | *(no change)*  |
| Slip Distribution | **0.5** : Tapered<br>**0.5** : Boxcar | *(no change)* |
| M ≥ 5 rate (yr⁻¹) | **0.1** : 6.5<br>**0.6** : 7.9<br>**0.3** : 9.6 | *(no change)*  |
| Inversion Constraint | **1.0** : UCERF2 (CH) | *(no change)*  |
| Fault Mo Rate | **1.0** : off |  *(no change)*  |

## Grid Source Model (CA, UCERF3)

| Model | 2014 | 2018 |
|:----- |:---- |:---- |
| Grid: Maximum M | **0.1** : 7.3<br>**0.8** : 7.6<br>**0.1** : 7.9 | *(no change)*  |
| Grid: Smoothing | **0.5** : Adaptive<br>**0.5** : Fixed | *(no change)*  |
| Grid: Focal Mechanisms | **1.0** : assigned | *(no change)*  |
| Magnitude Scaling | **1.0** : WC_94 (length) | *(no change)*  |

## Subduction seismic source model

| Model | 2008 | 2014 | 2018 |
|:----- |:---- |:---- |:---- |
| Rupture | **0.67** : Full<br>**0.33** : Partial | **1.0** : Full<br>**0.5** : Partial (segmented)<br>**0.5** : Partial (unsegmented) | *(no change)* |
| Magnitude Scaling | **1.0** : Youngs et al. (length) | **0.334** : Strasser et al. (2010)<br>**0.333** : Murotani et al. (2008)<br>**0.333** : Papazachos et al. (2004)  | *(no change)*  |
| Magnitude Uncertainty | **0.2, 0.6, 0.2** : M±0.2 | *(none)*  | *(no change)*  |
| Depth | **0.5** : Base<br>**0.2** : Bottom<br>**0.2** : Middle<br>**0.1** : Top | **0.3** : Bottom<br>**0.5** : Middle<br>**0.2** : Top | *(no change)* |
| Slab: Maximum M | **1.0** : 7.2 | **0.9** : 7.5<br>**0.1** : 8.0 | *(no change)* |

* Full: a.k.a. 'characteristic'
* Partial: a.k.a. 'Gutenberg-Richter' or 'floating'

---

## Related Pages

* [USGS Models](./USGS-Models.md#usgs-models)
  * [Model Editions](./Model-Editions.md#model-editions)
  * [Logic Trees & Uncertainty](./Logic-Trees-&-Uncertainty.md#logic-trees-&-uncertainty)
  * [Code Versions](./Code-Versions.md#code-versions)
* [**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

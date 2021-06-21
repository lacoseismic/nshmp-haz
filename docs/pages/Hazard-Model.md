# Hazard Model

A USGS seismic hazard model defines earthquake sources, the different rupture scenarios for each,
and the ground motion models to use with each scenario. Earthquake sources are representations of
geologic structures on which earthquake ruptures occur with some rate. They can be well defined
faults with a specific geometry, or uniformely distributed point source representations with rates
derived from historic earthquake catalogs. In either case, there may be multiple source geometries,
or earthquake sizes and rates, associated with a given source.

Epistemic uncertainty in source and ground motion models is represented with logic trees. The model
file formats and structure adopted here leverage the heirarchical organization of file systems to
support modeling of complex logic trees.

## Related Pages

* [Model Structure](model-structure)  
* [Model Files](model-files)  
* [Source Types](source-types)  
* [Magnitude-Frequency Distributions (MFDs)](magnitude-frequency-distributions-mfds)  
* [Rupture-Scaling Relations](magnitude-scaling-relations)  
* [Ground Motion Models (GMMs)](ground-motion-models-gmms)  

## Model Applicability

* NOTE FOR HAZARD CURVE DATA: While the gridded hazard curve data includes ground motions at long
  return periods, the USGS does not recommend using hazard values below 10<sup>-5</sup> (100,000
  years), and cautions users using values below 10<sup>-4</sup> (10,000 years). These models were
  developed for building codes concerned with return periods of 10<sup>-4</sup> and above.

* Important considerations when using NSHMs: NSHMs are only applicable to U.S. and it's
  territories. Although hazard close to the borders can be useful for comparison to other models,
  the further one drifts from the U.S. border, the more incomplete the underlying earthquake
  source model will be.

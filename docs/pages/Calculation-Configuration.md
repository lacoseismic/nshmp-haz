# Calculation Configuration

A `calc-config.json` file _may_ reside at the root of every [hazard model](hazard-model). This
file, if present, will override any built-in default calculation configuration parameters, as
listed below. See the [examples](/usgs/nshmp-haz/tree/master/etc/examples) directory, or any
[USGS model](usgs-models), for concrete examples (TODO decide if last sentence needed and check
links).

## Calculation Configuration Parameters

Calculation configuration parameters are optional (i.e. defaults are used for missing values) and
may be overridden. See [building and running](building-&-running) and the
[examples](/usgs/nshmp-haz/tree/master/etc/examples) for details.

(TODO needs updated javadoc links)

Parameter | Type | Default | Notes |
--------- | ---- | ------- | ----- |
__`hazard`__
&nbsp;&nbsp;&nbsp;`.exceedanceModel`       |`String`   | `TRUNCATION_3SIGMA_UPPER` | [`ExceedanceModel`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/ExceedanceModel.html)
&nbsp;&nbsp;&nbsp;`.truncationLevel`       |`Double`   | `3.0`                     | [1](notes)
&nbsp;&nbsp;&nbsp;`.imts`                  |`String[]` | `[ PGV, PGA, SA0P01, SA0P02, SA0P03, SA0P05, SA0P075, SA0P1, SA0P15, SA0P2, SA0P25, SA0P3, SA0P4, SA0P5, SA0P75, SA1P0, SA1P5, SA2P0, SA3P0, SA4P0, SA5P0, SA7P5, SA10P0 ]` | [`Imt`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/gmm/Imt.html)
&nbsp;&nbsp;&nbsp;`.customImls`            |`Map<String, Double[]>`  | `{}` (empty object)     | [2](#notes)
&nbsp;&nbsp;&nbsp;`.gmmUncertainty`        |`Boolean`  | `false`                   | [3](#notes)
&nbsp;&nbsp;&nbsp;`.valueFormat`           |`String`   | `ANNUAL_RATE`             | [`ValueFormat`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/ValueFormat.html)
__`deagg`__
&nbsp;&nbsp;&nbsp;`.bins`                  |`Object`   |                           | [4](#notes)
&nbsp;&nbsp;&nbsp;`.contributorLimit`      |`Double`   | `0.1`                     | [5](#notes)
__`rate`__
&nbsp;&nbsp;&nbsp;`.bins`                  |`Object`   |                           | [6](#notes)
&nbsp;&nbsp;&nbsp;`.distance`              |`Double`   | `20` km
&nbsp;&nbsp;&nbsp;`.distributionFormat`    |`String`   | `INCREMENTAL`             | [`DistributionFormat`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/DistributionFormat.html)
&nbsp;&nbsp;&nbsp;`.timespan`              |`Double`   | `30` years
&nbsp;&nbsp;&nbsp;`.valueFormat`           |`String`   | `ANNUAL_RATE`             | [`ValueFormat`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/ValueFormat.html)
__`site`__
&nbsp;&nbsp;&nbsp;`.vs30`                  |`Double`   | `760.0`                   | [`Site`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/Site.html)
&nbsp;&nbsp;&nbsp;`.vsInferred`            |`Boolean`  | `true`
&nbsp;&nbsp;&nbsp;`.z1p0`                  |`Double`   | `null`                    | [7](#notes)
&nbsp;&nbsp;&nbsp;`.z2p5`                  |`Double`   | `null`                    | [7](#notes)
__`output`__                               |
&nbsp;&nbsp;&nbsp;`.directory`             |`String`   | `hazout`
&nbsp;&nbsp;&nbsp;`.dataTypes`             |`String[]` | `[ TOTAL ]`               | [`DataType`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/DataType.html)
__`performance`__
&nbsp;&nbsp;&nbsp;`.optimizeGrids`         |`Boolean`  | `true`                    | [8](#notes)
&nbsp;&nbsp;&nbsp;`.smoothGrids`           |`Boolean`  | `true`                    | [9](#notes)
&nbsp;&nbsp;&nbsp;`.systemPartition`       |`Integer`  | `1000`                    | [10](#notes)
&nbsp;&nbsp;&nbsp;`.threadCount`           |`String`   | `ALL`                     | [`ThreadCount`](http://usgs.github.io/nshmp-haz/javadoc/index.html?gov/usgs/earthquake/nshmp/calc/ThreadCount.html)

## Notes

1. `hazard.truncationLevel`: This value is only used if the `hazard.exceedanceModel` requires a
   limit (e.g. `TRUNCATION_UPPER_ONLY`)
2. `hazard.gmmUncertainty`: If values for additional epistemic uncertainty on ground motion have
   been defined, this value en/disables this feature.
3. `hazard.customImls`: Hazard is computed at default intensity measure levels (IMLs) for every
   supported intenisty measure type (IMT), but a user can specify different IMLs as needed (see
   this [example](/usgs/nshmp-haz/blob/master/etc/examples/2-custom-config/config.json) and the
   table of default IMLs, below).
4. `disagg.bins`: This field maps to a data container that specifies the following default ranges
   and intervals for distance, magnitude, and epsilon binning: `"bins": { "rMin": 0.0, "rMax":
   1000.0, "Δr": 20.0, "mMin": 4.4, "mMax": 9.4, "Δm": 0.2, "εMin": -3.0, "εMax": 3.0, "Δε": 0.5 }`.
   The `bins` object must be fully specified; partial overrides do not apply to nested JSON objects.
5. `disagg.contributorLimit`: Specifies the cutoff (in %) below which contributing sources are not
   listed in disaggregation results.
6. `rate.bins`: This field maps to a data container that specifies the following default magnitude
   binning range and interval: `"bins": { "mMin": 4.2, "mMax": 9.4, "Δm": 0.1 }`. The `bins` object
   must be fully specified; partial overrides do not apply to nested JSON objects.
7. `site.z1p0` and `site.z2p5`: Basin terms may be specified as `null` or `NaN` (both unquoted).
   `null` is preferred as `NaN` does not conform to the JSON spec. When trying to override default
   values, however, a `null` term will be ignored whereas `NaN` will override any existing value.
8. `performance.optimizeGrids`: Gridded seismicity source optimizations are currently implemented
   for any non-fixed strike grid source. For any site, rates across all azimuths are aggregated
   in tables of distance and magnitude.
9. `performance.smoothGrids`: Resample gridded seismicity sources close to a site.
10. `performance.systemPartition`: The number of ruptures in a fault-system source to process
    concurrently.

## Default Intensity Measure Levels (IMLs)

Units of PGV IMLs are cm/s; all other IMTs are in units of g. Spectral acceleration IMTs that are
not listed use the values of the next highest spectral period.

IMT        | IMLs
-----------|-----
PGV        | 0.237, 0.355, 0.532, 0.798, 1.19, 1.80, 2.69, 4.04, 6.06, 9.09, 13.6, 20.5, 30.7, 46.0, 69.0, 103.0, 155.0, 233.0, 349.0, 525.0
PGA        | 0.00233, 0.00350, 0.00524, 0.00786, 0.0118, 0.0177, 0.0265, 0.0398, 0.0597, 0.0896, 0.134, 0.202, 0.302, 0.454, 0.680, 1.02, 1.53, 2.30, 3.44, 5.17
T ≤ 0.01 s | 0.00233, 0.00350, 0.00524, 0.00786, 0.0118, 0.0177, 0.0265, 0.0398, 0.0597, 0.0896, 0.134, 0.202, 0.302, 0.454, 0.680, 1.02, 1.53, 2.30, 3.44, 5.17
T ≤ 0.02 s | 0.00283, 0.00424, 0.00637, 0.00955, 0.0143, 0.0215, 0.0322, 0.0483, 0.0725, 0.109, 0.163, 0.245, 0.367, 0.551, 0.826, 1.24, 1.86, 2.79, 4.18, 6.27
T ≤ 0.05 s | 0.00333, 0.00499, 0.00749, 0.0112, 0.0169, 0.0253, 0.0379, 0.0569, 0.0853, 0.128, 0.192, 0.288, 0.432, 0.648, 0.972, 1.46, 2.19, 3.28, 4.92, 7.38
T ≤ 2 s    | 0.00250, 0.00375, 0.00562, 0.00843, 0.0126, 0.0190, 0.0284, 0.0427, 0.0640, 0.0960, 0.144, 0.216, 0.324, 0.486, 0.729, 1.09, 1.64, 2.46, 3.69, 5.54
T ≤ 3 s    | 0.00200, 0.00300, 0.00449, 0.00674, 0.0101, 0.0152, 0.0228, 0.0341, 0.0512, 0.0768, 0.115, 0.173, 0.259, 0.389, 0.583, 0.875, 1.31, 1.97, 2.95, 4.43
T ≤ 4 s    | 0.00133, 0.00200, 0.00300, 0.00449, 0.00674, 0.0101, 0.0152, 0.0228, 0.0341, 0.0512, 0.0768, 0.115, 0.173, 0.259, 0.389, 0.583, 0.875, 1.31, 1.97, 2.95
T ≤ 5 s    | 0.000999, 0.00150, 0.00225, 0.00337, 0.00506, 0.00758, 0.0114, 0.0171, 0.0256, 0.0384, 0.0576, 0.0864, 0.130, 0.194, 0.292, 0.437, 0.656, 0.984, 1.48, 2.21
T ≤ 7.5 s  | 0.000499, 0.000749, 0.00112, 0.00169, 0.00253, 0.00379, 0.00569, 0.00853, 0.0128, 0.0192, 0.0288, 0.0432, 0.0648, 0.0972, 0.146, 0.219, 0.328, 0.492, 0.738, 1.11
T ≤ 10 s   | 0.000333, 0.000499, 0.000749, 0.00112, 0.00169, 0.00253, 0.00379, 0.00569, 0.00853, 0.0128, 0.0192, 0.0288, 0.0432, 0.0648, 0.0972, 0.146, 0.219, 0.328, 0.492, 0.738

---

[**Documentation Index**](docs/README.md)

---
![USGS logo](images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

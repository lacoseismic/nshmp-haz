# Example 7: Disaggregation

__Working directory:__ `/path/to/nshmp-haz/etc/examples/7-disaggregation`

To perform a disaggregation of hazard, one must use the program `DisaggCalc`. Internally,
`DisaggCalc` calls `HazardCalc` and then reprocesses the data to generate output files of
disaggregation summary statistics and primary contributing sources. For this, it can be helpful
to create a second system alias:

```Shell
alias disagg='java -Xms4g -Xmx8g -cp /path/to/nshmp-haz/build/libs/nshmp-haz.jar gov.usgs.earthquake.nshmp.DisaggCalc'
```

The command line arguments for `DisaggCalc` are the same as those for `HazardCalc`. The target
return period for a disaggregation is specified in the config
[`disagg.returnPeriod`](../../../docs/pages/Calculation-Configuration.md#calculation-configuration-parameters)
field. For compute the disaggregation in this example, execute:

```Shell
disagg ../../../../nshm-conus sites.csv config.json
```

The results of the disaggregation are saved alongside hazard curves in a `disagg` directory.
Disaggregation results are stored in JSON format with one file for each site. The results for
each IMT are stored within that file as well. As with `HazardCalc`, if the `GMM` data type has
been specified (as it has in the
[config](../../../docs/pages/Calculation-Configuration.md#calculation-configuration)
file for this example) additional disaggregation results for each GMM are generated as well.
Disaggregations by individual `SOURCE` type are also possible.

Note that `DisaggCalc` will only process a CSV file of sites (not GeoJSON).

__Results directory structure:__

```text
7-disaggregation/
  └─ hazout/
      ├─ calc-config.json
      ├─ DisaggCalc.log
      ├─ disagg/Los Angeles CA.json
      │   ├─ Los Angeles CA.json
      │   ├─ Salt Lake City UT.json
      │   ├─ San Francisco CA.json
      │   └─ Seattle WA.json
      ├─ PGA/
      │   ├─ curves-truncated.csv
      │   ├─ curves.csv
      │   └─ gmm/
      │       ├─ AM_09_INTERFACE_BASIN/
      │       │   └─ curves.csv
      │       ├─ ...
      │       └─ ZHAO_06_SLAB_BASIN/
      │           └─ curves.csv
      ├─ SA0P1/
      │   └─ ...
      └─ ...
```

Note that in the output above, there are only disaggregation results for subduction GMMs
(e.g. `AM_09_INTERFACE_BASIN`) for sites closer to the Cascadia subduction zone; empty results
will not be saved.

<!-- markdownlint-disable MD001 -->
<!--  #### Next: [Example 8 – Earthquake probabilities and rates](../8-probabilities/README.md) -->

---

* [**Documentation Index**](../../../docs/README.md)

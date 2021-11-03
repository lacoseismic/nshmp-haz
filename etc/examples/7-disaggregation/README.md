# Example 7: Disaggregation

__Working directory:__ `/path/to/nshmp-haz/etc/examples/7-disaggregation`

To perform a disaggregation of hazard, one must use the program `DisaggCalc`. Internally,
`DisaggCalc` calls `HazardCalc` and then reprocesses the data to generate a comma-delimited
file of distance, magnitude, and epsilon bins, and a text file of summary statistics and primary
contributing sources. For this, it can be helpful to create a second system alias:

```Shell
alias disagg='java -Xms4g -Xmx8g -cp /path/to/nshmp-haz/build/libs/nshmp-haz.jar gov.usgs.earthquake.nshmp.DisaggCalc'
```

`DisaggCalc` is similar to `HazardCalc` in every way except that the return-period of interest
must be specified. For example, execute:

```Shell
disagg ../../../../nshm-conus sites.geojson 2475 config.json
```

The results of the disaggregation are saved along with hazard curves in `disagg` directories.
As with `HazardCalc`, if the `GMM` data type has been specified (as it has in the
[config](../../../docs/pages/Calculation-Configuration.md#calculation-configuration)
file for this example) additional disaggregation results for each GMM are generated as well.
Disaggregations by individual `SOURCE` type are also possible.

__Results directory structure:__

```text
7-disaggregation/
  └─ hazout/
      ├─ config.json
      ├─ DisaggCalc.log
      ├─ PGA/
      │   ├─ curves-truncated.csv
      │   ├─ curves.csv
      │   ├─ disagg/
      │   │   ├─ Los Angeles CA/
      │   │   │   ├─ data.csv
      │   │   │   └─ summary.txt
      │   │   ├─ Salt Lake City UT/
      │   │   │   ├─ data.csv
      │   │   │   └─ summary.txt
      │   │   ├─ San Francisco CA/
      │   │   │   ├─ data.csv
      │   │   │   └─ summary.txt
      │   │   └─ Seattle WA/
      │   │       ├─ data.csv
      │   │       └─ summary.txt
      │   └─ gmm/
      │       ├─ AM_09_INTERFACE_BASIN/
      │       │   ├─ curves.csv
      │       │   └─ disagg/
      │       │       ├─ San Francisco CA/
      │       │       │   ├─ data.csv
      │       │       │   └─ summary.txt
      │       │       └─ Seattle WA/
      │       │           ├─ data.csv
      │       │           └─ summary.txt
      │       ├─ ...
      │       ├─ CB_14_BASIN/
      │       │   ├─ curves.csv
      │       │   └─ disagg/
      │       │       ├─ Los Angeles CA/
      │       │       │   ├─ data.csv
      │       │       │   └─ dsummary.txt
      │       │       ├─ Salt Lake City UT/
      │       │       │   ├─ data.csv
      │       │       │   └─ summary.txt
      │       │       ├─ San Francisco CA/
      │       │       │   ├─ data.csv
      │       │       │   └─ summary.txt
      │       │       └─ Seattle WA/
      │       │           ├─ data.csv
      │       │           └─ summary.txt
      │       └─ ...
      ├─ SA0P1/
      │   └─ ...
      └─ ...
```

Note that in the output above, there are only disaggregation results for subduction GMMs
(e.g. `AM_09_INTERFACE_BASIN`) for sites closer to the Cascadia subduction zone; empty results
will not be saved.

<!-- markdownlint-disable MD001 -->
#### Next: [Example 8 – Earthquake probabilities and rates](../8-probabilities/README.md)

---

* [**Documentation Index**](../../../docs/README.md)
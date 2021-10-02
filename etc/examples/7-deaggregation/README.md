# Example 7: Deaggregation

__Working directory:__ `/path/to/nshmp-haz/etc/examples/7-deaggregation`

To perform a deaggregation of hazard, one must use the program `DeaggCalc`. Internally,
`DeaggCalc` calls `HazardCalc` and then reprocesses the data to generate a comma-delimited
file of distance, magnitude, and epsilon bins, and a text file of summary statistics and primary
contributing sources. For this, it can be helpful to create a second system alias:

```Shell
alias deagg='java -Xms4g -Xmx8g -cp /path/to/nshmp-haz/build/libs/nshmp-haz.jar gov.usgs.earthquake.nshmp.DeaggCalc'
```

`DeaggCalc` is similar to `HazardCalc` in every way except that the return-period of interest
must be specified. For example, execute:

```Shell
deagg ../../../../nshm-conus sites.geojson 2475 config.json
```

The results of the deaggregation are saved along with hazard curves in `deagg` directories.
As with `HazardCalc`, if the `GMM` ddata type has been specified (as it has in the
[config](../../../docs/pages/Calculation-Configuration.md#calculation-configuration)
file for this example) additional deaggregation results for each GMM are generated as well.
Deaggregations by individual `SOURCE` type are also possible.

__Results directory structure:__

```text
7-deaggregation/
  └─ hazout/
      ├─ config.json
      ├─ DeaggCalc.log
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
      │       │   └─ deagg/
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

Note that in the output above, there are only deaggregation results for
subduction GMMs (e.g. `AM_09_INTERFACE_BASIN`) for sites closer to the Cascadia subduction zone;
empty results will not be saved.

<!-- markdownlint-disable MD001 -->
#### Next: [Example 8 – Earthquake probabilities and rates](../8-probabilities/README.md)

---

* [**Documentation Index**](../../../docs/README.md)

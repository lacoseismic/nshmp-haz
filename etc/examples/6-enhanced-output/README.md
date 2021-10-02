# Example 6: Enhanced output

__Working directory:__ `/path/to/nshmp-haz/etc/examples/6-enhanced-output`

While mean hazard is of broad interest, it can be useful to preserve individual components of a
total curve, particularly with more complex models. Execute the following to write curves for
each source type and ground motion model (GMM) used in the CONUS NSHM (cloned in the previous
example):

```Shell
hazard ../../../../nshm-conus sites.geojson config.json
```

The config file for this example, `config.json`, specified `GMM` and `SOURCE` as
[output data types][output_types]. Note that the output curves directory now contains additional
directories of curves by source type and GMM.

[output_types]: ../../../docs/pages/Calculation-Configuration.md#calculation-configuration

See the `nshmp-haz` wiki and javadocs for more information on source types ([Wiki][source_wiki],
[JavaDoc][source_javadoc]) and GMMs ([Wiki][gmm_wiki], [JavaDoc][gmm_javadoc]).

[source_wiki]: ../../../docs/pages/Source-Types.md
[source_javadoc]: https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/model/SourceType.html
[gmm_wiki]: ./../../docs/pages/Ground-Motion-Models.md
[gmm_javadoc]: https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/gmm/package-summary.html

__Results directory structure:__

```text
6-enhanced-output/
  └─ hazout/
      ├─ config.json
      ├─ HazardCalc.log
      ├─ PGA/
      │   ├─ curves-truncated.csv
      │   ├─ curves.csv
      │   ├─ map.csv
      │   ├─ gmm/
      │   │   ├─ AM_09_INTERFACE_BASIN/
      │   │   │   ├─ curves.csv
      │   │   │   └─ map.csv
      │   │   ├─ ...
      │   │   └─ ZHAO_06_SLAB_BASIN/
      │   │       ├─ curves.csv
      │   │       └─ map.csv
      │   └─ source/
      │       ├─ FAULT/
      │       │   ├─ curves.csv
      │       │   └─ map.csv
      │       ├─ ...
      │       └─ SLAB/
      │           ├─ curves.csv
      │           └─ map.csv
      ├─ SA0P2/
      │   └─ ...
      └─ SA1P0/
          └─ ...
```

<!-- markdownlint-disable MD001 -->
#### Next: [Example 7 – Deaggregation](../7-deaggregation/README.md)

---

* [**Documentation Index**](../../../docs/README.md)

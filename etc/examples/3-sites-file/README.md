# Example 3: Using a custom sites file

__Working directory:__ `/path/to/nshmp-haz/etc/examples/3-sites-file`

To compute hazard at more than one site, one may supply a comma-delimited (\*.csv)
 or [GeoJSON](http://geojson.org) (\*.geojson) formatted site data file instead:

```Shell
hazard ../../peer/models/Set1-Case1 sites.csv config.json
```

or

```Shell
hazard ../../peer/models/Set1-Case1 sites.geojson config.json
```

The [site specification](../../../docs/pages/Site-Specification.md)
page provides details on the two file formats. Note that with either format,
if the name of a site is supplied, it will be included in the first column of any output curve files.

__Results directory structure:__

```text
3-sites-file/
  └─ hazout/
      ├─ config.json
      ├─ HazardCalc.log
      ├─ PGA/
      │   ├─ curves.csv
      │   ├─ curves-truncated.csv
      │   └─ map.csv
      ├─ SA0P2/
      │   ├─ curves.csv
      │   ├─ curves-truncated.csv
      │   └─ map.csv
      └─ SA1P0/
          ├─ curves.csv
          ├─ curves-truncated.csv
          └─ map.csv
```

<!-- markdownlint-disable MD001 -->
#### Next: [Example 4 – A simple hazard map](../4-hazard-map/README.md)

---

* [**Documentation Index**](../../../docs/README.md)

# Example 5: A more complex model

__Working directory:__ `/path/to/nshmp-haz/etc/examples/5-complex-model`

Most PSHAs involve the use of more complex source models, the components of which
might use different ground motion models. For this and ensuing examples, we'll use the
2018 USGS National Seismic Hazard Model (NSHM) for the western U.S. `nshmp-haz` uses an
in-memory source model. The USGS NSHMs are quite large, so it's helpful to increase the amount
memory available to Java when calling `HazardCalc`. For example, set your alias to:

```Shell
alias hazard='java -Xms4g -Xmx8g -cp /path/to/nshmp-haz/build/libs/nshmp-haz.jar gov.usgs.earthquake.nshmp.HazardCalc'
```

This will increase the minimum amount of memory Java requires to 4GB and will allow it to claim
up to 8GB, assuming that much is available.

First, clone the USGS CONUS NSHM repository and checkout the 2018 CONUS NSHM tag. Assuming you
are in the current working directory (above), the following will create a copy of the model
adjacent to nshmp-haz:

```Shell
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-conus.git ../../../../nshm-conus
# git checkout tags/5.1.0 -b 2018_CONUS_NSHM
```

TODO: checkout tag for nshm-conus-2018 release (current 5.1.0 tag does not load)

To compute hazard for a few sites in the Western U.S. at 1.0s and 2.0s
spectral periods, execute:

```Shell
hazard ../../../../nshm-conus sites.geojson config-sites.json
```

Note that more complex models take longer to initialize, although this only occurs once per
calculation, and make for longer, per-site calculations. However, `HazardCalc` will automatically
use all cores available by default and therefore performs better on multi-core systems.

To compute a small, low-resolution map for the central San Francisco bay area, execute:

```Shell
hazard ../../../../nshm-conus map.geojson config-map.json
```

This computes 121 curves over a 2° by 2° area and will give you a sense of how long a larger map
might take. This small coarse map may take 10 minutes to complete. Note that in the above two
examples we specified different output directories in the config files for each calculation.

__Results directory structure:__

```text
5-complex-model/
  ├─ hazout-sites/
  │   ├─ config.json
  │   ├─ HazardCalc.log
  │   ├─ SA1P0/
  │   │   └─ curves.csv
  │   └─ SA2P0/
  │       └─ curves.csv
  │
  └─ hazout-map/
      ├─ config.json
      ├─ HazardCalc.log
      ├─ SA1P0/
      │   └─ curves.csv
      └─ SA2P0/
          └─ curves.csv
```

<!-- markdownlint-disable MD001 -->
#### Next: [Example 6 – Enhanced output](../6-enhanced-output/README.md)

---

* [**Documentation Index**](../../../docs/README.md)

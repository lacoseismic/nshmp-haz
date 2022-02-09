# Example 1: A simple hazard calculation

__Working directory:__ `/path/to/nshmp-haz/etc/examples/1-hazard-curve`

On the command line, navigate to the directory above and execute the following:

```Shell
hazard ../../peer/models/Set1-Case1 site.csv
```

The PEER models, such as that designated above, consist of simple cases for different source
types commonly encountered in a PSHA and are included in the nshmp-haz repository to support
testing. See the [PEER directory](../../peer/) for more information.

The result of this calculation should be available as a single comma-delimited file containing
several total mean hazard curves for PGA in a newly created `hazout` directory. In this example,
the calculation configuration was derived from the model directory and the site is defined in
file `site.csv`. See the [site specification](../../../docs/pages/Site-Specification.md)
page for more details.

Note that not all [calculation configuration](../../../docs/pages/Calculation-Configuration.md)
parameters need be supplied; see the [configuration file](../../peer/models/Set1-Case1/config.json)
for this example model.

Also note that all output is written to a `hazout` directory by default, but the output destination
can be specified via the
[`output.directory`](../../../docs/pages/Calculation-Configuration.md#calculation-configuration-parameters)
parameter. In addition to hazard curves, the calculation configuration and a log of the calculation
are also saved. The primary outputs are hazard curves, hazard curves truncated below about 10⁻⁴,
and ground motion values derived from the curves for specific return periods.

__Results directory structure:__

```text
1-hazard-curve/
  └─ hazout/
      ├─ calc-config.json
      ├─ HazardCalc.log
      └─ PGA/
          ├─ curves.csv
          ├─ curves-truncated.csv
          └─ map.csv
```

In the next example, we'll override the model supplied configuration with a custom file.

<!-- markdownlint-disable MD001 -->
#### Next: [Example 2 – A custom configuration](../2-custom-config/README.md)

---

* [**Documentation Index**](../../../docs/README.md)

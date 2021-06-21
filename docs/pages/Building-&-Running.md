# Building & Running

## Related Pages

TODO

## Build & Run Options

* [Build and run locally](#build-and-run-locally)
* [Run with Docker](#run-with-docker)

## Build and Run Locally

Building and running *nshmp-haz* requires prior installation of Git and Java. Please see the
[developer basics](developer-basics) page for system configuration guidance.  

### Building

Navigate to a location on your system where you want *nshmp-haz* code to reside, clone the
repository, and compile:

```bash
cd /path/to/project/directory
git clone https://code.usgs.gov/ghsc/nshmp/nshmp-haz.git
cd nshmp-haz
./gradlew assemble
```

This creates a single file, `build/libs/nshmp-haz.jar` that may be used for hazard calculations.
`./gradlew` executes the Gradle Wrapper script (there is a `gradlew.bat` equivalent for Windows
users using the native command prompt). This executes any tasks (e.g. `assemble`) after
downloading all required dependencies, including Gradle itself.

### Computing Hazard

The `HazardCalc` program computes hazard curves at one or more sites for a variety of intensity
measures. For example:

```bash
java -cp path/to/nshmp-haz.jar gov.usgs.earthquake.nshmp.HazardCalc model sites [config]
```

At a minimum, the hazard source [model](hazard-model) and the [site](site-specification)(s) at
which to perform calculations must be specified. The source model should specified a path to a
directory. A single site may be specified with a string; multiple sites must be specified using
either a comma-delimited (CSV) or [GeoJSON](http://geojson.org) file. The path to a custom
[configuration](calculation-configuration) file containing user-specific settings may optionally
be supplied as a third argument. It can be used to override any calculation settings; if absent
[default](calculation-configuration) values are used.

See the [examples](/ghsc/nshmp/nshmp-haz-v2/-/tree/master/etc/examples) directory for more details.

### Computing Disaggregations

Like `HazardCalc`, the `DisaggCalc` program performs disaggregations at one or more sites for a
variety of intensity measures, but requires an additional `returnPeriod` argument, in years. For
example:

```bash
java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.DisaggCalc model sites returnPeriod [config]
```

Disaggregations build on and output `HazardCalc` results along with other disaggregation specific
files. Disaggregations also have some independent
[configuration](calculation-configuration#config-disagg) options.

## Run with [Docker](https://docs.docker.com/install/)

To ensure you are have the latest *nshmp-haz* update, always first pull the image from Docker:

```bash
docker pull usgs/nshmp-haz
```

### Docker Memory on Mac

By default, Docker Desktop for Mac is set to use 2 GB runtime memory. To run *nshmp-haz*, the
memory available to Docker must be [increased](https://docs.docker.com/docker-for-mac/#advanced)
to a minimum of 4 GB.

### Running

The *nshmp-haz* application may be run as a Docker container which mitigates the need to install
Git, Java, or other dependencies besides Docker. A public image is available on
Docker hub at [https://hub.docker.com/r/usgs/nshmp-haz](https://hub.docker.com/r/usgs/nshmp-haz)
which can be run with:

```bash
docker run \
    -e PROGRAM=<disagg | hazard | rate> \
    -e MODEL=<CONUS-2018 | HAWAII-2021> \
    -e RETURN_PERIOD=<RETURN_PERIOD> \
    -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
    -v /absolute/path/to/config/file:/app/config.json \
    -v /absolute/path/to/output:/app/output \
    usgs/nshmp-haz

# Example
docker run \
    -e PROGRAM=hazard \
    -e MODEL=CONUS-2018 \
    -v $(pwd)/sites.geojson:/app/sites.geojson \
    -v $(pwd)/config.json:/app/config.json \
    -v $(pwd)/hazout:/app/output \
    usgs/nshmp-haz
```

Where: (TODO links below need checking)

* `PROGRAM` is the nshmp-haz program to run:
  * disagg = `DisaggCalc`
  * hazard = `HazardCalc`
  * rate = `RateCalc`

* `MODEL` is the [USGS model (NSHM)](usgs-models) to run:
  * CONUS-2018: [Conterminous U.S. 2018](https://github.com/usgs/nshm-conus)
  * HAWAII-2021: [Hawaii 2021](https://code.usgs.gov/ghsc/nshmp/nshm-hawaii)

* `RETURN_PERIOD`, in years, is only required when running a disaggregation

* Other arguments:
  * (required) The absolute path to a GeoJSON or CSV [site(s)](site-specification) file
    * CSV example: `$(pwd)/my-csv-sites.csv:/app/sites.csv`
    * GeoJSON example: `$(pwd)/my-geojson-sites.geojson:/app/sites.geojson`
  * (optional) The absolute path to a [configuration](calculation-configuration) file
    * Example: `$(pwd)/my-custom-config.json:/app/config.json`
  * (required) The absolute path to an output directory
    * Example: `$(pwd)/my-hazard-output:/app/output`

### Run Customization

When running *nshmp-haz* with Docker the initial (Xms) and maximum (Xmx) JVM memory sizes can
be set with the environment flag (-e, -env):

```bash
docker run \
    -e JAVA_XMS=<JAVA_XMS> \
    -e JAVA_XMX=<JAVA_XMX> \
    ...
    usgs/nshmp-haz
```

Where:

* `JAVA_XMS` is the intial memory for the JVM (default: system)
* `JAVA_XMX` is the maximum memory for the JVM (default: 8g)

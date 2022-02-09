# Building & Running

## Related Pages

* [Building & Running](./Building-&-Running.md#building-&-running)
  * [Developer Basics](./Developer-Basics.md#developer-basics)
  * [Calculation Configuration](./Calculation-Configuration.md#calculation-configuration)
  * [Site Specification](./Site-Specification.md#site-specification)
  * [Examples](../../etc/examples) (or
    [on GitLab](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples))

## Build & Run Options

* [Build and run locally](#build-and-run-locally)
* [Run with Docker](#run-with-docker)

## Build and Run Locally

Building and running *nshmp-haz* requires prior installation of Git and Java. Please see the
[developer basics](./Developer-Basics.md) page for system configuration guidance.  

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

At a minimum, the hazard source [model](./Hazard-Model.md) and the [site](./Site-Specification.md)(s)
at which to perform calculations must be specified. The source model should specified a path to a
directory. A single site may be specified with a string; multiple sites must be specified using
either a comma-delimited (CSV) or [GeoJSON](http://geojson.org) file. The path to a custom
[configuration](./Calculation-Configuration.md) file containing user-specific settings may optionally
be supplied as a third argument. It can be used to override any calculation settings; if absent
[default](./Calculation-Configuration.md) values are used.

See the [examples](../../etc/examples) directory for more details (or
[on GitLab](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples))

### Computing Disaggregations

Like `HazardCalc`, the `DisaggCalc` program performs disaggregations at one or more sites for a
variety of intensity measures. The return period for the disaggregation is defined in the config,
see [`disagg.returnPeriod`](./Calculation-Configuration.md#calculation-configuration-parameters).
Example:

```bash
java -cp nshmp-haz.jar gov.usgs.earthquake.nshmp.DisaggCalc model sites [config]
```

Disaggregations build on and output `HazardCalc` results along with other disaggregation specific
files. Disaggregations also have some independent
[configuration](./Calculation-Configuration.md#calculation-configuration-parameters) options.

## Run with [Docker](https://docs.docker.com/install/)

nshmp-haz is available as a [public image](https://hub.docker.com/repository/docker/usgs/nshmp-haz)
with tags:

* `development-latest`: Developer forks
* `staging-latest`: Latest updates associated with the
[main](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main) branch
* `production-latest`: Latest stable release associated with the
[production](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/production) branch

To ensure you have the latest *nshmp-haz* update associated with a specific tag,
always first pull the image from Docker:

```bash
docker pull usgs/nshmp-haz:<tag>
```

> Replace `<tag>` with one of the above tags.

Example:

```bash
docker pull usgs/nshmp-haz:production-latest
```

### Docker Memory on Mac

By default, Docker Desktop for Mac is set to use 2 GB runtime memory. To run *nshmp-haz*, the
memory available to Docker must be [increased](https://docs.docker.com/docker-for-mac/#advanced)
to a minimum of 4 GB.

### Run nshmp-haz in Docker

The *nshmp-haz* application may be run as a Docker container which mitigates the need to install
Git, Java, or other dependencies besides Docker. A public image is available on
Docker hub at [https://hub.docker.com/r/usgs/nshmp-haz](https://hub.docker.com/r/usgs/nshmp-haz)
which can be run with:

```bash
docker run \
    --env CLASS_NAME=<DisaggCalc | HazardCalc | RateCalc> \
    --env IML=<NUMBER> \
    --env RETURN_PERIOD=<NUMBER> \
    --volume /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
    --volume /absolute/path/to/config/file:/app/config.json \
    --volume /absolute/path/to/output:/app/output \
    usgs/nshmp-haz
```

Where:

* `CLASS_NAME` is the nshmp-haz class to run:
  * [DisaggCalc](../../src/main/java/gov/usgs/earthquake/nshmp/DisaggCalc.java)
  * [HazardCalc](../../src/main/java/gov/usgs/earthquake/nshmp/HazardCalc.java)
  * [RateCalc](../../src/main/java/gov/usgs/earthquake/nshmp/RateCalc.java)
* Other arguments (local files mapped to files within the Docker container with `:/app/...`):
  * (required) The absolute path to a [USGS model (NSHM)](./USGS-Models.md)
    * Example: `$(pwd)/nshm-hawaii:/app/model`
  * (required) The absolute path to a GeoJSON or CSV [site(s)](./Site-Specification.md) file
    * CSV example: `$(pwd)/my-csv-sites.csv:/app/sites.csv`
    * GeoJSON example: `$(pwd)/my-geojson-sites.geojson:/app/sites.geojson`
  * (required) The absolute path to an output directory
    * Example: `$(pwd)/my-hazard-output:/app/output`
  * (optional) The absolute path to a [configuration](./Calculation-Configuration.md) file
    * Example: `$(pwd)/my-custom-config.json:/app/config.json`

### Docker Examples

#### [`HazardCalc`](../../src/main/java/gov/usgs/earthquake/nshmp/HazardCalc.java) Example

The following example runs the `HazardCalc` program in nshmp-haz with the
[nshm-hawaii](https://code.usgs.gov/ghsc/nshmp/nshms/nshm-hawaii.git) model and the
assumption a GeoJSON [site](./Site-Specification.md) file exists named `sites.geojson`.

```bash
# Download Hawaii NSHM
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-hawaii.git

# Pull image
docker pull usgs/nshmp-haz:production-latest

# Run nshmp-haz HazardCalc
docker run \
    --env CLASS_NAME="HazardCalc" \
    --volume "$(pwd)/nshm-hawaii:/app/model" \
    --volume "$(pwd)/sites.geojson" \
    --volume "$(pwd)/hawaii-hazard-output:/app/output" \
    usgs/nshmp-haz:production-latest
```

#### [`DisaggCalc`](../../src/main/java/gov/usgs/earthquake/nshmp/DisaggCalc.java) Example

The following example runs the `DisaggCalc` program in nshmp-haz with the
[nshm-hawaii](https://code.usgs.gov/ghsc/nshmp/nshms/nshm-hawaii.git) model and the
assumption a GeoJSON [site](./Site-Specification.md) file exists named `sites.geojson`.

```bash
# Download Hawaii NSHM
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-hawaii.git

# Pull image
docker pull usgs/nshmp-haz:production-latest

# Run nshmp-haz DisaggCalc
docker run \
    --env CLASS_NAME="DisaggCalc" \
    --env RETURN_PERIOD=475 \
    --volume "$(pwd)/nshm-hawaii:/app/model" \
    --volume "$(pwd)/sites.geojson" \
    --volume "$(pwd)/hawaii-disagg-output:/app/output" \
    usgs/nshmp-haz:production-latest
```

#### [`RateCalc`](../../src/main/java/gov/usgs/earthquake/nshmp/RateCalc.java) Example

The following example runs the `RateCalc` program in nshmp-haz with the
[nshm-hawaii](https://code.usgs.gov/ghsc/nshmp/nshms/nshm-hawaii.git) model and the
assumption a GeoJSON [site](./Site-Specification.md) file exists named `sites.geojson`.

```bash
# Download Hawaii NSHM
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-hawaii.git

# Pull image
docker pull usgs/nshmp-haz:production-latest

# Run nshmp-haz RateCalc
docker run \
    --env CLASS_NAME="RateCalc" \
    --volume "$(pwd)/nshm-hawaii:/app/model" \
    --volume "$(pwd)/sites.geojson" \
    --volume "$(pwd)/hawaii-rate-output:/app/output" \
    usgs/nshmp-haz:production-latest
```

### Run Customization

When running *nshmp-haz* with Docker the maximum JVM memory size can
be set with the environment flag (-e, -env):

```bash
docker run \
    --env JAVA_MEMORY=<MEMORY> \
    ...
    usgs/nshmp-haz

# Example
docker run \
    --env JAVA_MEMORY="12g" \
    ...
    usgs/nshmp-haz
```

Where:

* `JAVA_MEMORY` is the maximum memory for the JVM (default: 8g)

---

* [**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

# Web Services

## Related Pages

* [Building & Running](./Building-&-Running.md#building-&-running)
  * [Developer Basics](./Developer-Basics.md#developer-basics)

## Build & Run Options

* [Build and run locally](#build-and-run-locally)
* [Run with Docker](#run-with-docker)

## Build and Run Locally

Building and running *nshmp-haz* web services requires prior
installation of Git and Java. Please see the [developer basics](./Developer-Basics.md)
page for system configuration guidance.

### Building

Navigate to a location on your system where you want *nshmp-haz* code to reside, clone the
repository, and compile:

```bash
cd /path/to/project/directory
git clone https://code.usgs.gov/ghsc/nshmp/nshmp-haz.git
cd nshmp-haz
./gradlew assemble
```

This creates a single file, `build/libs/nshmp-haz.jar` that may be used to run the web services.
`./gradlew` executes the Gradle Wrapper script (there is a `gradlew.bat` equivalent for Windows
users using the native command prompt). This executes any tasks (e.g. `assemble`) after
downloading all required dependencies, including Gradle itself.

## Running Web Services

```bash
java -jar path/to/nshmp-haz.jar --model=path/to/model
```

Web service runs on [http://localhost:8080/](http://localhost:8080/)

The `--model` argument should contain the path to a single
hazard source [model](./Hazard-Model.md).

The [National Seisimic Hazard Models (NSHMs)](https://code.usgs.gov/ghsc/nshmp/nshms)
are available to download.

### Example with NSHM

```bash
# Build nshmp-haz
cd /path/to/nshmp-haz
./gradle assemble

# Download NSHM CONUS
cd ..
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-conus

# Run web services
cd /path/to/nshmp-haz
java -jar build/libs/nshmp-haz.jar --model=../nshm-conus
```

Open browser to [http://localhost:8080/](http://localhost:8080/).

## Run with Docker

### Docker Requirments

* [Docker](https://docs.docker.com/install/)

#### Docker Memory on Mac

By default, Docker Desktop for Mac is set to use 2 GB runtime memory. To run nshmp-haz-ws, the
memory available to Docker must be [increased](https://docs.docker.com/docker-for-mac/#advanced)
to a minimum of 4 GB.

### Docker Build Options

* [Build and run docker locally](#build-and-run-docker-locally)
* [Run from Container Registry](#run-from-container-registry)

### Build and Run Docker Locally

The Docker image may be built with the provided web service [Dockerfile](../../ws.Dockerfile).

```bash
cd /path/to/nshmp-haz

# Build docker image
docker build -f ws.Dockerfile -t nshmp-haz-ws .

# Run Docker image
docker run -p 8080:8080 -v "path/to/model:/model" nshmp-haz-ws
```

Web service runs on [http://localhost:8080/](http://localhost:8080/)

The hazard model is read in via Docker volumes.

#### Local Docker Example with NSHM

```bash
# Build docker image
cd /path/to/nshmp-haz
docker build -f ws.Dockerfile -t nshmp-haz-ws .

# Download NSHM CONUS
cd ..
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-conus

# Run web services
docker run -p 8080:8080 -v "$(pwd):/model" nshmp-haz-ws
```

Open browser to [http://localhost:8080/](http://localhost:8080/).

### Run from Container Registry

A public Docker image is avaialable from [Docker hub](https://hub.docker.com/r/usgs/nshmp-haz-ws).

There are 4 main tags:

* `latest`: Refers to the latest updates from the main or production branch
* `development-latest`: Refers to forks of the repository.
* `staging-latest`: Refers to the
[main](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main) branch and is the latest updates
* `production-latest`: Refers to the
[production](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/production) branch and is stable

```bash
# Pull image
docker pull usgs/nshmp-haz-ws:latest 

# Run
docker run -p 8080:8080 -v "/path/to/model:/model" usgs/nshmp-haz-ws
```

Web service runs on [http://localhost:8080/](http://localhost:8080/)

The hazard model is read in via Docker volumes.

#### Container Registry Example with NSHM

```bash
# Pull image
docker pull usgs/nshmp-haz-ws:latest 

# Download NSHM CONUS
cd ..
git clone https://code.usgs.gov/ghsc/nshmp/nshms/nshm-conus

# Run web services
docker run -p 8080:8080 -v "$(pwd):/model" usgs/nshmp-haz-ws
```

Open browser to [http://localhost:8080/](http://localhost:8080/).

### Java Memory

When running **nshmp-haz** web services with Docker
the initial (Xms) and maximum (Xmx) JVM memory sizes can
be set with the environment flag (-e, -env):

```bash
docker run -p <PORT>:8080 -e JAVA_OPTS="-Xms<INITIAL> -Xmx<MAX>" -d usgs/nshmp-haz-ws

# Example
docker run -p 8080:8080 -e JAVA_OPTS="-Xms1g -Xmx8g" -d usgs/nshmp-haz-ws
```

Where `<INITIAL>` and `<MAX >`should be set to the desired initial and maximum memory sizes,
respectively.

---

* [**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))

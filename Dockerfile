####
# Dockerfile for nshmp-haz.
#
# Usage:
#   docker run \
#       -e PROGRAM=<deagg | deagg-epsilon | deagg-iml | hazard | hazard-2018 | rate> \
#       -e MODEL=<WUS-20[08|14|18] | CEUS-20[08|14|18] | COUS-20[08|14|18] | AK-2007> \
#       -e ACCESS_VISUALVM=<true | false> \
#       -e VISUALVM_PORT=<port> \
#       -e VISUALVM_HOSTNAME=<hostname> \
#       -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
#       -v /absolute/path/to/config/file:/app/config.json \
#       -v /absolute/path/to/output:/app/output \
#       usgs/nshmp-haz
#
# Usage with custom model:
#   docker run \
#       -e PROGRAM=<deagg | deagg-epsilon | deagg-iml | hazard | hazard-2018 | rate> \
#       -e ACCESS_VISUALVM=<true | false> \
#       -e VISUALVM_PORT=<port> \
#       -e VISUALVM_HOSTNAME=<hostname> \
#       -e MOUNT_MODEL=true \
#       -v /absolute/path/to/model:/app/model \
#       -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
#       -v /absolute/path/to/config/file:/app/config.json \
#       -v /absolute/path/to/output:/app/output \
#       usgs/nshmp-haz
#
# Note: Models load as requested. While all supported models are
# available, requesting them all will eventually result in an
# OutOfMemoryError. Increase -Xmx to -Xmx16g or -Xmx24g, if available.
####

# Project
ARG project=nshmp-haz-v2

# Builder image working directory
ARG builder_workdir=/app/${project}

# Path to JAR file in builder image
ARG libs_dir=${builder_workdir}/build/libs

####
# Builder image
####
FROM usgs/centos:8 as builder

ARG builder_workdir

WORKDIR ${builder_workdir}

COPY . .

RUN yum install -y java-1.8.0-openjdk-devel which git

RUN mv nshmp-lib ../. \
    && ./gradlew --no-daemon assemble

####
# Application image
####
FROM usgs/centos:8

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ARG libs_dir
ARG builder_workdir
ARG project

ENV PROJET ${project}
ENV JAVA_XMS 8g
ENV JAVA_XMX 8g

# Whether to run hazard jar file or web services war file
ENV RUN_HAZARD true

# Running Hazard
ENV MODEL ""
ENV NSHM_VERSION=master
ENV MOUNT_MODEL false
ENV PROGRAM hazard
ENV RETURN_PERIOD ""
ENV IML ""
ENV CONFIG_FILE "config.json"

# Java VisualVM
ENV ACCESS_VISUALVM false
ENV VISUALVM_PORT 9090
ENV VISUALVM_HOSTNAME localhost

VOLUME [ "/app/output" ]

# Tomcat
ENV CATALINA_HOME /usr/local/tomcat
ENV TOMCAT_WEBAPPS ${CATALINA_HOME}/webapps
ENV PATH ${CATALINA_HOME}/bin:${PATH}
ENV JAVA_OPTS -Xms${JAVA_XMS} -Xmx${JAVA_XMX}

ENV WS_HOME ${CATALINA_HOME}
ENV HAZ_HOME /app

WORKDIR ${HAZ_HOME}

COPY --from=builder ${libs_dir}/* ./
COPY docker-entrypoint.sh .

WORKDIR ${WS_HOME}

RUN yum update -y \
    && yum install -y file jq java-1.8.0-openjdk-headless \
    && curl -L ${TOMCAT_URL} | tar -xz --strip-components=1

WORKDIR ${HAZ_HOME}

ENTRYPOINT [ "bash", "docker-entrypoint.sh" ]

EXPOSE ${VISUALVM_PORT} 8080

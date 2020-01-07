####
# Run hazard jar file or run web services war file.
#
# Running Hazard:
#   docker pull code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
#   docker run \
#       -e PROGRAM=<deagg | deagg-epsilon | deagg-iml | hazard | hazard-2018 | rate> \
#       -e MODEL=<WUS-20[08|14|18] | CEUS-20[08|14|18] | COUS-20[08|14|18] | AK-2007 | HI-2020> \
#       -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
#       -v /absolute/path/to/config/file:/app/config.json \
#       -v /absolute/path/to/output:/app/output \
#       code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
#
# Running Web Services:
#   docker pull code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
#   docker run -p <PORT>:8080 \
#       -e RUN_HAZARD=false \
#       -e MODEL=<COUS-20[08|14|18] | AK-2007 | HI-2020> \
#       code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
####

ARG project=nshmp-haz-v2
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs

####
# Builder image: Build jar and war file.
####
FROM usgs/centos:8 as builder

ARG builder_workdir

WORKDIR ${builder_workdir}

COPY . .

RUN yum install -y java-1.8.0-openjdk-devel which git

RUN mv nshmp-lib ../. \
    && ./gradlew --no-daemon assemble

####
# Application image: Run jar or war file.
####
FROM usgs/centos:8

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ARG libs_dir
ARG builder_workdir
ARG project
ARG TOMCAT_MAJOR=8
ARG TOMCAT_VERSION=${TOMCAT_MAJOR}.5.40

ENV PROJECT ${project}
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
VOLUME [ "/app/output" ]

# Tomcat
ENV CATALINA_HOME /usr/local/tomcat
ENV TOMCAT_WEBAPPS ${CATALINA_HOME}/webapps
ENV PATH ${CATALINA_HOME}/bin:${PATH}
ENV TOMCAT_SOURCE http://archive.apache.org/dist/tomcat
ENV TOMCAT_URL ${TOMCAT_SOURCE}/tomcat-${TOMCAT_MAJOR}/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz
ENV JAVA_OPTS -Xms${JAVA_XMS} -Xmx${JAVA_XMX}

ENV WS_HOME ${CATALINA_HOME}
ENV HAZ_HOME /app

WORKDIR ${HAZ_HOME}

COPY --from=builder ${libs_dir}/* ./
COPY docker-entrypoint.sh .

WORKDIR ${WS_HOME}

RUN yum update -y \
    && yum install -y file jq zip java-1.8.0-openjdk-headless \
    && curl -L ${TOMCAT_URL} | tar -xz --strip-components=1

WORKDIR ${HAZ_HOME}

ENTRYPOINT [ "bash", "docker-entrypoint.sh" ]

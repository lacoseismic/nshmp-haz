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
ARG jar_file=${libs_dir}/${project}.jar
ARG ws_file=${libs_dir}/${project}-ws.jar

####
# Builder image: Build jar and war file.
####
FROM usgs/centos:8 as builder

ENV LANG="en_US.UTF-8"

ARG builder_workdir
ARG libs_dir
ARG jar_file

WORKDIR ${builder_workdir}

COPY . .

RUN yum install -y java-11-openjdk-devel which git

RUN mv nshmp-lib ../. \
    && ./gradlew --no-daemon assemble \
    && mv ${libs_dir}/*-all.jar ${ws_file}

####
# Application image: Run jar or war file.
####
FROM usgs/centos:8

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ENV LANG="en_US.UTF-8"

ARG libs_dir
ARG ws_file
ARG builder_workdir
ARG project

ENV PROJECT ${project}
ENV CONTEXT_PATH "/"
ENV MODEL_PATH /app/models

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

WORKDIR /app

COPY --from=builder ${libs_dir}/* ./
COPY docker-entrypoint.sh .

RUN yum update -y \
    && yum install -y file jq zip java-11-openjdk-headless

EXPOSE 8080
ENTRYPOINT [ "bash", "docker-entrypoint.sh" ]

####
# Run hazard jar file.
#
# Running Hazard:
#   docker pull code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz;
#   docker run \
#       -e PROGRAM=<deagg | deagg-epsilon | deagg-iml | hazard | hazard-2018 | rate> \
#       -e MODEL=<WUS_20[08|14|18] | CEUS_20[08|14|18] | COUS_20[08|14|18] | AK_2007 | HI_2020> \
#       -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
#       -v /absolute/path/to/config/file:/app/config.json \
#       -v /absolute/path/to/output:/app/output \
#       code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz;
#
# Build locally:
#   docker build
#       --build-arg gitlab_token=<git-api-token>
#       -t nshmp-haz .
####

ARG BUILD_IMAGE=usgs/java:11
ARG FROM_IMAGE=usgs/java:11

ARG project=nshmp-haz
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs

####
# Builder image: Build jar file.
####
FROM ${BUILD_IMAGE} as builder

ARG builder_workdir
ARG libs_dir

# TODO
# Remove once nshmp-lib is public
ARG git_username
ARG git_password
ARG GITLAB_TOKEN=null
ARG CI_JOB_TOKEN=null

ENV GIT_NSHMP_USERNAME ${git_username}
ENV GIT_NSHMP_PASSWORD ${git_password}

WORKDIR ${builder_workdir}

COPY . .

RUN ./gradlew assemble

####
# Application image: Run jar file.
####
FROM ${FROM_IMAGE}

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ARG builder_workdir
ARG libs_dir
ARG project
ARG ws_file

ENV CONFIG_FILE ""
ENV DEBUG false
ENV IML ""
ENV JAVA_XMX "8g"
ENV MODEL ""
ENV MOUNT_MODEL false
ENV NSHM_VERSION main
ENV PROGRAM hazard
ENV PROJECT ${project}
ENV RETURN_PERIOD ""

VOLUME [ "/app/output" ]

WORKDIR /app

COPY --from=builder ${libs_dir}/* ./
COPY scripts scripts

RUN yum install -y jq

EXPOSE 8080
ENTRYPOINT [ "bash", "scripts/docker-entrypoint.sh" ]

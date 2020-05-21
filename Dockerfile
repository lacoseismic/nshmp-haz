####
# Run hazard jar file.
#
# Running Hazard:
#   docker pull code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
#   docker run \
#       -e PROGRAM=<deagg | deagg-epsilon | deagg-iml | hazard | hazard-2018 | rate> \
#       -e MODEL=<WUS_20[08|14|18] | CEUS_20[08|14|18] | COUS_20[08|14|18] | AK_2007 | HI_2020> \
#       -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \
#       -v /absolute/path/to/config/file:/app/config.json \
#       -v /absolute/path/to/output:/app/output \
#       code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
#
# Build locally:
#   docker build --build-arg ssh_private_key="$(cat ~/.ssh/id_rsa)" -t nshmp-haz .
####

ARG project=nshmp-haz-v2
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs

####
# Builder image: Build jar and war file.
####
FROM usgs/centos:8 as builder

ARG builder_workdir
ARG libs_dir
ARG ssh_private_key

ENV LANG="en_US.UTF-8"

WORKDIR ${builder_workdir}

COPY . .

RUN yum install -y java-11-openjdk-devel which git \
    && eval $(ssh-agent -s) \
    && mkdir -p ~/.ssh \
    && chmod 700 ~/.ssh \
    && echo "${ssh_private_key}" >> ~/.ssh/id_rsa \
    && chmod 0600 ~/.ssh/id_rsa \
    && echo -e "Host *\n\tStrictHostKeyChecking no\n\n" > ~/.ssh/config

RUN ./gradlew --no-daemon assemble

####
# Application image: Run jar or war file.
####
FROM usgs/centos:8

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ARG builder_workdir
ARG libs_dir
ARG project
ARG ws_file

ENV CONFIG_FILE ""
ENV DEBUG false
ENV IML ""
ENV JAVA_XMX "8g"
ENV LANG="en_US.UTF-8"
ENV MODEL ""
ENV MOUNT_MODEL false
ENV NSHM_VERSION master
ENV PROGRAM hazard
ENV PROJECT ${project}
ENV RETURN_PERIOD ""

VOLUME [ "/app/output" ]

WORKDIR /app

COPY --from=builder ${libs_dir}/* ./
COPY scripts scripts

RUN yum update -y \
    && yum install -y jq git java-11-openjdk-headless

EXPOSE 8080
ENTRYPOINT [ "bash", "scripts/docker-entrypoint.sh" ]

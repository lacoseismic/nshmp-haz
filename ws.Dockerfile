####
# Run hazard web services.
#
# Build locally:
#   docker build --build-arg ssh_private_key="$(cat ~/.ssh/id_rsa)" -t nshmp-haz-ws .
####

ARG project=nshmp-haz-v2
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs
ARG ws_file=${libs_dir}/${project}-ws.jar

####
# Builder image: Build jar and war file.
####
FROM usgs/centos:8 as builder

ARG builder_workdir
ARG libs_dir
ARG ssh_private_key
ARG ws_file

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

RUN ./gradlew --no-daemon assemble \
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

ENV DEBUG false
ENV PROJECT ${project}
ENV CONTEXT_PATH "/"

WORKDIR /app

COPY --from=builder ${libs_dir}/* ./
COPY scripts scripts

RUN yum update -y \
    && yum install -y git java-11-openjdk-headless

EXPOSE 8080
ENTRYPOINT [ "bash", "scripts/docker-entrypoint.ws.sh" ]

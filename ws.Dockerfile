####
# Run hazard web services.
#
# Build locally:
#   docker build
#       -f ws.Dockerfile
#       --build-arg git_username=<user@name.com>
#       --build-arg git_password=<git-api-token>
#       -t nshmp-haz-ws .

####

ARG project=nshmp-haz-v2
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs
ARG jar_file=${libs_dir}/${project}.jar

####
# Builder image: Build jar and war file.
####
FROM usgs/centos:8 as builder

ARG builder_workdir
ARG git_username
ARG git_password
ARG libs_dir
ARG jar_file

ENV LANG="en_US.UTF-8"

WORKDIR ${builder_workdir}
ENV GIT_NSHMP_USERNAME ${git_username}
ENV GIT_NSHMP_PASSWORD ${git_password}

COPY . .
RUN yum install -y glibc-langpack-en java-11-openjdk-devel which git \
    && ./gradlew --no-daemon assemble

####
# Application image: Run jar or war file.
####
FROM usgs/centos:8

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ENV LANG="en_US.UTF-8"

ARG libs_dir
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

####
# Run hazard web services.
#
# Build locally:
#   docker build
#       -f ws.Dockerfile
#       --build-arg gitlab_token=<git-api-token>
#       -t nshmp-haz-ws .
####

ARG BUILD_IMAGE=usgs/java:11
ARG FROM_IMAGE=usgs/java:11

ARG project=nshmp-haz-v2
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs
ARG jar_file=${libs_dir}/${project}.jar

####
# Builder image: Build war file.
####
FROM ${BUILD_IMAGE} as builder

ARG builder_workdir
ARG libs_dir
ARG jar_file
ARG git_username
ARG git_password
ARG gitlab_token=null
ARG ci_job_token=null

ENV GIT_NSHMP_USERNAME ${git_username}
ENV GIT_NSHMP_PASSWORD ${git_password}
ENV GITLAB_TOKEN ${gitlab_token}
ENV CI_JOB_TOKEN ${ci_job_token}

WORKDIR ${builder_workdir}

COPY . .
RUN ./gradlew assemble

####
# Application image: Run war file.
####
FROM ${FROM_IMAGE}

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ARG libs_dir
ARG builder_workdir
ARG project

ENV DEBUG false
ENV PROJECT ${project}
ENV CONTEXT_PATH "/"

WORKDIR /app

COPY --from=builder ${libs_dir}/* ./
COPY scripts scripts

EXPOSE 8080
ENTRYPOINT [ "bash", "scripts/docker-entrypoint.ws.sh" ]

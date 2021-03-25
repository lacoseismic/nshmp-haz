####
# Run hazard web services.
#
# Build locally:
#   docker build
#       -f ws.Dockerfile
#       --build-arg gitlab_token=<git-api-token>
#       -t nshmp-haz-ws .
#
# Run locally:
#   docker run -p 8080:8080
#       -v "path/to/models:/models"
#       nshmp-haz-ws
####

ARG BUILD_IMAGE=usgs/java:11
ARG FROM_IMAGE=usgs/java:11

ARG project=nshmp-haz-v2
ARG builder_workdir=/app/${project}
ARG libs_dir=${builder_workdir}/build/libs
ARG jar_file=${libs_dir}/${project}.jar

####
# Builder image: Build jar file.
####
FROM ${BUILD_IMAGE} as builder

ARG builder_workdir

# TODO
# Remove once nshmp-lib is public
ARG gitlab_token=null
ARG ci_job_token=null
ENV GITLAB_TOKEN ${gitlab_token}
ENV CI_JOB_TOKEN ${ci_job_token}

WORKDIR ${builder_workdir}

COPY . .

RUN ./gradlew assemble

####
# Application image: Run jar file.
####
FROM ${FROM_IMAGE}

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ARG libs_dir
ARG builder_workdir
ARG project

ENV PROJECT ${project}
ENV CONTEXT_PATH "/"
ENV BASIN_SERVICE_URL "https://staging-earthquake.usgs.gov/nshmp/ws/data/basin"
ENV JAVA_XMX="8g"
ENV JAVA_XMS="2g"

WORKDIR /app

COPY --from=builder ${libs_dir}/* ./

VOLUME [ "/models" ]

EXPOSE 8080

ENTRYPOINT java -jar "${PROJECT}.jar" \
    "-Xms${JAVA_XMS}" \
    "-Xmx${JAVA_XMX}" \
    "-Dmicronaut.server.context-path=${CONTEXT_PATH}" \
    --basin-service-url="${BASIN_SERVICE_URL}" \
    --models="/models";

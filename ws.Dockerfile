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

####
# Builder image: Build jar file.
####
FROM ${BUILD_IMAGE} as builder

ARG builder_workdir

# TODO
# Remove once nshmp-lib is public
ARG GITLAB_TOKEN=null
ARG CI_JOB_TOKEN=null

WORKDIR /app

COPY . .

RUN ./gradlew assemble

####
# Application image: Run jar file.
####
FROM ${FROM_IMAGE}

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ENV CONTEXT_PATH="/"
ENV BASIN_SERVICE_URL="https://earthquake.usgs.gov/ws/nshmp/data/basin"
ENV JAVA_OPTS="-Xms2g -Xmx8g"
ENV MODELS_DIRECTORY="/models"

WORKDIR /app

COPY --from=builder /app/build/libs/nshmp-haz.jar .

VOLUME [ "${MODELS_DIRECTORY}" ]

EXPOSE 8080

ENTRYPOINT java \
    ${JAVA_OPTS} \
    -jar \
    nshmp-haz.jar \
    "-Dmicronaut.server.context-path=${CONTEXT_PATH}" \
    --basin-service-url="${BASIN_SERVICE_URL}" \
    --models="${MODELS_DIRECTORY}";

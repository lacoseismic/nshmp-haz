####
# Run hazard web services.
#
# Pull Docker Image:
#   - Production (stable): docker pull usgs/nshmp-haz-ws:production-latest
#   - Staging (latest, main branch of repo): docker pull usgs/nshmp-haz-ws:staging-latest
#   - Development (developer forks): docker pull usgs/nshmp-haz-ws:development-latest
#
# Build locally:
#   docker build
#       -f ws.Dockerfile
#       -t nshmp-haz-ws .
#
# Run locally:
#   docker run -p 8080:8080
#       -v "path/to/models:/models"
#       nshmp-haz-ws
####

ARG BUILD_IMAGE=usgs/amazoncorretto:11
ARG FROM_IMAGE=usgs/amazoncorretto:11

####
# Builder image: Build jar file.
####
FROM ${BUILD_IMAGE} as builder

ARG builder_workdir

# For GitLab CI/CD
ARG CI_PROJECT_URL=null
ARG CI_COMMIT_BRANCH=null

WORKDIR /app

COPY . .

RUN ./gradlew assemble

####
# Application image: Run jar file.
####
FROM ${FROM_IMAGE}

LABEL maintainer="Peter Powers <pmpowers@usgs.gov>, Brandon Clayton <bclayton@usgs.gov>"

ENV CONTEXT_PATH="/"
ENV JAVA_OPTS="-Xms2g -Xmx8g"
ENV MODELS_DIRECTORY="/model"

WORKDIR /app

COPY --from=builder /app/build/libs/nshmp-haz.jar .

VOLUME [ "${MODELS_DIRECTORY}" ]

EXPOSE 8080

ENTRYPOINT java \
    ${JAVA_OPTS} \
    -jar \
    nshmp-haz.jar \
    "-Dmicronaut.server.context-path=${CONTEXT_PATH}" \
    --model="${MODELS_DIRECTORY}";

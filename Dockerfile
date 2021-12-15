####
# Run nshmp-haz
#
# Pull Docker Image:
#   - Production (stable): docker pull usgs/nshmp-haz:production-latest
#   - Staging (latest, main branch of repo): docker pull usgs/nshmp-haz:staging-latest
#   - Development (developer forks): docker pull usgs/nshmp-haz:development-latest
#
# Run Docker Image:
#   Parameters:
#     - CLASS_NAME: The nshmp-haz class name to run (e.g. HazardCalc)
#     - IML: The intensity measure level, used in certain programs
#     - JAVA_OPTS: Any JVM options (e.g. -Xmx8g)
#     - RETURN_PERIOD: The return period, used in certian programs
#
#   Volumes:
#     - Model: /app/model
#     - Output: /app/output
#
#   docker run \
#       --env CLASS_NAME="nshmp-haz class name" \
#       --volume "/path/to/model:/app/model" \
#       --volume "/path/to/output:/app/output" \
#       usgs/nshmp-haz:production-latest
#
# Build locally:
#   docker build -t nshmp-haz .
####

ARG BUILD_IMAGE=usgs/amazoncorretto:11
ARG FROM_IMAGE=usgs/amazoncorretto:11

####
# Builder image: Build jar file.
####
FROM ${BUILD_IMAGE} as builder

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

# nshmp-haz inputs
ENV CLASS_NAME "HazardCalc"
ENV IML ""
ENV RETURN_PERIOD ""

ENV CONFIG_FILE "/app/config.json"
ENV JAVA_MEMORY "8g"
ENV MODEL_PATH "/app/model"
ENV OUTPUT_PATH "/app/output"

VOLUME [ "${MODEL_PATH}", "${OUTPUT_PATH}" ]

WORKDIR /app

COPY --from=builder /app/build/libs/nshmp-haz.jar .
COPY scripts scripts

RUN yum install -y jq \
    && echo "{}" > "${CONFIG_FILE}"

ENTRYPOINT [ "bash", "scripts/docker-entrypoint.sh" ]

#!/bin/bash
#
# Script to build Docker image and push to internal registry.
##

BUILD_ARGS="";

for arg in ${DOCKER_BUILD_ARGS}; do
  BUILD_ARGS="${BUILD_ARGS} --build-arg ${arg}";
done

echo "${BUILD_ARGS}";

# Build Docker image
# shellcheck disable=SC2086
docker build \
    ${BUILD_ARGS} \
    --pull \
    --tag "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:${CI_COMMIT_REF_SLUG}" \
    --file "${DOCKERFILE}" \
    .;

# Push image to internal registry
docker push "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:${CI_COMMIT_REF_SLUG}";

# Push latest tag
if [[
    ${CI_COMMIT_REF_SLUG} == "master" ||
    ${CI_COMMIT_REF_SLUG} == "production" ||
    -n "${CI_COMMIT_TAG}"
]]; then
  docker tag \
      "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:${CI_COMMIT_REF_SLUG}" \
      "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:latest";
  docker push "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:latest";
fi

# Push specific tag
if [[
    -n "${CI_COMMIT_TAG}"
]]; then
  docker tag \
      "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:latest" \
      "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:${CI_COMMIT_TAG}";
  docker push "${CODE_REGISTRY_IMAGE}/${IMAGE_NAME}:${CI_COMMIT_TAG}";
fi

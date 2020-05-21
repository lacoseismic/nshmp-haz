#!/bin/bash
# shellcheck disable=SC1090

source "$(dirname "${0}")/docker-config.inc.sh";
exit_status=${?};
[ "${exit_status}" -eq 0 ] || exit "${exit_status}";

# Download models to use
model_path=$(get_models "${MODEL}" "${NSHM_VERSION}");
exit_status=${?};
check_exit_status ${exit_status};

# Run web services
java -jar "${PROJECT}-ws.jar" \
    "-Dmicronaut.server.context-path=${CONTEXT_PATH}" \
    --model="/app/${model_path}";
exit_status=${?};
check_exit_status ${exit_status};

exit ${exit_status};

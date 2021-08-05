#!/bin/bash

##
# Run nshmp-haz
##
main() {
  # Get name of sites file
  sites_file=$(ls /app/sites.*);

  # Run nshmp-haz
  java "${JAVA_OPTS}" \
      -cp "/app/nshmp-haz.jar" \
      "gov.usgs.earthquake.nshmp.${CLASS_NAME}" \
      "${MODEL_PATH}" \
      "${sites_file}" \
      ${RETURN_PERIOD:+ "${RETURN_PERIOD}"} \
      ${IML:+ "${IML}"} \
      "${CONFIG_FILE}";
  exit_status=${?};
  check_exit_status "${exit_status}";

  # Move results to container volume
  move_to_output_volume;
  exit_status=${?};
  check_exit_status "${exit_status}";

  exit ${exit_status};
}

####
# Check current exit status.
#
# @param $1 exit_status {Integer}
#     Current exit status
####
check_exit_status() {
  local exit_status=${1};
  [ "${exit_status}" -eq 0 ] || exit "${exit_status}";
}

####
# Exit with an error message.
#
# @param $1 msg {String}
#     The message for exit
# @param $2 exit_status {Integer}
#     The exit status
####
error_exit() {
  local msg=${1}; shift;
  local exit_status=${1}
  echo "Error: ${msg}" >> /dev/stderr;
  exit "${exit_status}";
}

####
# Move artifacts to mounted volume.
#
# @status Integer
#     The status of moving the files.
####
move_to_output_volume() {
  local hazout;
  hazout=$(jq -r ".output.directory" "${CONFIG_FILE}");

  if [ "${hazout}" == null ]; then
    hazout="hazout";
  fi

  mv "${hazout}/*" "${OUTPUT_PATH}/.";
  return ${?};
}

# Run nshmp-haz
main "$@";

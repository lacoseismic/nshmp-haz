#!/bin/bash

####
# Docker entrypoint to run web services.
####

readonly CEUS="Central & Eastern US";
readonly CONTEXT_PATH="${CONTEXT_PATH:-/}";
readonly DEBUG="${DEUB:-false}";
readonly MODEL=$(echo "${MODEL:-${1}}"  | awk \{'print toupper($0)'\});
readonly NSHM_VERSION="${NSHM_VERSION:-master}";
readonly PROJECT="${PROJECT:-nshmp-haz-v2}";
readonly WUS="Western US";
readonly VERSION_2014B="v4.1.1";

####
# Start web services
#
# @param $1 nshm {String}
#     The NSHM to download.
# @param $1 nshm_version {String}
#     The version to download from GitHub.
#
# @status Integer
#     The result for get_model
####
main() {
  local nshm=${1};
  local nshm_version=${2};
  local model_path;
  local exit_status;

  if [ "${DEBUG}" == "true" ]; then
    set -x;
  fi;

  model_path=$(get_models "${nshm}" "${nshm_version}");
  exit_status=${?};

  echo "${model_path}";
  ls;
  ls "${model_path}";

  if [ "${exit_status}" -eq 0 ]; then
    java -jar "${PROJECT}-ws.jar" \
        "-Dmicronaut.server.context-path=${CONTEXT_PATH}" \
        --model="/app/${model_path}";
    exit_status=${?};
  fi

  return ${exit_status};
}

####
# Download a repository from Github.
#
# @param $1 repo {String}
#     The repository name
# @param $2 url {String}
#     The url to download
#
# @status Integer
#     The status of the curl call
####
download_repo() {
  local repo=${1};
  local url=${2};
  local exit_status;

  curl -L "${url}" | tar -xz;
  exit_status=${?};

  if [ ${exit_status} -eq 0 ]; then
    mv "${repo}-*" "${repo}";
  else
    error_exit "Could not download [${url}]" ${exit_status};
  fi

  return ${exit_status};
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
  local msg=${1};
  local exit_status=${2}
  echo "Error: ${msg}" >> /dev/stderr;
  exit "${exit_status}";
}

####
# Returns the model path for deagg-epsilon and hazard-2018.
#
# @param $1 nshm {String}
#     The NSHM to download.
# @param $1 nshm_version {String}
#     The version to download from GitHub.
#
# @return String
#     The model path
# @status Integer
#     The result of downloading the repository.
####
get_model() {
  local nshm=${1};
  local nshm_version=${2};
  local model;
  local url;
  local exit_status;

  if [ "${nshm_version}" == "null" ]; then
    return 0;
  fi

  case ${nshm} in
    "AK-2007")
      model="nshm-ak-2007";
      url="https://github.com/usgs/${model}/archive/${nshm_version}.tar.gz";
      ;;
    "CONUS-2008")
      model="nshm-cous-2008";
      url="https://github.com/usgs/${model}/archive/${nshm_version}.tar.gz";
      ;;
    "CONUS-2014")
      model="nshm-cous-2014";
      url="https://github.com/usgs/${model}/archive/${nshm_version}.tar.gz";
      ;;
    "CONUS-2014B")
      model="nshm-cous-2014";
      nshm_version="${VERSION_2014B}";
      url="https://github.com/usgs/${model}/archive/${nshm_version}.tar.gz";
      ;;
    "CONUS-2018")
      model="nshm-cous-2018";
      url="https://github.com/usgs/${model}/archive/${nshm_version}.tar.gz";
      ;;
    "HI-2020")
      model="nshm-hi-2020";
      url="https://github.com/usgs/${model}/archive/${nshm_version}.tar.gz";
      ;;
    *)
      error_exit "Model [${nshm}] not supported" 1;
      ;;
  esac

  download_repo "${url}";
  exit_status=${?};

  echo ${model};
  return ${exit_status}
}

####
# Get NSHMs for web services.
#
# @param $1 nshm {String}
#     The NSHM to download.
# @param $1 nshm_version {String}
#     The version to download from GitHub.
#
# @status Integer
#     The result for get_model
####
get_models() {
  local nshm=${1};
  local nshm_version=${2};
  local model_base_path="models";
  local nshm_path;
  local exit_status;

  if [ -d "${model_base_path}" ]; then
    rm -rf "${model_base_path:?}/*";
  else
    mkdir ${model_base_path};
  fi

  cd ${model_base_path} || error_exit "Could not change directory [${model_base_path}]" 1;
  nshm_path=$(get_model "${nshm}" "${nshm_version}");
  exit_status=${?};

  if [ ${exit_status} -eq 0 ]; then
    local model;
    local year;
    model="$(echo "${nshm}" | cut -d - -f1 | awk \{'print tolower($0)'\})";
    year="$(echo "${nshm}" | cut -d - -f2 | awk \{'print tolower($0)'\})";

    if [ "${model}" == 'conus' ]; then
      mkdir wus ceus;
      mv "${nshm_path}/${CEUS}" "ceus/${year}";
      mv "${nshm_path}/${WUS}" "wus/${year}";
      rm -r "${nshm_path}";
    else
      mkdir "${model}";
      mv "${nshm_path}" "${model}/${year}";
    fi
  fi

  cd ../;

  echo ${model_base_path};
  return ${exit_status};
}

####
# Run main
####
main "${MODEL}" "${NSHM_VERSION}";
exit_status=${?};

exit ${exit_status};

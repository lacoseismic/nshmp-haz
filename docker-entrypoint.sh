#!/bin/bash

####
# Docker entrypoint to run hazard code or web services.
####

set -o errexit;
set -o errtrace;

readonly WUS="Western US";

readonly CEUS="Central & Eastern US";

# check_config_file global return variable
CHECK_CONFIG_FILE_RETURN="";

# check_site_file global return variable
CHECK_SITE_FILE_RETURN="";

# get_cous_model global return variable
GET_COUS_MODEL_RETURN="";

# get_model global return variable
GET_MODEL_RETURN="";

# get_model_path global return variable
GET_MODEL_PATH_RETURN="";

# get_nshmp_program global return variable
GET_NSHMP_PROGRAM_RETURN="";

# Log file
readonly LOG_FILE="docker-entrypoint.log";

# Docker usage
readonly USAGE="
  docker run \\
      -e RUN_HAZARD=<true | false> \\
      -e PROGRAM=<deagg | deagg-epsilon | hazard | rate> \\
      -e MODEL=<WUS-20[08|14|18] | CEUS-20[08|14|18] | COUS-20[08|14|18] | AK-2007> \\
      -v /absolute/path/to/sites/file:/app/sites.<geojson | csv> \\
      -v /absolute/path/to/config/file:/app/config.json \\
      -v /absolute/path/to/output:/app/output \\
      code.chs.usgs.gov:5001/ghsc/nshmp/images/nshmp-haz-v2;
";

main() {
  # Set trap for uncaught errors
  trap 'error_exit "${BASH_COMMAND}" "$(< ${LOG_FILE})" "${USAGE}"' ERR;

  if [ ${RUN_HAZARD} = true ]; then
    run_hazard;
  else
    run_ws;
  fi
}

####
# Run hazard code.
# Globals:
#   (string) GET_MODEL_PATH_RETURN - The return of get_model_path
#   (string) GET_NSHMP_PROGRAM_RETURN - The return of get_nshmp_program
#   (string) CHECK_CONFIG_FILE_RETURN - The return of check_config_file
#   (string) CHECK_SITE_FILE_RETURN - The return of check_site_file
#   (numnber) IML - The intensity measure level for deagg-iml
#   (string) JAVA_XMS - Java initial memory
#   (string) JAVA_XMX - Java max memory
#   (string) MODEL - The nshm
#   (boolean) MOUNT_MODEL - Whether to mount the model instead of selecting model
#   (string) PROGRAM - The program to run
#   (number) RETURN_PERIOD - The return period for deagg
# Arguments:
#   None
# Returns:
#   None
####
run_hazard() {
  # Get Java class to run
  get_nshmp_program 2> ${LOG_FILE};
  local nshmp_program="${GET_NSHMP_PROGRAM_RETURN}";

  # Get model path
  local nshmp_model_path="";
  if [ ${MOUNT_MODEL} = true ]; then
    nshmp_model_path="model";
  else
    get_model_path 2> ${LOG_FILE};
    nshmp_model_path="${GET_MODEL_PATH_RETURN}";
  fi

  # Check site file
  check_sites_file 2> ${LOG_FILE};
  local site_file="${CHECK_SITE_FILE_RETURN}";

  # Check config file
  echo "{}" > ${CONFIG_FILE};
  check_config_file 2> ${LOG_FILE};
  local config_file="${CHECK_CONFIG_FILE_RETURN}";

  # Monitor log file
  tail -f ${LOG_FILE} &

  # Run nshmp-haz
  java -Xms${JAVA_XMS} -Xmx${JAVA_XMX} \
      -cp nshmp-haz.jar \
      gov.usgs.earthquake.nshmp.${nshmp_program} \
      "${nshmp_model_path}" \
      "${site_file}" \
      ${RETURN_PERIOD:+ "${RETURN_PERIOD}"} \
      ${IML:+ "${IML}"} \
      "${config_file}" 2> ${LOG_FILE} || \
      error_exit "Failed running nshmp-haz" "$(tail -n 55 ${LOG_FILE})" "${USAGE}";

  # Move artifacts to mounted volume
  move_to_output_volume 2> ${LOG_FILE};
}

####
# Run web services in Tomcat.
# Globals:
#   (string) GET_COUS_MODEL_RETURN - The return of get_cous_model
#   (string) HAZ_HOME - The home for running hazard code
#   (string) LOG_FILE - The log file
#   (string) PROJECT - The project name
#   (string) TOMCAT_WEBAPPS - The Tomcat webapps dir
#   (string) WS_HOME - The home for running the web services
# Arguments:
#   None
# Returns:
#   None
####
run_ws() {
  cd ${TOMCAT_WEBAPPS} 2> ${LOG_FILE};
  mkdir ${PROJECT} 2> ${LOG_FILE};
  cd ${PROJECT} 2> ${LOG_FILE};
  cp ${HAZ_HOME}/${PROJECT}.war . 2> ${LOG_FILE};
  unzip ${PROJECT}.war 2> ${LOG_FILE};

  mkdir models 2> ${LOG_FILE};
  cd models 2> ${LOG_FILE};
  get_cous_model 2> ${LOG_FILE};
  local nshm_model="${GET_COUS_MODEL_RETURN}";
  local model="$(echo ${nshm_model} | cut -d - -f1 | awk {'print tolower($0)'})" 2> ${LOG_FILE};
  local year="$(echo ${nshm_model} | cut -d - -f2 | awk {'print tolower($0)'})" 2> ${LOG_FILE};

  if [ ${model} == 'cous' ]; then
    mkdir wus ceus 2> ${LOG_FILE};
    mv ${nshm_model}/${CEUS} ceus/${year} 2> ${LOG_FILE};
    mv ${nshm_model}/${WUS} wus/${year} 2> ${LOG_FILE};
  else
    mkdir ${model} 2> ${LOG_FILE};
    mv ${nshm_model} ${model}/${year} ${LOG_FILE};
  fi

  cd ${WS_HOME} 2> ${LOG_FILE};

  catalina.sh run 2>&1;
}

####
# Check that the config file is valid json.
# Globals:
#   (string) CONFIG_FILE - The config file name
#   (string) CHECK_CONFIG_FILE_RETURN - The return for the function
# Arguments:
#   None
# Returns:
#   (string) CHECK_CONFIG_FILE_RETURN - The config file name
####
check_config_file() {
  # Check if file is valid JSON
  jq empty < ${CONFIG_FILE} 2> ${LOG_FILE} || \
      error_exit "Config file is not valid JSON" "$(< ${LOG_FILE})" "${USAGE}";

  # Return
  CHECK_CONFIG_FILE_RETURN=${CONFIG_FILE};
}

####
# Check that the sites file is valid.
# Globals:
#   (string) CHECK_SITE_FILE_RETURN - The return for the function
# Arguments:
#   None
# Returns:
#   (string) CHECK_SITE_FILE_RETURN - The site file name
####
check_sites_file() {
  local site_file=$(ls sites*) 2> ${LOG_FILE} || \
      error_exit "Site file does not exist." "$(< ${LOG_FILE})" "${USAGE}";

  # Check if valid JSON or ASCII file
  case ${site_file} in
    *.geojson)
      jq empty < ${site_file} 2> ${LOG_FILE} || \
          error_exit "Site file [${site_file}] is not valid JSON" "$(< ${LOG_FILE})" "${USAGE}";
      ;;
    *.csv)
      if [[ "$(file ${site_file} -b)" != "ASCII text"* ]]; then
        error_exit \
            "Site file [${site_file}] is not valid ASCII" \
            "Site file is not valid ASCII" \
            "${USAGE}";
      fi
      ;;
    *)
      error_exit "Bad site file [${site_file}]." "Bad site file." "${USAGE}";
      ;;
  esac

  # Return
  CHECK_SITE_FILE_RETURN=${site_file};
}

####
# Download a repository from Github.
# Globals:
#   (string) LOG_FILE - The log file
# Arguments:
#   (string) user - The Github user
#   (string) repo - The project to download
#   (string) version - The version to download
#   (string) directory - The direcotry name for repo download
# Returns:
#   None
####
download_repo() {
  local usage="download_repo <user> <repo> <version>";

  local user=${1};
  local repo=${2};
  local version=${3};
  local directory=${4};
  local url="https://github.com/${user}/${repo}/archive/${version}.tar.gz";

  if [ ${version} == "null" ]; then
    printf "\n Skipping download of [${user}/${repo}]\n";
    return;
  fi

  printf "\n Downloading [${url}] \n\n";

  if [ -z "${directory}" ]; then
    directory=${repo};
  fi

  curl -L ${url} | tar -xz 2> ${LOG_FILE} || \
      error_exit "Could not download [${url}]" "$(< ${LOG_FILE})" "${usage}";

  mv ${repo}-${version#v*} ${directory};
}

####
# Exit script with error.
# Globals:
#   None
# Arguments:
#   (string) err - The error message
#   (string) logs - The log for the error
#   (string) usage - The Docker usage
# Returns:
#   None
####
error_exit() {
  local err="${1}";
  local logs="${2}";
  local usage="${3}";

  local message="
    Error:
    ${err}

    ----------
    Logs:

    ${logs}

    ----------
    Usage:

    ${usage}

  ";

  printf "${message}";

  exit -1;
}

####
# Returns the model path for deagg-epsilon and hazard-2018.
# Globals:
#   (string) MODEL - The nshm
#   (string) PROGRAM - The program to run
#   (string) GET_COUS_MODEL_RETURN - The return for the function
#   (string) NSHM_VERSION - The NSHM repository version
# Arguments:
#   None
# Returns:
#   (string) GET_COUS_MODEL_RETURN - The cous model path
####
get_cous_model() {
  local model="";
  local version="${NSHM_VERSION}";

  case ${MODEL} in
    "AK-2007")
      model="nshm-ak-2007";
      ;;
    "COUS-2008")
      model="nshm-cous-2008";
      ;;
    "COUS-2014")
      model="nshm-cous-2014";
      ;;
    "COUS-2014B")
      model="nshm-cous-2014";
      version="v4.1.1";
      ;;
    "COUS-2018")
      model="nshm-cous-2018";
      ;;
    "HI-2020")
      model="hi-2020";
      ;;
    *)
      error_exit \
          "Model [${MODEL}] not supported for program [${PROGRAM}]" \
          "Model not supported" \
          "${USAGE}";
      ;;
  esac

  download_repo "usgs" ${model} ${version};

  # Return
  GET_COUS_MODEL_RETURN=${model};
}

####
# Returns the model path for all programs except deagg-epsilon.
# Globals:
#   (string) MODEL - The nshm
#   (string) PROGRAM - The program to run
#   (string) GET_MODEL_RETURN - The return for the function
#   (string) NSHM_VERSION - The NSHM repository version
# Arguments:
#   None
# Returns:
#   (string) GET_MODEL_RETURN - The model path
####
get_model() {
  local model=""
  local model_path=""

  case ${MODEL} in
    "AK-2007")
      model="nshm-ak-2007";
      model_path="${model}";
      ;;
    "HI-2020")
      model="hi-2020";
      model_path="${model}";
      ;;
    "CEUS-2008")
      model="nshm-cous-2008";
      model_path="${model}/${CEUS}/";
      ;;
    "CEUS-2014")
      model="nshm-cous-2014";
      model_path="${model}/${CEUS}/";
      ;;
    "CEUS-2018")
      model="nshm-cous-2018";
      model_path="${model}/${CEUS}/";
      ;;
    "WUS-2008")
      model="nshm-cous-2008";
      model_path="${model}/${WUS}/";
      ;;
    "WUS-2014")
      model="nshm-cous-2014";
      model_path="${model}/${WUS}/";
      ;;
    "WUS-2018")
      model="nshm-cous-2018";
      model_path="${model}/${WUS}/";
      ;;
    *)
      error_exit \
          "Model [${MODEL}] not supported for program [${PROGRAM}]" \
          "Model not supported" \
          "${USAGE}";
      ;;
  esac

  download_repo "usgs" ${model} ${version};

  # Return
  GET_MODEL_RETURN=${model_path}
}

####
# Returns the path to the model.
# Globals:
#   (string) PROGRAM - The program to run
#   (string) GET_MODEL_PATH_RETURN - The return value for the funciton
#   (string) GET_COUS_MODEL_RETURN -  The return for get_cous_model
#   (string) GET_MODEL_RETURN - The return for get_model
# Arguments:
#   None
# Returns:
#   (string) GET_MODEL_PATH_RETURN - The model path
####
get_model_path() {
  local nshmp_model_path="";

  if [ ${PROGRAM} == 'deagg-epsilon' ] || [ ${PROGRAM} == 'hazard-2018' ]; then
    get_cous_model 2> ${LOG_FILE};
    nshmp_model_path="${GET_COUS_MODEL_RETURN}";
  else
    get_model 2> ${LOG_FILE};
    nshmp_model_path="${GET_MODEL_RETURN}";
  fi

  # Return
  GET_MODEL_PATH_RETURN=${nshmp_model_path};
}

####
# Returns to nshmp-haz Java class to call.
# Globals:
#   (string) PROGRAM - The program to run: deagg | deagg-epsilon | hazard | rate
#   (string) GET_NSHMP_PROGRAM_RETURN - The return value for the function
# Arguments:
#   None
# Returns:
#   (string) GET_NSHMP_PROGRAM_RETURN - The Java class to call
####
get_nshmp_program() {
  local nshmp_program="";

  case ${PROGRAM} in
    "deagg")
      nshmp_program="DeaggCalc";
      ;;
    "deagg-epsilon")
      nshmp_program="DeaggEpsilon";
      ;;
    "deagg-iml")
      nshmp_program="DeaggIml";
      ;;
    "hazard-2018")
      nshmp_program="Hazard2018";
      ;;
    "hazard")
      nshmp_program="HazardCalc";
      ;;
    "rate")
      nshmp_program="RateCalc";
      ;;
    *)
      error_exit "Program [${PROGRAM}] not supported" "Program not supported" "${USAGE}";
      ;;
  esac

  # Return
  GET_NSHMP_PROGRAM_RETURN=${nshmp_program};
}

####
# Move artifacts to mounted volume.
# Globals:
#   (string) CONFIG_FILE - The config file name
# Arguments:
#   None
# Returns:
#   None
####
move_to_output_volume() {
  # Get output directory
  local hazout=$(jq -r '.output.directory' ${CONFIG_FILE});

  if [ ${hazout} == null ]; then
    hazout="hazout";
  fi

  # Copy output to volume output
  cp -r ${hazout}/* output/. 2> ${LOG_FILE};
}

####
# Run main
####
main "$@";

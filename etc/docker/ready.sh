#!/bin/bash -e

# check that the slave is online

if [ -z "${JENKINS_URL}" ] ; then
  echo "ERROR: JENKINS_URL not set"
  exit 1
fi

if [ -z "${HOSTNAME}" ] ; then
  echo "ERROR: HOSTNAME not set"
  exit 1
fi

if ! type curl > /dev/null 2>&1; then
  echo "[ERROR] - curl not found in PATH"
  exit 1
fi

readonly SLAVE_STATUS_OUT=$(mktemp -t XXXcurl-out)
readonly HTTP_STATUS=$(curl \
  -o ${SLAVE_STATUS_OUT} \
  -w "%{http_code}" \
  "${JENKINS_URL}/computer/${HOSTNAME}/api/xml?xpath=*/offline\[1\]" \
  2> /dev/null)

if [ "${HTTP_STATUS}" != "200" ] ; then
  echo "[ERROR] - Unable to get slave status - HTTP_STATUS=${HTTP_STATUS}"
fi

readonly SLAVE_STATUS=$(cat ${SLAVE_STATUS_OUT})

if [ "${SLAVE_STATUS}" != "<offline>false</offline>" ] ; then
  echo "[ERROR] - Slave is not online"
  exit 1
fi

if [ "${1}" = "--mounts" ] ; then
  shift
else
  # no mounts passed, stop here.
  exit 0
fi

# block until all mounts are available
for mount in ${@}
do
  while ! mountpoint -q "${mount}" ; do
    echo "[INFO] - mountpoint not available "${mount}
    sleep 10
  done
done

# make sure the custom workspace directory exists
if [ !-z "${CUSTOM_WORKSPACE}" ] && [ !-z "${PIPELINE_NAME}" ] ; then
  mkdir -p ${CUSTOM_WORKSPACE}/${PIPELINE_NAME}_${HOSTNAME}
fi
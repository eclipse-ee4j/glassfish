#!/bin/bash
#
# Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#

set -e
# Change to -x for echoing commands
set +x

catch() {
  if [ "$1" != "0" ]; then
    "${S1AS_HOME}"/bin/asadmin stop-domain --kill=true --force=true domain1 || true;
    echo "Error $1 occurred on $2";
    exit $1;
  fi
}

install_glassfish() {
  mvn clean -N org.apache.maven.plugins:maven-dependency-plugin:3.2.0:copy \
  -Dartifact=org.glassfish.main.distributions:glassfish:${GF_VERSION}:zip \
  -Dmdep.stripVersion=true \
  -DoutputDirectory=${WORKSPACE}/bundles \
  -Pstaging
}

install_jacoco() {
  mvn -N org.apache.maven.plugins:maven-dependency-plugin:3.2.0:copy \
  -Dartifact=org.jacoco:org.jacoco.agent:0.8.8:jar:runtime \
  -Dmdep.stripVersion=true \
  -Dmdep.stripClassifier=true \
  -DoutputDirectory=${WORKSPACE}/bundles \
  -Pstaging
}

####################################

trap 'catch $? $LINENO' EXIT

echo "This script can be temporarily used for local testing until we integrate current set of tests to Maven";
echo "First argument is a version of GlassFish used for testing. It will be downloaded by Maven command";
echo "Second argument is a test set id, one of:
cdi_all, ql_gf_full_profile_all, \n
\n
web_jsp, deployment_all, \n
ejb_group_1. ejb_group_2, ejb_group_3, ejb_group_embedded, \n
cdi_all, ql_gf_full_profile_all, ql_gf_nucleus_all, \n
ql_gf_web_profile_all, nucleus_admin_all, jdbc_all, batch_all, persistence_all, webservice_all, \n
connector_group_1, connector_group_2, connector_group_3, connector_group_4";

echo "If you need to use a different JAVA_HOME than is used by your system, export it or edit this script.";
echo "";


test="${1}"
if [ -z "${test}" ]; then
  echo "No test supplied."
  exit 1;
fi

if [ -z "${JAVA_HOME}" ]; then
  export JAVA_HOME=/usr/lib/jvm/jdk21
fi
export PATH="${JAVA_HOME}/bin:${PATH}"

if [ -z "${MVN_REPOSITORY}" ]; then
  export MVN_REPOSITORY="${HOME}/.m2/repository"
fi

export M2_HOME="${M2_HOME=$(realpath $(dirname $(realpath $(which mvn)))/..)}"
export APS_HOME="$(pwd)/appserver/tests/appserv-tests"

if [ -z "${2}" ]; then
  export GF_VERSION="$(mvn help:evaluate -f "${APS_HOME}/pom.xml" -Dexpression=project.version -q -DforceStdout -Pstaging)"
else
  export GF_VERSION="$2"
fi

if [ ! -z "${3}" ]; then
   export GLASSFISH_SUSPEND="--suspend"
else
   unset GLASSFISH_SUSPEND
fi

export JACOCO_ENABLED="true"
export WORKSPACE="$(pwd)/target"
export TEST_RUN_LOG="${WORKSPACE}/tests-run.log"
export GLASSFISH_HOME="${WORKSPACE}/glassfish8"
export CLASSPATH="${GLASSFISH_HOME}/javadb"
export S1AS_HOME="${GLASSFISH_HOME}/glassfish"

# These values can be preset by the caller
export PORT_ADMIN="${PORT_ADMIN=4848}"
export PORT_HTTP="${PORT_HTTP=8080}"
export PORT_HTTPS="${PORT_HTTPS=8181}"
if [ ! -f "${WORKSPACE}/bundles/glassfish.zip" ]; then
  install_glassfish;
fi
install_jacoco;

rm -rf "${GLASSFISH_HOME}"
rm -f ./appserver/tests/appserv-tests/test_resultsValid.xml
rm -f ./appserver/tests/appserv-tests/test_results.xml
./appserver/tests/gftest.sh run_test "${test}"


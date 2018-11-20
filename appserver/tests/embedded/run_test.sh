#!/bin/bash -ex
#
# Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

test_run_embedded(){
  cd ${WORKSPACE}
  mvn -DskipTests=true clean install
  EMBEDDED_WORKSPACE=${WORKSPACE}/appserver/extras/embedded
  cd ${EMBEDDED_WORKSPACE}/all
  mvn -DskipTests=true clean install | tee ${TEST_RUN_LOG}
  cd ${EMBEDDED_WORKSPACE}/nucleus
  mvn -DskipTests=true clean install | tee -a ${TEST_RUN_LOG}
  cd ${EMBEDDED_WORKSPACE}/web
  mvn -DskipTests=true clean install | tee -a ${TEST_RUN_LOG}
  cd ${WORKSPACE}/appserver/tests/embedded/maven-plugin/remoteejbs
  mvn -DskipTests=true clean verify | tee -a ${TEST_RUN_LOG}
  cd ${WORKSPACE}/appserver/tests/embedded/maven-plugin/mdb
  mvn -DskipTests=true clean verify | tee -a ${TEST_RUN_LOG}
  cd ${WORKSPACE}/appserver/tests/embedded
  mvn -Dbuild=snapshot -Dmaven.test.failure.ignore=true clean verify | tee -a ${TEST_RUN_LOG}
  merge_junits
}

merge_junits(){
  TEST_ID="embedded_all"
  rm -rf ${WORKSPACE}/results || true
  mkdir -p ${WORKSPACE}/results/junitreports
  JUD="${WORKSPACE}/results/junitreports/test_results_junit.xml"
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > ${JUD}
  echo "<testsuites>" >> ${JUD}
  for i in `find . -type d -name "surefire-reports"`
  do    
    ls -d -1 ${i}/*.xml | xargs cat | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\" *?>//g' >> ${JUD}
  done
  echo "</testsuites>" >> ${JUD}
  ${SED} -i 's/\([a-zA-Z-]\w*\)\./\1-/g' ${JUD}
  ${SED} -i "s/\bclassname=\"/classname=\"${TEST_ID}./g" ${JUD}
}

run_test_id(){
  case ${TEST_ID} in
    embedded_all)
      unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
   	  test_run_embedded;;
  esac
}

list_test_ids(){
	echo embedded_all
}

OPT=${1}
TEST_ID=${2}
source `dirname ${0}`/../common_test.sh

case ${OPT} in
	list_test_ids )
		list_test_ids;;
	run_test_id )
		trap "copy_test_artifacts ${TEST_ID}" EXIT
    run_test_id ${TEST_ID} ;;
esac

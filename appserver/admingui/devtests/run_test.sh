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

list_test_ids(){
  echo admingui_all
}

merge_junit_xmls(){
  JUD_DIR=${1}
  JUD=${WORKSPACE}/results/junitreports/test_results_junit.xml
  rm -f ${JUD} || true
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> ${JUD}
  echo "<testsuites>" >> ${JUD}
  find ${JUD_DIR} -name "*.xml" -type f -exec cat '{}' \; | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\" ?>//g' >> ${JUD}
  echo -e "\n</testsuites>" >> ${JUD}
}

test_run(){
  export PWD=$(date | md5sum | cut -d' ' -f 1)
  touch ${APS_HOME}/password.txt
  chmod 600 ${APS_HOME}/password.txt
  echo "AS_ADMIN_PASSWORD=" > ${APS_HOME}/password.txt
  echo "AS_ADMIN_NEWPASSWORD=${PWD}" >> ${APS_HOME}/password.txt
  LOCKFILE=${S1AS_HOME}/domains/domain1/imq/instances/imqbroker/lockv
  if [ -f ${LOCKFILE} ];then
    rm ${LOCKFILE}
  fi
  ${S1AS_HOME}/bin/asadmin --user admin --passwordfile ${APS_HOME}/password.txt change-admin-password
  ${S1AS_HOME}/bin/asadmin start-domain
  echo "AS_ADMIN_PASSWORD=${PWD}" > ${APS_HOME}/password.txt
  ${S1AS_HOME}/bin/asadmin --passwordfile ${APS_HOME}/password.txt enable-secure-admin
  ${S1AS_HOME}/bin/asadmin restart-domain
  cd ${APS_HOME}/../../admingui/devtests/
  export DISPLAY=127.0.0.1:1	
  mvn -DsecureAdmin=true -Dpasswordfile=${APS_HOME}/password.txt test | tee ${TEST_RUN_LOG}
  ${S1AS_HOME}/bin/asadmin stop-domain
  rm -rf ${APS_HOME}/password.txt
}

run_test_id(){
  source `dirname ${0}`/../../tests/common_test.sh
  kill_process
  delete_gf
  unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip "${WORKSPACE}/bundles/tests-maven-repo.zip -d ${WORKSPACE}/repository"
  cd `dirname ${0}`
  test_init
  test_run
  merge_junit_xmls ${WORKSPACE}/appserver/admingui/devtests/target/surefire-reports
  change_junit_report_class_names
}

post_test_run(){
  cp $TEST_RUN_LOG ${WORKSPACE}/results/ || true
  cp ${WORKSPACE}/glassfish5/glassfish/domains/domain1/logs/server.log* ${WORKSPACE}/results/ || true
  cp ${WORKSPACE}/appserver/admingui/devtests/target/surefire-reports/*.png ${WORKSPACE}/results/ || true
  delete_bundle
}

OPT=${1}
TEST_ID=$2
case ${OPT} in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    trap post_test_run SIGTERM SIGABRT EXIT
    run_test_id ${TEST_ID} ;;
esac

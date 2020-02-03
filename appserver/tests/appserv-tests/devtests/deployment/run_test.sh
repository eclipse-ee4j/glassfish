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

test_run(){
  if [[ ${1} = "deployment_cluster_all" ]]; then
    DEPL_TARGET=CLUSTER
    export DEPL_TARGET
  fi

  export HUDSON=true
  export ROOT=`pwd`

  # The first command-line argument is the (optional) predecessor job from which
  # to get the revision under test and the glassfish.zip file to expand.
  # Default: gf-trunk-build-continuous

  if [ -x "/usr/bin/cygpath" ]
  then
    ROOT=`cygpath -d ${ROOT}`
    echo "Windows ROOT: ${ROOT}"
    export CYGWIN=nontsec
  fi
  antTarget="all-ee"
  if [ -z "${DEPL_TARGET}" ]
  then
      ${S1AS_HOME}/bin/asadmin start-domain
      antTarget="all"
  fi
  # Get rid of any lingering password file from an earlier run
  rm ~/.asadminpass || true

  time ant ${antTarget} | tee ${TEST_RUN_LOG}
  antStatus=${?}
  # Copy the report to $APS_HOME
  cp tests-results.xml ${APS_HOME}/tests-results.xml
  cp results.html ${APS_HOME}/test_results.html

  if [ -z "${DEPL_TARGET}" ]
  then
      ${S1AS_HOME}/bin/asadmin stop-domain
  fi
   if [[ ${1} = "deployment_cluster_all" ]]; then
      cp -r ${APS_HOME}/devtests/deployment/server-logs/ ${WORKSPACE}/results/
  fi  
  echo DEPL_TARGET is ${DEPL_TARGET}
}

generate_junit_report_deployment(){
  printf "\n%s \n\n" "===== GENERATE JUNIT REPORT ====="
  TD=${APS_HOME}/tests-results.xml
  JUD=${APS_HOME}/test_results_junit.xml
  TESTSUITE_NAME=${1}

  cat ${TD} | ${AWK} -v suitename=${TESTSUITE_NAME} '
  BEGIN{
    RS="</test>";
    FS="=";
    print "<testsuites>";
    print " <testsuite>";
    id=0;
  }
  {
    if ( NF > 1 ) {
      print "  <testcase classname=\"DeploymentTest\" name=\"" \
        substr($2,2,index($2,"description")-4) \
        id \
        "\">"		
      
      # searching for FAILED in field 4,5,6
      # if not found, test PASSED
      failure=1
      match($6,"FAILED")
      if( RLENGTH == -1) {
        match($5,"FAILED")
        if( RLENGTH == -1) {
          match($4,"FAILED")
          if( RLENGTH == -1) {
            failure=0;
          }
        }
      } 
      if( failure == 1 ) {
        print "   <failure type=\"testfailure\"/>"
      }
      
      print "  </testcase>"
      id++;
    }
  }
  END{
    print " </testsuite>";
    print "</testsuites>";
  }' > ${JUD}
  cp ${JUD} ${WORKSPACE}/results/junitreports
}

run_test_id(){
  unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
  cd `dirname ${0}`
  test_init
  test_run ${1}
  check_successful_run
  generate_junit_report_deployment ${1}
  change_junit_report_class_names
}

list_test_ids(){
  echo deployment_all deployment_cluster_all
}

OPT=${1}
TEST_ID=${2}
source `dirname ${0}`/../../../common_test.sh

case ${OPT} in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    trap "copy_test_artifacts ${TEST_ID}" EXIT
    run_test_id ${TEST_ID} ;;
esac

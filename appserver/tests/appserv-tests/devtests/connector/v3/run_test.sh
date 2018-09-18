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

#Contract 1. returns the TEST ID, which you assigned in step 3.a
list_test_ids(){
  echo connector_all connector_group_1 connector_group_2 connector_group_3 connector_group_4
}
 
test_run(){
  #test functions goes here, maven test or ant test etc.
  export HUDSON=true
  export ROOT=`pwd`
  echo $ROOT
  ant startDomain startDerby
  cd $ROOT
  echo Running target: $TARGET
  time ant clean-all start-record $TARGET stop-record report | tee $TEST_RUN_LOG
  antStatus=$?
  ant stopDomain stopDerby
  if [ $antStatus -ne 0 ]
  then
      ps -ef
      exit $antStatus
  fi
}

#Contract 2. does the clean up, downloads the tests/build sources and eventually runs tests
run_test_id(){
  #a common util script located at main/appserver/tests/common_test.sh
  source `dirname $0`/../../../../common_test.sh
  kill_process
  delete_gf
  download_test_resources glassfish.zip version-info.txt
  unzip_test_resources $WORKSPACE/bundles/glassfish.zip
  cd `dirname $0`
  test_init
  get_test_target $1
  export ROOT=`pwd`
  export TEST_RUN_LOG=$ROOT/tests-run.log
  #run the actual test function
  test_run
  check_successful_run
  generate_junit_report $1
  change_junit_report_class_names
}

post_test_run(){
     copy_test_artifects
     upload_test_results
     delete_bundle
     cd -
}

get_test_target(){
	case $1 in
		connector_all )
			TARGET=all
			export TARGET;;
                * )
                       TARGET=$1
                       export TARGET;;
	esac

}
 
#Contract 3. script init code.
OPT=$1
TEST_ID=$2
case $OPT in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    trap post_test_run EXIT
    run_test_id $TEST_ID ;;
esac

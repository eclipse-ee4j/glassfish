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
  echo webservice_all
}

test_run(){
  ${S1AS_HOME}/bin/asadmin start-domain
  cd ${APS_HOME}/devtests/webservice
  ant ${TARGET} | tee ${TEST_RUN_LOG}
  ${S1AS_HOME}/bin/asadmin stop-domain
}

run_test_id(){
  source `dirname ${0}`/../../../common_test.sh
  kill_process
  delete_gf
  unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
  cd `dirname ${0}`
  test_init
  get_test_target ${1}
  test_run
  check_successful_run
  generate_junit_report ${1}
  change_junit_report_class_names
}

post_test_run(){
  copy_test_artifacts ${TEST_ID}
  delete_bundle
  cd -
}

get_test_target(){
	case ${1} in
		webservice_all )
			TARGET=all
			export TARGET;;
	esac
}

OPT=${1}
TEST_ID=${2}
case ${OPT} in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    trap post_test_run EXIT
    run_test_id ${TEST_ID} ;;
esac

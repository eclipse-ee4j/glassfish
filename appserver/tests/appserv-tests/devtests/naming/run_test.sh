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
  echo naming_all
}

test_run(){
  ${S1AS_HOME}/bin/asadmin start-domain
  ${S1AS_HOME}/bin/asadmin start-database
  cd ${APS_HOME}/devtests/naming
  PROXY_HOST=`echo ${http_proxy} | cut -d':' -f2 | ${SED} 's/\/\///g'`
  PROXY_PORT=`echo ${http_proxy} | cut -d':' -f3 | ${SED} 's/\///g'`
  ANT_OPTS="${ANT_OPTS} \
  -Dhttp.proxyHost=${PROXY_HOST} \
  -Dhttp.proxyPort=${PROXY_PORT} \
  -Dhttp.noProxyHosts='127.0.0.1|localhost|*.oracle.com' \
  -Dhttps.proxyHost=${PROXY_HOST} \
  -Dhttps.proxyPort=${PROXY_PORT} \
  -Dhttps.noProxyHosts='127.0.0.1|localhost|*.oracle.com'"
  export ANT_OPTS
  echo "ANT_OPTS=${ANT_OPTS}"
  ant ${TARGET} | tee ${TEST_RUN_LOG}
  ${S1AS_HOME}/bin/asadmin stop-database
  ${S1AS_HOME}/bin/asadmin stop-domain
}

run_test_id(){
  unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
  cd `dirname ${0}`
  test_init
  get_test_target ${1}
  test_run
  generate_junit_report ${1}
  change_junit_report_class_names
}

get_test_target(){
	case $1 in
		naming_all )
			TARGET=all
			export TARGET;;
	esac
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

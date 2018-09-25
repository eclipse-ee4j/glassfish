#!/bin/bash
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

	cp -f ${APS_HOME}/devtests/security/ldap/opends/X500Signer.jar ${OPENDS_HOME}/lib

	# Configure and start OpenDS using the default ports
	${OPENDS_HOME}/setup \
    -i \
    -v \
    -n \
    -p 1389 \
    --adminConnectorPort 4444 \
    -x 1689 \
    -w dmanager \
    -b "dc=sfbay,dc=sun,dc=com" \
    -Z 1636 \
    --useJavaKeystore ${S1AS_HOME}/domains/domain1/config/keystore.jks \
    -W changeit \
    -N s1as

	${S1AS_HOME}/bin/asadmin start-database
	${S1AS_HOME}/bin/asadmin start-domain
	cd ${APS_HOME}/devtests/security

	ant ${TARGET} | tee ${TEST_RUN_LOG}

	${S1AS_HOME}/bin/asadmin stop-domain
	${S1AS_HOME}/bin/asadmin stop-database
	${OPENDS_HOME}/bin/stop-ds \
    -p 4444 \
    -D "cn=Directory Manager" \
    -w dmanager \
    -P ${OPENDS_HOME}/config/admin-truststore \
    -U ${OPENDS_HOME}/config/admin-keystore.pin

	#egrep 'FAILED= *0' ${TEST_RUN_LOG}
	#egrep 'DID NOT RUN= *0' ${TEST_RUN_LOG}
	cd -
}

get_test_target(){
	case $1 in
		security_all )
			TARGET=all
			export TARGET;;
	esac
}

merge_result_files(){
	cat ${APS_HOME}/test_resultsValid.xml ${APS_HOME}/security-gtest-results.xml > ${APS_HOME}/temp.xml
	mv ${APS_HOME}/temp.xml ${APS_HOME}/test_resultsValid.xml
}

run_test_id(){
  # setup opendj (fork of opends)
  wget https://github.com/OpenIdentityPlatform/OpenDJ/releases/download/4.1.10/opendj-4.1.10.zip
  unzip opendj-4.1.10.zip
  export OPENDS_HOME=${PWD}/opendj

	unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
	cd `dirname ${0}`
	test_init
	get_test_target ${1}
	test_run
	merge_result_files
	check_successful_run
  generate_junit_report ${1}
  change_junit_report_class_names
}

list_test_ids(){
	echo security_all
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

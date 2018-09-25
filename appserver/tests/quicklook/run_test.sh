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

copy_ql_results(){
	if [[ ${1} = "ql_gf_web_profile_all" || ${1} = "ql_gf_full_profile_all" || "ql_gf_embedded_profile_all" = ${1} ]]; then
		cp ${WORKSPACE}/glassfish5/glassfish/domains/domain1/logs/server.log* ${WORKSPACE}/results/ || true
		cp -r ${WORKSPACE}/appserver/tests/quicklook/test-output/* ${WORKSPACE}/results/
		cp ${WORKSPACE}/appserver/tests/quicklook/test-output/TESTS-TestSuites.xml ${WORKSPACE}/results/junitreports/test_results_junit.xml
		cp ${WORKSPACE}/appserver/tests/quicklook/quicklook_summary.txt ${WORKSPACE}/results || true
	else
		cp ${WORKSPACE}/nucleus/domains/domain1/logs/server.log* ${WORKSPACE}/results
	fi
	cp ${TEST_RUN_LOG} ${WORKSPACE}/results/
	tar -cvf ${WORKSPACE}/${1}-results.tar.gz ${WORKSPACE}/results
	change_junit_report_class_names
}

run_test_id(){
	mkdir ${WORKSPACE}/repository
	if [[ ${1} = "ql_gf_full_profile_all" ]]; then
		unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
		cd ${WORKSPACE}/appserver/tests/quicklook/
		mvn -Dglassfish.home=${S1AS_HOME} -Ptest_gd_security,report test | tee ${TEST_RUN_LOG}
	elif [[ ${1} = "ql_gf_nucleus_all" || ${1} = "nucleus_admin_all" ]]; then
		unzip_test_resources ${WORKSPACE}/bundles/nucleus-new.zip
		if [[ ${1} = "ql_gf_nucleus_all" ]]; then
			cd ${WORKSPACE}/nucleus/tests/quicklook
		elif [[ ${1} = "nucleus_admin_all"  ]]; then
			cd ${WORKSPACE}/nucleus/tests/admin
		fi
		mvn -Dmaven.test.failure.ignore=true -Dnucleus.home=${WORKSPACE}/nucleus clean test | tee ${TEST_RUN_LOG}
		if [[ ${1} = "ql_gf_nucleus_all" ]]; then
			merge_junit_xmls ${WORKSPACE}/nucleus/tests/quicklook/target/surefire-reports/junitreports
		elif [[ ${1} = "nucleus_admin_all"  ]]; then
			merge_junit_xmls ${WORKSPACE}/nucleus/tests/admin/target/surefire-reports/junitreports
		fi
	elif [[ ${1} = "ql_gf_web_profile_all" || $1 = "ql_gf_embedded_profile_all" ]]; then
    unzip_test_resources ${WORKSPACE}/bundles/web.zip
		cd ${WORKSPACE}/appserver/tests/quicklook/
		if [[ ${1} = "ql_gf_web_profile_all" ]]; then
			mvn -Dglassfish.home=${S1AS_HOME} -Ptest_wd_security,report test | tee ${TEST_RUN_LOG}
		elif [[ ${1} = "ql_gf_embedded_profile_all" ]]; then
			mvn -Dglassfish.home=${S1AS_HOME} -Ptest_em,report test | tee ${TEST_RUN_LOG}
		fi
	else
		echo "Invalid Test Id"
		exit 1
	fi
}

merge_junit_xmls(){
  JUD_DIR=${1}
  JUD=${WORKSPACE}/results/junitreports/test_results_junit.xml
  rm -f ${JUD} || true
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> ${JUD}
  echo "<testsuites>" >> ${JUD}
  find ${JUD_DIR} -name "*.xml" -type f -exec cat '{}' \; | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\"?>//g' >> ${JUD}
  echo "</testsuites>" >> ${JUD}
}

list_test_ids(){
	echo ql_gf_full_profile_all ql_gf_nucleus_all ql_gf_web_profile_all ql_gf_embedded_profile_all nucleus_admin_all
}

OPT=${1}
TEST_ID=${2}
source `dirname ${0}`/../common_test.sh
mkdir -p ${WORKSPACE}/results/junitreports

case ${OPT} in
	list_test_ids )
		list_test_ids;;
	run_test_id )
    trap "copy_ql_results ${TEST_ID}" EXIT
		run_test_id ${TEST_ID} ;;
esac

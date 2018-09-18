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
	cp $WORKSPACE/glassfish5/glassfish/domains/domain1/logs/server.log* $WORKSPACE/results/ || true
	cp $TEST_RUN_LOG $WORKSPACE/results/
	cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
	cp -r test-output/* $WORKSPACE/results/
	cp test-output/TESTS-TestSuites.xml $WORKSPACE/results/junitreports/test_results_junit.xml
	cp quicklook_summary.txt $WORKSPACE/results || true
}

run_test_id(){
	source `dirname $0`/../common_test.sh
	kill_process
	delete_gf
	ql_init	
	if [[ $1 = "ql_gf_full_profile_all" ]]; then
	    download_test_resources glassfish.zip tests-maven-repo.zip version-info.txt
		unzip_test_resources $WORKSPACE/bundles/glassfish.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"	    	
		cd $WORKSPACE/main/appserver/tests/quicklook/
		mvn -Dglassfish.home=$WORKSPACE/glassfish5/glassfish -Dmaven.repo.local=$WORKSPACE/repository -Ptest_gd_security,report test | tee $TEST_RUN_LOG
		copy_ql_results
	elif [[ $1 = "ql_gf_nucleus_all" || $1 = "nucleus_admin_all" ]]; then
		download_test_resources nucleus-new.zip tests-maven-repo.zip version-info.txt
		unzip_test_resources $WORKSPACE/bundles/nucleus-new.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"
		if [[ $1 = "ql_gf_nucleus_all" ]]; then
			cd $WORKSPACE/main/nucleus/tests/quicklook
		elif [[ $1 = "nucleus_admin_all"  ]]; then
			cd $WORKSPACE/main/nucleus/tests/admin
		fi
		mvn -Dmaven.test.failure.ignore=true -Dnucleus.home=$WORKSPACE/nucleus -Dmaven.repo.local=$WORKSPACE/repository clean test | tee $TEST_RUN_LOG
		cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/
		if [[ $1 = "ql_gf_nucleus_all" ]]; then
			merge_junit_xmls $WORKSPACE/main/nucleus/tests/quicklook/target/surefire-reports/junitreports
		elif [[ $1 = "nucleus_admin_all"  ]]; then
			merge_junit_xmls $WORKSPACE/main/nucleus/tests/admin/target/surefire-reports/junitreports
		fi
		cp $WORKSPACE/nucleus/domains/domain1/logs/server.log* $WORKSPACE/results
		cp $TEST_RUN_LOG $WORKSPACE/results/
	elif [[ $1 = "ql_gf_web_profile_all" || $1 = "ql_gf_embedded_profile_all" ]]; then
		download_test_resources web.zip tests-maven-repo.zip version-info.txt
		unzip_test_resources $WORKSPACE/bundles/web.zip "$WORKSPACE/bundles/tests-maven-repo.zip -d $WORKSPACE/repository"
		cd $WORKSPACE/main/appserver/tests/quicklook/
		if [[ $1 = "ql_gf_web_profile_all" ]]; then
			mvn -Dglassfish.home=$WORKSPACE/glassfish5/glassfish -Dmaven.repo.local=$WORKSPACE/repository -Ptest_wd_security,report test | tee $TEST_RUN_LOG
		elif [[ $1 = "ql_gf_embedded_profile_all" ]]; then
			mvn -Dglassfish.home=$WORKSPACE/glassfish5/glassfish -Dmaven.repo.local=$WORKSPACE/repository -Ptest_em,report test | tee $TEST_RUN_LOG
		fi
		copy_ql_results
	else
		echo "Invalid Test Id"
		exit 1
	fi
    change_junit_report_class_names
}

post_test_run(){
    if [[ $? -ne 0 ]]; then
    	if [[ $TEST_ID = "ql_gf_full_profile_all" || $TEST_ID = "ql_gf_web_profile_all" || $TEST_ID = "ql_gf_embedded_profile_all" ]]; then
	  		copy_ql_results || true
	  	fi
	  	if [[ $TEST_ID = "ql_gf_nucleus_all" || $TEST_ID = "nucleus_admin_all" ]]; then
	  		cp $WORKSPACE/bundles/version-info.txt $WORKSPACE/results/ || true
	  		cp $WORKSPACE/nucleus/domains/domain1/logs/server.log* $WORKSPACE/results || true
		    cp $TEST_RUN_LOG $WORKSPACE/results/ || true
	  	fi
	fi
    upload_test_results
    delete_bundle
    cd -
}

merge_junit_xmls(){
  JUD_DIR=$1
  JUD=$WORKSPACE/results/junitreports/test_results_junit.xml
  rm -f ${JUD} || true
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> ${JUD}
  echo "<testsuites>" >> ${JUD}
  find ${JUD_DIR} -name "*.xml" -type f -exec cat '{}' \; | ${SED} 's/<?xml version=\"1.0\" encoding=\"UTF-8\"?>//g' >> ${JUD}
  echo "</testsuites>" >> ${JUD}
}

list_test_ids(){
	echo ql_gf_full_profile_all ql_gf_nucleus_all ql_gf_web_profile_all ql_gf_embedded_profile_all nucleus_admin_all
}

OPT=$1
TEST_ID=$2

case $OPT in
	list_test_ids )
		list_test_ids;;
	run_test_id )
        trap post_test_run EXIT
		run_test_id $TEST_ID ;;
esac

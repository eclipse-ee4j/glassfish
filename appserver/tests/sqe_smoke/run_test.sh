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

test_run_sqe_smoke(){
        GF_MAVEN=gf-maven.us.oracle.com
        INTERNAL_RELEASE_REPO=http://$GF_MAVEN/nexus/content/repositories/gf-internal-release
	SPS_HOME=$WORKSPACE/appserver-sqe; export SPS_HOME
	# MACHINE CONFIGURATION
	pwd
	uname -a
	java -version
        svn --version
        # CLEANUPS
        kill_clean `ps -ef |grep jre|grep -v grep|cut -f4,5 -d" "`
        kill_clean `jps |grep Main |grep -v grep |cut -f1 -d" "`
        kill_clean `ps -ef | grep $WORKSPACE/glassfish5/glassfish|grep -v grep`
        
        curl --noproxy $GF_MAVEN $INTERNAL_RELEASE_REPO/com/oracle/glassfish/sqe-smoke/1.0/sqe-smoke-1.0.zip > bundles/sqe-smoke.zip
        unzip bundles/sqe-smoke.zip
        
        cd $SPS_HOME
        ant start-domain v3g-smoke-test stop-domain
        archive_artifacts
}

run_test_id(){
	source `dirname $0`/../common_test.sh
	kill_process
	delete_workspace
	download_test_resources glassfish.zip version-info.txt
	unzip_test_resources $WORKSPACE/bundles/glassfish.zip
	test_init
	if [[ $1 = "sqe_smoke_all" ]]; then
		test_run_sqe_smoke
		result=$WORKSPACE/results/test_resultsValid.xml
                resultGtest=$WORKSPACE/results/security-gtest-results-valid.xml
	else
		echo "Invalid Test ID"
		exit 1
	fi
    generate_junit_report_sqe $1 $result $WORKSPACE/results/test_results_junit.xml 
    generate_junit_report_sqe $1 $resultGtest $WORKSPACE/results/test_results_gtest_junit.xml
    merge_junit_xmls $WORKSPACE/results/test_results_junit.xml $WORKSPACE/results/test_results_gtest_junit.xml
    change_junit_report_class_names
}

archive_artifacts(){
	# Archiving
    cp $S1AS_HOME/domains/domain1/logs/server.log* $WORKSPACE/results 
	cp $SPS_HOME/summaryreport-v3smoke.html $WORKSPACE/results/ST-GP-report.html
	cp $SPS_HOME/count.txt $WORKSPACE/results
	cp $SPS_HOME/test_resultsValid.xml $WORKSPACE/results
    find $SPS_HOME/reports -name security-gtest-results-valid.xml -exec cp '{}' $WORKSPACE/results \; > /dev/null || true        
}
merge_junit_xmls(){
  JUD_1=$1
  JUD_2=$2
  JUD=$WORKSPACE/results/junitreports/test_results_junit.xml
  rm -f ${JUD} || true
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" >> ${JUD}
  echo "<testsuites>" >> ${JUD}
  cat $1 >> ${JUD}
  cat $2 >> ${JUD}
  echo "</testsuites>" >> ${JUD}
}


generate_junit_report_sqe(){
	printf "\n%s \n\n" "===== GENERATE JUNIT REPORT ====="
	TD=$2
	JUD=$3
	TESTSUITE_NAME=$1

	cat ${TD} | ${AWK} -v suitename=${TESTSUITE_NAME} '
	  BEGIN {
	    totaltests = 0;
	    totalfailures = 0;
	    totalerrors = 0;
	  }
	  function getPropVal(str){
	    split(str, a, "=");
	    val = a[2];
	    # remove quotes
	    gsub("\"","",val);
	    return val;
	  }
	   function removeXMLTag(str){  
	    # remove xml tag quotes
	    gsub("</.*>","",str);
	    gsub("<.*>","",str);
	    gsub(">","",str);
	    return str;
	  }
	  /status value=/ {
	    result=getPropVal($0);
	    result=removeXMLTag(result);
	  }
	  /<testsuite>/ {
	    getline;
	    getline;
            getline;
	    testunit=removeXMLTag($0);
	    gsub("\"","",testunit);
	  }
	  /<testcase>/ {
	    getline;
	    testname=removeXMLTag($0);
	    gsub("\"","",testname);
	  }
	  /<\/testcase>/{
	    classname=testunit
	    # printing testcase to out
	    out = out sprintf(" <testcase classname=\"%s\" name=\"%s\" time=\"0.0\">\n", classname, testname);
	    if (result == "fail") {
	     out = out "  <failure message=\"NA\" type=\"NA\"/>\n";
	     totalfailures++;
	    } else if (result == "did_not_run") {
	     out = out "  <error message=\"NA\" type=\"NA\"/>\n";
	     totalerrors++;
	    }
	    out = out " </testcase>\n";

	    totaltests++;
	    result="";
	    testname="";
	  }
	  END {	    
	    printf "<testsuite tests=\"%d\" failures=\"%d\" errors=\"%d\" name=\"%s\">\n", totaltests, totalfailures, totalerrors, suitename;
	    printf "%s", out;
	    print "</testsuite>"
	  }' > ${JUD}
}


list_test_ids(){
	echo sqe_smoke_all
}


delete_workspace(){
	printf "\n%s \n\n" "===== DELETE WORKSPACE ====="
    rm -rf $WORKSPACE/glassfish5 > /dev/null || true
    rm -rf $WORKSPACE/appserver-sqe > /dev/null  || true
    rm -rf $WORKSPACE/sqe-smoke.zip > /dev/null || true
    for f in `find $WORKSPACE -type f`; do
    	rm $f > /dev/null
    done
} 

kill_clean(){ 
  if [ ${#1} -ne 0 ] ; then kill -9 $1 ; fi 
}

post_test_run(){
	if [[ $? -ne 0 ]]; then
	  archive_artifacts
	fi
    upload_test_results
    delete_bundle
    cd -
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

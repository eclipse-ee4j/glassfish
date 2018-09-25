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

# OS-specific section
if [ `uname | grep -i "sunos" | wc -l | awk '{print $1}'` -eq 1 ] ; then
  GREP="ggrep"
  AWK="gawk"
  SED="gsed"
  BC="gbc"
  export PATH=/gf-hudson-tools/bin:${PATH}
else
  GREP="grep"
  AWK="awk"
  SED="sed"
  BC="bc"
fi
export GREP AWK SED BC

kill_clean(){
  if [ ${#1} -ne 0 ] ; then kill -9 ${1} || true ; fi
}

kill_process(){
  printf "\n%s \n\n" "===== KILL THEM ALL ====="
  kill_clean `jps | grep ASMain | awk '{print $1}'`
  kill_clean `jps | grep DerbyControl | awk '{print $1}'`
  kill_clean `jps | grep DirectoryServer | awk '{print $1}'`
}

test_init(){
  printf "\n%s \n\n" "===== V2 DEVTESTS INIT ====="
  # workaround for OSGI timestamp issue
  find ${S1AS_HOME} -type f | xargs touch > /dev/null
  echo S1AS_HOME is ${S1AS_HOME}
  echo ANT_HOME is ${ANT_HOME}
  echo APS_HOME is ${APS_HOME}
  java -version
  ant -version
  mkdir -p ${WORKSPACE}/results/junitreports
}

zip_test_results(){
  printf "\n%s \n\n" "===== ZIP THE TESTS RESULTS ====="
  zip -r ${WORKSPACE}/results.zip ${WORKSPACE}/results > /dev/nul
}

unzip_test_resources(){
  printf "\n%s \n\n" "===== UNZIP TEST RESOURCES ====="
  for i in "${@}"; do
    if [[ ${i} == *.zip* ]]; then
      unzip ${i} > /dev/null
    else
      tar -xf ${i}  > /dev/null
    fi
  done
}

copy_test_artifacts(){
  printf "\n%s \n\n" "===== COPY TEST ARTIFACTS ====="
  mkdir -p ${WORKSPACE}/results/junitreports
  tar -cvf ${WORKSPACE}/results/domainArchive.tar.gz ${S1AS_HOME}/domains
  cp ${S1AS_HOME}/domains/domain1/logs/server.log* ${WORKSPACE}/results/ || true
  cp ${TEST_RUN_LOG} ${WORKSPACE}/results/
  cp ${APS_HOME}/test_results*.* ${WORKSPACE}/results/ || true
  cp `pwd`/*/*logs.zip ${WORKSPACE}/results/ || true
  cp `pwd`/*/*/*logs.zip ${WORKSPACE}/results/ || true
  tar -cvf ${WORKSPACE}/${1}-results.tar.gz ${WORKSPACE}/results
}

generate_junit_report(){
  printf "\n%s \n\n" "===== GENERATE JUNIT REPORT ====="
  TD=${APS_HOME}/test_resultsValid.xml
  JUD=${APS_HOME}/test_results_junit.xml
  TESTSUITE_NAME=${1}

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
      print "<?xml version=\"1.0\" ?>"
      printf "<testsuite tests=\"%d\" failures=\"%d\" errors=\"%d\" name=\"%s\">\n", totaltests, totalfailures, totalerrors, suitename;
      printf "%s", out;
      print "</testsuite>"
    }' > ${JUD}
  cp ${JUD} ${WORKSPACE}/results/junitreports
}

change_junit_report_class_names(){
  ${SED} -i 's/\([a-zA-Z-]\w*\)\./\1-/g' ${WORKSPACE}/results/junitreports/*.xml
  ${SED} -i "s/\bclassname=\"/classname=\"${TEST_ID}./g" ${WORKSPACE}/results/junitreports/*.xml
}

check_successful_run(){
  printf "\n%s \n\n" "===== CHECK SUCCESSFUL RUN ====="
  FILE=${APS_HOME}/test_results.html
  if [ -f ${FILE} ]; then
    echo "File ${FILE} exists.Test build successful"
  else
    echo "File ${FILE} does not exist.There is problem in test build."
    exit 1
  fi
}

delete_gf(){
  printf "\n%s \n\n" "===== DELETE GLASSFISH, MAVEN LOCAL_REPO, NUCLEUS ====="
  rm -rf \
    ${WORKSPACE}/glassfish5 \
    ${WORKSPACE}/repository \
    ${WORKSPACE}/nucleus
} 

delete_bundle(){
  printf "\n%s \n\n" "===== DELETE BUNDLES ====="
  rm -rf ${WORKSPACE}/bundles
}

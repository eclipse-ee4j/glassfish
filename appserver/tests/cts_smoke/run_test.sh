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

archive_cts(){
  cp ${TS_HOME}/bin/xml/config_vi.log ${WORKSPACE}/results
  cp ${TS_HOME}/bin/xml/smoke.log ${WORKSPACE}/results
  cp ${S1AS_HOME}/domains/domain1/logs/server.log* ${WORKSPACE}/results
  cp ${TS_HOME}/bin/ts.jte ${WORKSPACE}/results
  echo ${BUILD_ID} > ${WORKSPACE}/results/count.txt
  ${GREP} "Number of Tests Passed" ${WORKSPACE}/results/smoke.log >> ${WORKSPACE}/results/count.txt
  ${GREP} "Number of Tests Failed" ${WORKSPACE}/results/smoke.log >> ${WORKSPACE}/results/count.txt
  ${GREP} "Number of Tests with Errors" ${WORKSPACE}/results/smoke.log >> ${WORKSPACE}/results/count.txt
  cat ${WORKSPACE}/results/count.txt | ${SED} -e 's/\[javatest.batch\] Number/Number/g' > ${WORKSPACE}/results/CTS-GP-count.txt
  rm ${WORKSPACE}/results/count.txt
  tar -cvf ${WORKSPACE}/${TEST_ID}-results.tar.gz -C ${WORKSPACE}/results .
}

test_run_cts_smoke(){
  TS_HOME=${WORKSPACE}/javaee-smoke
  if [[ -z ${CTS_SMOKE_URL} ]]; then
    echo "error: CTS_SMOKE_URL is not set"
    exit 1
  fi
  CTS_SMOKE_BUNDLE=javaee-smoke-8.0_latest.zip
  CTS_EXCLUDE_LIST=ts.jtx

  pwd
  uname -a
  java -version

  rm -rf /tmp/JTreport /tmp/JTwork /disk1/java_re/.javatest || true

  wget ${CTS_SMOKE_URL}/${CTS_SMOKE_BUNDLE}
  unzip -q ${CTS_SMOKE_BUNDLE}

  cd ${TS_HOME}/bin
  #cp $CTS_SMOKE/$CTS_EXCLUDE_LIST .
  cp ts.jte ts.jte.orig

  ${SED} \
    -e "s@javaee.home=@javaee\.home=${S1AS_HOME}@g" \
    -e "s@javaee.home.ri=@javaee\.home\.ri=${S1AS_HOME}@g" \
    -e "s/^orb\.host=/orb\.host=localhost/g" \
    -e "s/^mailHost=/mailHost=localhost/g" \
    -e "s/^mailuser1=/mailuser1=${USER:-root}@localhost/g" \
    -e "s/^mailFrom=.*/mailFrom=${USER:-root}@localhost/g" \
    -e "s/orb.host.ri=/orb.host.ri=localhost/g" \
    -e "s/^work\.dir=\/files/work\.dir=\/tmp/g" \
    -e "s/^report\.dir=\/files/report\.dir=\/tmp/g" \
    -e "s/^tz=.*/tz=US\/Pacific/g" \
    -e "s/modules\/gf-client.jar/lib\/gf-client.jar/g" \
    -e "s/\${pathsep}\${ri\.modules}\/javax\.jms\.jar/\${pathsep}\${ri\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{ri\.modules}\/javax\.jms\.jar/g" \
    -e "s/\${pathsep}\${s1as\.modules}\/javax\.jms\.jar/\${pathsep}\${s1as\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{s1as\.modules}\/javax\.jms\.jar/g" \
    -e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/cdi-api\.jar\${pathsep}\${ri\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" \
    -e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/cdi-api\.jar\${pathsep}\${s1as\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" \
    -e "s/tyrus-container-grizzly\.jar/tyrus-container-grizzly-client\.jar/g" \
    -e "s/javamail\.password=/javamail\.password\=cts1/g" \
    ts.jte > ts.jte.new
  mv ts.jte.new ts.jte

  cd ${TS_HOME}/bin/xml
  export ANT_HOME=${TS_HOME}/tools/ant
  export PATH=${ANT_HOME}/bin:${PATH}

  # SECURITY MANAGER ON
  ${S1AS_HOME}/bin/asadmin start-domain
  ${S1AS_HOME}/bin/asadmin create-jvm-options "-Djava.security.manager"
  ${S1AS_HOME}/bin/asadmin stop-domain
  if [[ -n ${1} ]]; then
    ${TS_HOME}/tools/ant/bin/ant \
      -Dgroups.count=5 \
      -Dgroup.id=${1} \
      -Dgroups.work.dir=/tmp \
      -f ${TS_HOME}/bin/xml/impl/glassfish/smoke-groups.xml \
      smoke.split.groups

    cat /tmp/javaee-smoke-group${1}.properties

    ${TS_HOME}/tools/ant/bin/ant \
      -Dreport.dir=${WORKSPACE}/${BUILD_NUMBER}/JTReport \
      -Dwork.dir=${WORKSPACE}/${BUILD_NUMBER}/JTWork \
      -propertyfile /tmp/javaee-smoke-group${1}.properties \
      -f smoke.xml \
      smoke
  else
    ${TS_HOME}/tools/ant/bin/ant \
      -Dreport.dir=${WORKSPACE}/${BUILD_NUMBER}/JTReport \
      -Dwork.dir=${WORKSPACE}/${BUILD_NUMBER}/JTWork \
      -f smoke.xml \
      smoke
  fi

  kill_process
  archive_cts
}

archive_servlet_tck(){
  cp ${S1AS_HOME}/domains/domain1/logs/server.log* ${WORKSPACE}/results
  cp ${WORKSPACE}/tests.log ${WORKSPACE}/results
  cp -r ${TS_HOME}/report/ ${WORKSPACE}/results
  tar -cvf ${WORKSPACE}/${TEST_ID}-results.tar.gz ${WORKSPACE}/results
}

test_run_servlet_tck(){
  export TS_HOME=${WORKSPACE}/servlettck
  if [[ -z ${CTS_SMOKE_URL} ]]; then
    echo "error: CTS_SMOKE_URL is not set"
    exit 1
  fi

  pwd
  uname -a
  java -version

  rm -rf /tmp/JTreport /tmp/JTwork /disk1/java_re/.javatest || true

  wget ${CTS_SMOKE_URL}/servlettck-4.0_Latest.zip -O servlettck.zip
  unzip -q servlettck.zip

  if [[ -n ${1} ]]; then
    TESTDIR=${WORKSPACE}/servlettck/src/com/sun/ts/tests
    for i in `ls ${TESTDIR}`
    do
      if [[ (-d ${TESTDIR}/$i)  && ( ${i} != "jsp" &&  ${i} != "common" && ${i} != "signaturetest") ]]; then
        if [[ -z $(grep ${i} `dirname ${0}`/test_dir.properties) ]]; then
          echo "A new folder ${i} is added in the test source which has no entry in the properties file"
          exit 1
        fi
      fi
    done
  fi

  cd ${TS_HOME}/bin
  cp ts.jte ts.jte.orig

  cat ts.jte.orig | ${SED} \
  -e "s@webServerHost=@webServerHost=localhost@g" \
  -e "s@webServerPort=@webServerPort=8080@g" \
  -e "s@securedWebServicePort=@securedWebServicePort=8181@g" \
  -e "s@web.home=@web\.home=${S1AS_HOME}@g" \
  -e "s@javaee\.home\.ri=@javaee\.home\.ri=${S1AS_HOME}@g" \
  -e "s/^orb\.host=/orb\.host=localhost/g" \
  -e "s/^mailHost=/mailHost=localhost/g" \
  -e "s/^mailuser1=/mailuser1=${USER:-root}@localhost/g" \
  -e "s/^mailFrom=.*/mailFrom=${USER:-root}@localhost/g" \
  -e "s/orb.host.ri=/orb.host.ri=localhost/g" \
  -e "s/^work\.dir=\/files/work\.dir=\/tmp/g" -e "s/^report\.dir=\/files/report\.dir=\/tmp/g" \
  -e "s/^tz=.*/tz=US\/Pacific/g" -e "s/modules\/gf-client.jar/lib\/gf-client.jar/g" \
  -e "s/\${pathsep}\${ri\.modules}\/javax\.jms\.jar/\${pathsep}\${ri\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{ri\.modules}\/javax\.jms\.jar/g" \
  -e "s/\${pathsep}\${s1as\.modules}\/javax\.jms\.jar/\${pathsep}\${s1as\.modules}\/javax\.jms-api\.jar\${pathsep}\$\{s1as\.modules}\/javax\.jms\.jar/g" \
  -e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/cdi-api\.jar\${pathsep}\${ri\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" \
  -e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/cdi-api\.jar\${pathsep}\${s1as\.modules}\/cdi-api-fragment\.jar\${pathsep}/g" \
  -e "s/implementation\.classes\.ri=/implementation\.classes\.ri=\${ri\.modules}\/tyrus-container-grizzly-client\.jar\${pathsep}/g" \
  -e "s/implementation\.classes=/implementation\.classes=\${s1as\.modules}\/tyrus-container-grizzly-client\.jar\${pathsep}/g" \
  -e "s/tyrus-container-grizzly\.jar/tyrus-container-grizzly-client\.jar/g" \
  -e "s/impl\.vi=/impl\.vi\=glassfish/g" \
  > ts.jte

  echo "# Disabling signature tests for CI build pipeline" >> ts.jtx
  echo "com/sun/ts/tests/signaturetest/servlet/ServletSigTest.java#signatureTest" >> ts.jtx

  cd ${S1AS_HOME}
  bin/asadmin start-domain

  cd ${TS_HOME}/bin
  ant config.security
  ant deploy.all

  if [ -n ${1} ]; then
    cd ${TS_HOME}/src/com/sun/ts/tests/${1}
  else
    cd ${TS_HOME}/src/com/sun/ts/tests/
  fi
  export JAVA_OPTIONS="-Xbootclasspath/p:${TS_HOME}/lib/flow.jar"

  (ant runclient -Dreport.dir=${WORKSPACE}/servlettck/report | tee ${WORKSPACE}/tests.log) || true

  cd ${S1AS_HOME}
  bin/asadmin stop-domain

  kill_process
  archive_servlet_tck
}

run_test_id(){
  source `dirname $0`/../common_test.sh
  kill_process
  delete_workspace
  unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
  test_init
  if [[ ${1} = "cts_smoke_all" ]]; then
    test_run_cts_smoke
    result=${WORKSPACE}/results/smoke.log
  elif [[ ${1} = "cts_smoke_group-"* ]]; then
    GROUP_ID=(`echo ${1} | sed 's/cts_smoke_group-//'`)
    test_run_cts_smoke ${GROUP_ID}
    result=${WORKSPACE}/results/smoke.log
  elif [[ ${1} = "servlet_tck_"* ]]; then
    TEST_DIR_PROPERTIES=`dirname ${0}`/test_dir.properties
    TEST_DIR_PROP_KEY=(`echo ${1} | sed 's/servlet_tck_//'`)
    TEST_DIR=(`cat ${TEST_DIR_PROPERTIES} | grep ${TEST_DIR_PROP_KEY} | cut -d'=' -f2`)
    test_run_servlet_tck ${TEST_DIR}
    result=${WORKSPACE}/results/tests.log
  else
    echo "Invalid Test ID"
    exit 1
  fi
  cts_to_junit ${result} ${WORKSPACE}/results/junitreports/test_results_junit.xml ${1}
}

post_test_run(){
  delete_bundle
  cd -
}

list_test_ids(){
  echo cts_smoke_all servlet_tck_all servlet_tck_servlet-api-servlet servlet_tck_servlet-api-servlet-http servlet_tck_servlet-compat servlet_tck_servlet-pluggability servlet_tck_servlet-spec  cts_smoke_group-1 cts_smoke_group-2 cts_smoke_group-3 cts_smoke_group-4 cts_smoke_group-5
}

cts_to_junit(){
  junitCategory=${3}
  cd ${WORKSPACE}/results
  rm -rf ${2}
  cat ${1} | ${GREP} -a "\[javatest.batch\] Finished Test" >  results.txt
  tail $((`${GREP} -n "Completed running" results.txt | ${AWK} '{print $1}' | cut -d ':' -f1`-`cat results.txt | wc -l`)) results.txt > summary.txt

  echo "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" >> ${2}
  echo "<testsuites>" >> ${2}
  echo "  <testsuite>" >> ${2}
  for i in `${GREP} "\.\.\.\.\.\.\.\." summary.txt | ${AWK} '{print $4}'`
  do
    line=`echo ${i} | ${SED} s@"\.\.\.\.\.\.\.\."@" "@g`
    status=`echo ${line} | ${AWK} '{print $1}'`
    id=`echo ${line} | ${AWK} '{print $2}'`
    classname=`echo ${id} | cut -d '#' -f1 | ${SED} s@"\/"@"_"@g | ${SED} s@".java"@@g`
    name=`echo ${id} | cut -d '#' -f2`

    echo "    <testcase classname=\"${junitCategory}.${classname}\" name=\"${name}\">" >> ${2}
    if [ "${status}" = "FAILED" ]
    then
      echo "      <failure type=\"CtsFailure\"> n/a </failure>" >> ${2}
    fi
    echo "    </testcase>" >> ${2}
  done
  echo "  </testsuite>" >> ${2}
  echo "</testsuites>" >> ${2}
}

delete_workspace(){
  printf "\n%s \n\n" "===== DELETE WORKSPACE ====="
    rm -rf ${WORKSPACE}/glassfish5 > /dev/null || true
    rm -rf ${WORKSPACE}/servlettck > /dev/null  || true
    rm ${WORKSPACE}/servlettck.zip > /dev/null || true
    rm -rf ${WORKSPACE}/javaee-smoke > /dev/null || true
    rm ${WORKSPACE}/javaee-smoke-7.0_latest.zip > /dev/null || true
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

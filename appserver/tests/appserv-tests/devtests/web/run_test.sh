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

kill_processes() {
  (ps -aef | grep java | grep ASMain | grep -v grep | awk '{print $2}' | xargs kill -9 > /dev/null 2>&1) || true
  (jps | grep Main | grep -v grep | awk '{print $1}' | xargs kill -9 > /dev/null 2>&1) || true
  (ps -aef | grep derby | grep -v grep | awk '{print $2}' | xargs kill -9 > /dev/null 2>&1) || true
}

is_target(){
  case "${1}" in
    "jsp" | \
    "taglib" | \
    "el" | \
    "servlet" | \
    "web-container" | \
    "security" | \
    "http-connector" | \
    "comet" | \
    "misc" | \
    "weblogicDD" | \
    "clustering" | \
    "ha" | \
    "embedded-all" | \
    "group-1" | \
    "all") echo 1;;
    *) echo 0;;
  esac
}

get_test_target(){
  case ${1} in
    web_all )
      TARGET=all
      export TARGET;;
    group-1 )
      TARGET="init taglib el security http-connector comet misc clustering ha finish-report"
      export TARGET;;
    * )
      TARGET="init $1 finish-report"
      export TARGET;;
  esac
}


test_run(){
  export WEBTIER_ADMIN_PORT=45707
  export WEBTIER_JMS_PORT=45708
  export WEBTIER_JMX_PORT=45709
  export WEBTIER_ORB_PORT=45710
  export WEBTIER_HTTP_PORT=45711
  export WEBTIER_HTTPS_PORT=45712
  export WEBTIER_ALTERNATE_PORT=45713
  export WEBTIER_ORB_SSL_PORT=45714
  export WEBTIER_ORB_SSL_MUTUALAUTH_PORT=45715
  export WEBTIER_INSTANCE_PORT=45716
  export WEBTIER_INSTANCE_PORT_2=45717
  export WEBTIER_INSTANCE_PORT_3=45718
  export WEBTIER_INSTANCE_HTTPS_PORT=45719

  export AS_LOGFILE=${S1AS_HOME}/cli.log
  #export AS_DEBUG=true

  #Copy over the modified run.xml for dumping thread stack
  #cp ../../run.xml $PWD/appserv-tests/config

  rm -rf ${S1AS_HOME}/domains/domain1
  cd ${APS_HOME}

  echo "AS_ADMIN_PASSWORD=" > temppwd
  ${S1AS_HOME}/bin/asadmin \
    --user admin \
    --passwordfile ${APS_HOME}/config/adminpassword.txt \
    create-domain \
      --adminport ${WEBTIER_ADMIN_PORT} \
      --domainproperties jms.port=${WEBTIER_JMS_PORT}:domain.jmxPort=${WEBTIER_JMX_PORT}:orb.listener.port=${WEBTIER_ORB_PORT}:http.ssl.port=${WEBTIER_HTTPS_PORT}:orb.ssl.port=${WEBTIER_ORB_SSL_PORT}:orb.mutualauth.port=${WEBTIER_ORB_SSL_MUTUALAUTH_PORT} \
      --instanceport ${WEBTIER_HTTP_PORT} \
      domain1

  HOST="localhost"

  # Create
  echo "admin.domain=domain1
  admin.domain.dir=\${env.S1AS_HOME}/domains
  admin.port=${WEBTIER_ADMIN_PORT}
  admin.user=admin
  admin.host=${HOST}
  http.port=${WEBTIER_HTTP_PORT}
  https.port=${WEBTIER_HTTPS_PORT}
  http.host=${HOST}
  http.address=127.0.0.1
  http.alternate.port=${WEBTIER_ALTERNATE_PORT}
  orb.port=${WEBTIER_ORB_PORT}
  admin.password=
  ssl.password=changeit
  master.password=changeit
  admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
  appserver.instance.name=server
  config.dottedname.prefix=server
  resources.dottedname.prefix=domain.resources
  results.mailhost=${HOST}
  results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
  results.mailee=yourname@sun.com
  autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
  precompilejsp=true
  jvm.maxpermsize=192m
  ENABLE_REPLICATION=false
  appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}
  cluster.name=clusterA
  instance.name=inst1
  instance.name.2=inst2
  instance.name.3=inst3
  instance.http.port=${WEBTIER_INSTANCE_PORT}
  instance.https.port=${WEBTIER_INSTANCE_HTTPS_PORT}
  instance.http.port.2=${WEBTIER_INSTANCE_PORT_2}
  instance.http.port.3=${WEBTIER_INSTANCE_PORT_3}
  nodeagent.name=localhost-domain1
  " > config.properties

  kill_processes

  cd ${APS_HOME}/devtests/web
  ant ${TARGET} | tee ${TEST_RUN_LOG}

  kill_processes
  (cat web.output | grep FAIL | grep -v "Total FAIL") || true
}

run_test_id(){
  cat /etc/hosts
  unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
  cd `dirname ${0}`
  test_init
  TARGET_FROM_INPUT=(`echo $1 | sed 's/web_//'`)
  get_test_target ${TARGET_FROM_INPUT}
  test_run
  check_successful_run
  generate_junit_report ${TARGET_FROM_INPUT}
  change_junit_report_class_names
}

list_test_ids(){
  echo web_all web_jsp web_servlet web_web-container web_group-1
}

OPT=$1
TEST_ID=$2
source `dirname $0`/../../../common_test.sh

case $OPT in
  list_test_ids )
    list_test_ids;;
  run_test_id )
    trap "copy_test_artifacts ${TEST_ID}" EXIT
    run_test_id $TEST_ID ;;
esac

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

copyright_run(){
  rm -f ${WORKSPACE}/copyright-files.txt || true
  rm -f ${WORKSPACE}/copyright-files-temp*.txt || true
  rm -rf ${WORKSPACE}/tmp-users || true
  cd ${WORKSPACE}

  mvn -e -q \
    -Dcopyright.normalize=true \
    org.glassfish.copyright:glassfish-copyright-maven-plugin:copyright \
    > ${WORKSPACE}/copyright-files-temp-open.txt
  cat ${WORKSPACE}/copyright-files-temp-open.txt
  cat ${WORKSPACE}/copyright-files-temp-open.txt | sed s@${PWD}/@@g > copyright-files.txt
}

generate_copyright_results(){
  rm -rf ${WORKSPACE}/results || true
  mkdir -p ${WORKSPACE}/results/copyright_results
  if [ ! -f copyright-files.txt ] ; then
    echo "FAILED" > ${WORKSPACE}/results/copyright_results/copyrightcheck.log
  elif [ `wc -l copyright-files.txt | awk '{print $1}'` -gt 0 ]; then
    echo "UNSTABLE" > ${WORKSPACE}/results/copyright_results/copyrightcheck.log
  else
    echo "SUCCESS" > ${WORKSPACE}/results/copyright_results/copyrightcheck.log
  fi
  cp copyright-files.txt ${WORKSPACE}/results/copyright_results/copyright-files.txt
  tar -cvf ${WORKSPACE}/${TEST_ID}-results.tar.gz ${WORKSPACE}/results
}

run_test_id(){
  source `dirname ${0}`/../common_test.sh
  kill_process
  copyright_run
  generate_copyright_results
}

post_test_run(){
  if [[ ${?} -ne 0 ]]; then
    generate_copyright_results
  fi
  delete_bundle
}

list_test_ids(){
  echo copyright
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

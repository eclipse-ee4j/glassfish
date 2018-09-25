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

findbugs_run(){
  CLASSPATH=${WORKSPACE}/findbugstotext; export CLASSPATH
  cd ${WORKSPACE}
  mvn -e -Pfindbugs clean install
  mvn -e -Pfindbugs findbugs:findbugs

}

findbugs_low_priority_all_run(){
  cd ${WORKSPACE}
  mvn -e -Pfindbugs clean install
  mvn -e -B -Pfindbugs -Dfindbugs.threshold=Low findbugs:findbugs
}

generate_findbugs_result(){
  rm -rf ${WORKSPACE}/results
  mkdir -p ${WORKSPACE}/results/findbugs_results

  # check findbbugs
  set +e
  cd /net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/findbugs-tool-latest; ./findbugscheck ${WORKSPACE}
  if [ ${?} -ne 0 ]
  then
     echo "FAILED" > ${WORKSPACE}/results/findbugs_results/findbugscheck.log
  else
     echo "SUCCESS" > ${WORKSPACE}/results/findbugs_results/findbugscheck.log
  fi
  set -e
  # archive the findbugs results
  for i in `find ${WORKSPACE} -name findbugsXml.xml`
  do
     cp ${i} ${WORKSPACE}/results/findbugs_results/`echo $i | sed s@"${WORKSPACE}"@@g | sed s@"/"@"_"@g`
  done
  tar -cvf ${WORKSPACE}/${TEST_ID}-results.tar.gz ${WORKSPACE}/results
}

generate_findbugs_low_priority_all_result(){
  rm -rf ${WORKSPACE}/results
  mkdir -p ${WORKSPACE}/results/findbugs_low_priority_all_results

  # check findbbugs
  set +e
  cd /net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/findbugs-tool-latest; ./fbcheck ${WORKSPACE}
  if [ $? -ne 0 ]
  then
     echo "FAILED" > ${WORKSPACE}/results/findbugs_low_priority_all_results/findbugscheck.log
  else
     echo "SUCCESS" > ${WORKSPACE}/results/findbugs_low_priority_all_results/findbugscheck.log
  fi
  set -e
  cp /net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/findbugs-tool-latest/fbstatsdetails.log ${WORKSPACE}/results/findbugs_low_priority_all_results/fbstatsdetails.log | true
  # archive the findbugs results
  for i in `find ${WORKSPACE} -name findbugsXml.xml`
  do
     cp ${i} ${WORKSPACE}/results/findbugs_low_priority_all_results/`echo $i | sed s@"${WORKSPACE}"@@g | sed s@"/"@"_"@g`
  done
  tar -cvf ${WORKSPACE}/${TEST_ID}-results.tar.gz ${WORKSPACE}/results
}

run_test_id(){
  source `dirname ${0}`/../common_test.sh
  kill_process
  case ${TEST_ID} in
    findbugs_all)
      findbugs_run
      generate_findbugs_result;;
    findbugs_low_priority_all)
      findbugs_low_priority_all_run
      generate_findbugs_low_priority_all_result;;
  esac
}

post_test_run(){
  if [[ ${?} -ne 0 ]]; then
    if [[ ${TEST_ID} = "findbugs_all" ]]; then
      generate_findbugs_result || true
    fi
    if [[ ${TEST_ID} = "findbugs_low_priority_all" ]]; then
      generate_findbugs_low_priority_all_result || true
    fi
  fi
  delete_bundle
}

list_test_ids(){
  echo findbugs_all
  echo findbugs_low_priority_all
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

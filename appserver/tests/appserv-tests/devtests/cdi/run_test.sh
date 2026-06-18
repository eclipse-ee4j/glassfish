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

test_run(){
    printf "\n%s \n\n" "===== TEST RUN - MAIN ====="

    # Fix for false positive
    echo "<property name=\"libraries\" value=\"' '\"/>" >> smoke-tests/simple-wab-with-cdi/build.properties
    find . -name "RepRunConf.txt" | xargs rm -f
    rm -rf *.output alltests.res ${APS_HOME}/test_results*

    # Run the tests
    set +e
    ant clean -e -q > /dev/null

    printf "\n%s \n\n" "===== TEST RUN - STARTING GLASSFISH AND DB ====="
    ${S1AS_HOME}/bin/asadmin start-domain
    if [ "${JACOCO_ENABLED}" = "true" ]; then
        ${S1AS_HOME}/bin/asadmin create-jvm-options -javaagent\\:${BUNDLES_DIR}/org.jacoco.agent.jar=destfile=${WORKSPACE}/cdi.exec,append=true,includes=org/glassfish/**\\:com/sun/enterprise/**
        ${S1AS_HOME}/bin/asadmin restart-domain
    fi
    ${S1AS_HOME}/bin/asadmin start-database

    printf "\n%s \n\n" "===== TEST RUN - RUNNING TESTS for ${TARGET} ====="
    ant ${TARGET} | tee ${TEST_RUN_LOG}

    printf "\n%s \n\n" "===== TEST RUN - STOPPING GLASSFISH AND DB ====="
    ${S1AS_HOME}/bin/asadmin stop-domain
    ${S1AS_HOME}/bin/asadmin stop-database
    set -e
}

get_test_target(){
    printf "\n%s \n\n" "===== GETTING TEST TARGET ====="
    case ${1} in
        cdi_all )
            TARGET=all
            export TARGET;;
    esac
}


run_test_id(){
    unzip_test_resources ${BUNDLES_DIR}/glassfish.zip
    cd `dirname ${0}`
    test_init
    get_test_target ${1}
    test_run
    check_successful_run
    generate_junit_report ${1}
    change_junit_report_class_names
}

list_test_ids(){
    echo cdi_all
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

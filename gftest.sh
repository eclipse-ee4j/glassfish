#
# Copyright (c) 2020 Eclipse Foundation and/or its affiliates. All rights reserved.
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

#!/bin/bash -e

export WORKSPACE=`pwd`
export S1AS_HOME="${WORKSPACE}/glassfish8/glassfish"
export APS_HOME="${WORKSPACE}/appserver/tests/appserv-tests"
export TEST_RUN_LOG="${WORKSPACE}/tests-run.log"
export LANG="en_US.UTF-8"
export MVN_REPOSITORY="${MVN_REPOSITORY:=${HOME}/.m2/repository}"
export M2_REPO="${M2_REPO:=$MVN_REPOSITORY}"
export GF_VERSION="$(mvn help:evaluate -f "${WORKSPACE}/pom.xml" -Dexpression=project.version -q -DforceStdout)"

echo WORKSPACE =  $WORKSPACE
echo S1AS_HOME =  $S1AS_HOME
echo APS_HOME  =  $APS_HOME
echo TEST_RUN_LOG =  $TEST_RUN_LOG
echo M2_REPO=MVN_REPOSITORY = ${M2_REPO}
echo GF_VERSION = ${GF_VERSION}

if [ "$#" -eq 0 ]; then
    declare -a arr=(
       "web_jsp"
       "deployment_all"
       "ejb_group_1"
       "ejb_group_2"
       "ejb_group_3"
       "ejb_group_embedded"
       "ejb_web_all"
       "cdi_all"
       "ql_gf_full_profile_all"
       "ql_gf_nucleus_all"
       "ql_gf_web_profile_all"
       "nucleus_admin_all"
       "jdbc_all"
       "batch_all"
       "persistence_all"
       "connector_group_1"
       "connector_group_2"
       "connector_group_3"
       "connector_group_4")
    printf '\nNo tests specified as command arguments, using default set\n'
else
    declare -a arr=("$@")
fi

echo "Removing old glassfish directory: ${S1AS_HOME}";
rm -rf "${S1AS_HOME}";

printf '\n Running tests for: \n\n'
printf '* %s\n' "${arr[@]}"

top_dir="$(pwd)"

printf '\n Running tests from dir %s' "$top_dir"

for i in "${arr[@]}"
do

   printf "\n\n\n\n\n **************************************  \n Start Running $i \n **************************************  \n\n\n\n\n\n"

   ./appserver/tests/gftest.sh run_test ${i}

   exit_code=$?

   cd $top_dir

   printf "\n\n\n\n\n **************************************  \n Finished Running $i \n **************************************  \n\n\n\n\n\n"

   printf 'Back at %s\n' "$(pwd)"

   if [ "$exit_code" -ne "0" ]; then
     printf "\n\n\n\n EXITING BECAUSE OF FAILURES. SEE ABOVE! \n\n\n\n"
     exit $exit_code
   fi
done



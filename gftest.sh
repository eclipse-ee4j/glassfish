#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#!/bin/bash -e

export WORKSPACE=`pwd`
export S1AS_HOME="${WORKSPACE}/glassfish6/glassfish"
export APS_HOME="${WORKSPACE}/appserver/tests/appserv-tests"
export TEST_RUN_LOG="${WORKSPACE}/tests-run.log"

echo WORKSPACE =  $WORKSPACE
echo S1AS_HOME =  $S1AS_HOME
echo APS_HOME  =  $APS_HOME
echo TEST_RUN_LOG =  $TEST_RUN_LOG

if [ "$#" -eq 0 ]; then
    declare -a arr=(
       "deployment_all" 
       "ejb_group_1" 
       "ejb_group_2" 
       "ejb_group_3" 
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



#!/bin/bash -e
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

if [ -z "${WORKSPACE}" ] ; then
  export WORKSPACE=`dirname ${0}`
fi

merge_junits(){
  TEST_ID="build-unit-tests"
  rm -rf ${WORKSPACE}/test-results
  mkdir -p ${WORKSPACE}/test-results/${TEST_ID}/results/junitreports
  JUD="${WORKSPACE}/test-results/${TEST_ID}/results/junitreports/test_results_junit.xml"
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > ${JUD}
  echo "<testsuites>" >> ${JUD}
  for i in `find . -type d -name "surefire-reports"`
  do    
    ls -d -1 ${i}/*.xml | xargs cat | sed 's/<?xml version=\"1.0\" encoding=\"UTF-8\" *?>//g' >> ${JUD}
  done
  echo "</testsuites>" >> ${JUD}
  sed -i 's/\([a-zA-Z-]\w*\)\./\1-/g' ${JUD}
  sed -i "s/\bclassname=\"/classname=\"${TEST_ID}./g" ${JUD}
}

archive_bundles(){
  printf "\n%s \n\n" "===== ARCHIVE BUNDLES ====="
  mkdir ${WORKSPACE}/bundles
  cp appserver/distributions/glassfish/target/*.zip ${WORKSPACE}/bundles
  cp appserver/distributions/web/target/*.zip ${WORKSPACE}/bundles
  cp nucleus/distributions/nucleus/target/*.zip ${WORKSPACE}/bundles
}

dev_build(){
  printf "\n%s \n\n" "===== DO THE BUILD! ====="
  mvn -U clean install -Dmaven.test.failure.ignore=true
}

build_re_dev(){
  dev_build
  archive_bundles
  merge_junits
}

if [ ! -z "${JENKINS_HOME}" ] ; then

  # inject internal environment
  readonly GF_INTERNAL_ENV_SH=$(mktemp -t XXXgf-internal-env)
  if [ ! -z "${GF_INTERNAL_ENV}" ] ; then
    echo "${GF_INTERNAL_ENV}" | base64 -d > ${GF_INTERNAL_ENV_SH}
    . ${GF_INTERNAL_ENV_SH}
    export MAVEN_OPTS="${ANT_OPTS} -Dmaven.repo.local=/root/.m2/repository"
  fi

  apt-get update
  apt-get install -y tar
fi

"$@"
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

merge_junits(){
  local test_id="build-unit-tests"
  rm -rf ${WORKSPACE}/test-results && \
  mkdir -p ${WORKSPACE}/test-results/${test_id}/results/junitreports
  local jud="${WORKSPACE}/test-results/${test_id}/results/junitreports/test_results_junit.xml"
  echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" > ${jud}
  echo "<testsuites>" >> ${jud}
  for i in `find . -type d -name "surefire-reports"`
  do
    ls -d -1 ${i}/*.xml | xargs cat | sed 's/<?xml version=\"1.0\" encoding=\"UTF-8\" *?>//g' >> ${jud}
  done
  echo "</testsuites>" >> ${jud}
  sed -i 's/\([a-zA-Z-]\w*\)\./\1-/g' ${jud}
  sed -i "s/\bclassname=\"/classname=\"${test_id}./g" ${jud}
}

bundles(){
  archive_bundles
}

archive_bundles(){
  mkdir -p ${WORKSPACE}/bundles
  cp appserver/distributions/glassfish/target/*.zip ${WORKSPACE}/bundles
  cp appserver/distributions/web/target/*.zip ${WORKSPACE}/bundles
  cp nucleus/distributions/nucleus/target/*.zip ${WORKSPACE}/bundles
}

dev_build(){
  mvn -U clean install -Pstaging,fastest -T2C ${MVN_EXTRA}
}

build_re_dev(){
  if [ -f "${WORKSPACE}/snapshots/pom.xml" ]; then
    mvn clean install -T2C -f "${WORKSPACE}/snapshots/pom.xml" ${MVN_EXTRA}
  fi
  dev_build
  archive_bundles
  merge_junits
}

if [ -z "${WORKSPACE}" ] ; then
  export WORKSPACE=`dirname ${0}`
fi

"$@"

if [ ! -z "${JENKINS_HOME}" ] ; then
  tar -c --exclude='*.zip' --exclude='*/main/tests/tck/*' --exclude='*/main/distributions/*' --exclude='*/main/extras/*' --exclude='*/main/admingui/*' --newer-mtime '1 day ago' -C ${HOME}/.m2/repository org/glassfish/main | gzip --fast > ${WORKSPACE}/bundles/maven-repo.tar.gz
fi

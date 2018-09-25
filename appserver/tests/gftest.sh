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

run_test(){
	local testid=${1}
	local found=false
	for runtest in `find . -name run_test\.sh`; do
		for id in `${runtest} list_test_ids`; do
			if [[ "${id}" = "${testid}" ]]; then
				found=true
				break
			fi
		done
		if [[ "${found}" = true ]]; then
			${runtest} run_test_id ${testid}
			break
		fi
	done
	if [[ "${found}" = false ]]; then
		echo Invalid Test Id.
		exit 1
	fi
}

if [ ! -z "${JENKINS_HOME}" ] ; then

  # inject internal environment
  readonly GF_INTERNAL_ENV_SH=$(mktemp -t XXXgf-internal-env)
  if [ ! -z "${GF_INTERNAL_ENV}" ] ; then
    echo "${GF_INTERNAL_ENV}" | base64 -d > ${GF_INTERNAL_ENV_SH}
    . ${GF_INTERNAL_ENV_SH}
    export ANT_HOME=/usr/share/ant
    export MAVEN_OPTS="${ANT_OPTS} -Dmaven.repo.local=/root/.m2/repository"
  fi

  apt-get update
  apt-get install -y apt-utils ant unzip tar wget zip sendmail

  # only needed by some of the tests (cts-smoke*)
  echo "starting sendmail..."
  /usr/sbin/sendmail -bd -q1h
fi

"$@"

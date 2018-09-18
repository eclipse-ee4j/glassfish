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

unzip_test_sources(){
	unzip -d main/  $WORKSPACE/bundles/tests-workspace.zip > /dev/null
}
delete_test_sources(){
	rm -rf $WORKSPACE/main
	rm -rf $WORKSPACE/bundles
} 
download_test_zip(){
	mkdir bundles
	scp -o "StrictHostKeyChecking no" ${PARENT_NODE}:${PARENT_WS_PATH}/bundles/tests-workspace.zip bundles
}
	
###########################
#Start Script
###########################

run_test(){
	TEST_ID=$1
	delete_test_sources
	download_test_zip
	unzip_test_sources
	found=false
	for runtest in `find . -name run_test\.sh`; do
		for testid in `$runtest list_test_ids`; do
			if [[ "$testid" = "$TEST_ID" ]]; then
				found=true
				break
			fi
		done
		if [[ "$found" = true ]]; then
			$runtest run_test_id $TEST_ID
			break
		fi
	done
	if [[ "$found" = false ]]; then
		echo Invalid Test Id.
		exit 1
	fi

}

generate_platform(){
	uname -nsp > /tmp/platform
	scp -o "StrictHostKeyChecking no" -r /tmp/platform ${PARENT_NODE}:${PARENT_WS_PATH}/test-results/$TEST_ID
}

list_test_ids(){
	for runtest in `find . -name run_test\.sh`; do
		echo `$runtest list_test_ids`
	done
}

list_group_test_ids(){
	test_groups=`find . -type d -name test_groups` 
	test_id_arr+=(`cat  $test_groups/$1 |tr "\n" " "`)
	echo ${test_id_arr[*]}
}

OPT=$1
TEST_ID=$2

case $OPT in
	list_test_ids )
		if [[ -z $2 ]]; then
			list_test_ids
		else
			list_group_test_ids $2
		fi;;
		
	run_test )
		trap generate_platform EXIT
		run_test $TEST_ID ;;
esac
 

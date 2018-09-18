#!/bin/bash -e
#
# Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

USAGE="Usage:\n\n 1. rq.sh -l ---> List all available test identifiers without running them\n\
	   2. rq.sh -b <branch> -a ---> For running all tests in remote branch\n\
	   3. rq.sh -b <branch> -g <test_group_name> ---> For running a test group\n\
	   4. rq.sh -b <branch> -t \"<test_id1> <test_id2> <test_id3>\" ---> For running a space separated list of tests\n\
	   5. rq.sh -u <glassfish binary url>  -a|-u|-t ---> For running all tests with GlassFish binary provided in the http url.-u option works with -a, -g and -t options as well\n\
	   6. rq.sh -b <branch> -a|-u|-t -e <email-id> ---> For getting the test results in the email id.This works with -a -t and -g options"
	   

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

if [[ -z $GLASSFISH_REMOTE_QUEUE_URL ]]; then
	echo "Please enter hudson url"
	exit 1
fi

OPTIND=1    

output_file=""
verbose=0

if [ $# -eq 0 ];
then
    echo -e $USAGE
    exit 0
fi    
while getopts ":b:t:g:e:u:al" opt; do
    case "$opt" in
    b)	branch=$OPTARG;;
    t)  test_ids=($OPTARG);;
    a)  test_ids=(`list_test_ids`);;
	g)  test_ids=(`list_group_test_ids $OPTARG`);;
	l)	test_ids=(`list_test_ids`)
		echo ${test_ids[@]} | tr " " "\n"
		exit 0;;
	e)  GLASSFISH_REMOTE_QUEUE_EMAIL=$OPTARG;;
	u)  url=$OPTARG;;
    *)	echo -e "Invalid option"
		echo -e $USAGE
		exit 1 ;;         
    esac
done

shift $((OPTIND-1))

if [[ -z $branch && -z $url ]]; then
	echo "Please provide a remote branch or a glassfish binary url to trigger glassfish remote queue"
	echo -e $USAGE
	exit 1
fi
if [[ -z $test_ids ]]; then
	echo "test id is missing"
	echo -e $USAGE
	exit 1
fi
if [[ -z $GLASSFISH_REMOTE_QUEUE_EMAIL && ! -z $branch ]]; then
	echo "EMAIL_ID is missing"
	echo -e $USAGE
	exit 1
fi
fork_origin=`git config --get remote.origin.url`
test_ids_encoded=`echo ${test_ids[@]} | tr ' ' '+'`
params="BRANCH=${branch}&TEST_IDS=${test_ids_encoded}&FORK_ORIGIN=${fork_origin}&URL=${url}&EMAIL_ID=${GLASSFISH_REMOTE_QUEUE_EMAIL}"
status=`curl -s -o /dev/null -w "%{http_code}" -X POST "${GLASSFISH_REMOTE_QUEUE_URL}/buildWithParameters?${params}&delay=0sec"`
echo $status
echo "----------------------------------------------------------------------------"
if [[ ${status} -eq 201 ]]; then
	printf "RQ triggered successfully. You will get the job link via email shortly\n"
	echo "----------------------------------------------------------------------------"
else
	printf "Issue in RQ client.Please check your settings\n"
    echo "----------------------------------------------------------------------------"
fi

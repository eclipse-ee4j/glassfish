#!/bin/bash -e
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

################################## Constants ##################################

declare -r USAGE=$(cat <<'EOF'
Prerequites: User must specify CTS_REMOTE_QUEUE_URL before running this script.
             This URL must point to the upstream hudson job of CTS Remote Queue.

Usage:
 1. ctsrq.sh -l  --->  List all the available CTS test identifiers
                       (both top-level and inner-level) without running them
 2. ctsrq.sh -s  --->  List all the available top-level CTS test identifiers
                       without running them
 3. ctsrq.sh -i  --->  List the inner-level CTS test identifiers for a given
                       top-level CTS test identifier without running them
 4. ctsrq.sh -b <Glassfish_Branch> -a [-e "<email-id1> <email-id2> .."]
                 --->  Run all the available top-level CTS test identifiers
                       against the specified local Glassfish branch and email
               the results to the specified email-ids
 5. ctsrq.sh -b <Glassfish_Branch> -t "<test_id1> <test_id2> <test_id3>"
                                  [-e "email-id1 email-id2 .."]
                 --->  Run the specified CTS test identifiers against the
                       specified local Glassfish branch and email the results
               to the specified email-ids. Note that double quotes are
                       necessary if providing multiple test-ids.
 6. ctsrq.sh -b <Glassfish_Branch> -a|-t
                         -c "</net/<Host_name>/<cts_bundle_location>/*.zip"
         --->  Run the specified CTS test identifiers against specified
               branch using the locally built CTS bundle. Note that CTS
               bundle location should be network accessible.
 7. ctsrq.sh -u <glassfish binary url>  -a|-t
         --->  For running all tests or specified tests with GlassFish
               binary provided in the http url. Note that -u option
               works with -a and -t options as well.
 8. ctsrq.sh [-h]--->  Show this message
EOF
)

################################## Variables ##################################

# Array to hold the valid CTS test identifiers
declare -a valid_test_ids

################################## Functions ##################################

main() {
    # Make sure that CTS_REMOTE_QUEUE_URL env variable is set
    [[ -n "${CTS_REMOTE_QUEUE_URL}" ]] || { echo "CTS_REMOTE_QUEUE_URL environment variable must be set"; exit 1; }

    # Print the usage and exit if no arguments given
    [[ $# -eq 0 ]] && { echo -e "${USAGE}"; exit 0; }

    valid_test_ids=`list_test_ids`
    cts_bundle_location="remote"
    # Process the input arguments
    while getopts ":b:t:i:e:c:u:alsh" opt; do
        case "$opt" in
            b) gfs_branch=$OPTARG;;
            t) test_ids=($OPTARG);;
        c) cts_bundle_location=$OPTARG;;
            a) test_ids=(`list_top_level_test_ids`);;
            l) print_test_ids "${valid_test_ids}";;
            s) print_test_ids "$(list_top_level_test_ids)";;
            i) print_test_ids "$(list_inner_level_test_ids $OPTARG)";;
            e) email_ids=$OPTARG;;
        u) gfs_url=$OPTARG;;
            h) echo -e "${USAGE}"; exit 0;;
           \?) echo "Invalid option -$OPTARG is specified"; exit 1;;
            :) case "$OPTARG" in
                b) echo -e "Branch must be specified for the option -$OPTARG"; exit 1;;
                t) echo -e "Test Ids must be specified for the option -$OPTARG"; exit 1;;
                i) echo -e "Top level test id must be specified for the option -$OPTARG"; exit 1;;
                e) echo -e "Comma separated email-ids must be specified for the option -$OPTARG"; exit 1;;
               esac
        esac
    done

    shift "$((OPTIND-1))"

    [[ -n "${gfs_branch}" ]] || [[ -n "${gfs_url}" ]] || { echo "Branch or glassfish binary url must be specified"; exit 1; }
    [[ -n "${test_ids}" ]] || { echo "Test ids must be specified using -t option"; exit 1; }

    validate_given_test_ids ${test_ids[@]}

    fork_origin=`git config --get remote.origin.url`
    test_ids_encoded=`echo ${test_ids[@]} | tr ' ' '+'`
    email_ids_encoded=`echo ${email_ids} | tr ' ' '+'`
    # Generate a unique id for the job which will be used for identifying the job id later
    unique_id=$RANDOM

    params="BRANCH=${gfs_branch}&TEST_IDS=${test_ids_encoded}&FORK_ORIGIN=${fork_origin}&UNIQUE_ID=${unique_id}&EMAIL_IDS=${email_ids_encoded}&CTS_BUNDLE_LOCATION=${cts_bundle_location}&GLASSFISH_URL=${gfs_url}"
    last_build=`get_last_build_number`

    # Trigger the build
    curl -g -X POST "${CTS_REMOTE_QUEUE_URL}/buildWithParameters?${params}&delay=0sec" 2> /dev/null

    # Get the build number
    build_number=`find_job_id ${last_build} ${unique_id}`

    print_result ${build_number}
}

# Lists all the available CTS test identifiers
list_test_ids() {
    local test_ids_location=${CTS_TEST_IDS_LOCATION}
    local default_test_ids_location_xpath="${CTS_REMOTE_QUEUE_URL}/api/xml?xpath=//action/parameterDefinition[name='"'CTS_TEST_IDS_LOCATION'"']/defaultParameterValue/value/text()"
    [ ${test_ids_location} ] || test_ids_location=$(curl -g "${default_test_ids_location_xpath}" 2> /dev/null)
    echo $(curl -g "${test_ids_location}" 2> /dev/null)
}

print_test_ids() {
    echo "${1}" | tr " " "\n"; exit 0;
}

# Lists all the available top-level CTS test identifiers
list_top_level_test_ids() {
    local idx=0
    for test_id in ${valid_test_ids[@]}; do
        [[ ${test_id} == */* ]] || { top_level_test_ids[$idx]=${test_id}; let idx++; }
    done
    echo ${top_level_test_ids[@]}
}

# Lists the inner-level CTS test identifiers for a given top-level CTS test identifier
list_inner_level_test_ids() {
    local top_level_test_id=${1}
    local idx=0
    for test_id in ${valid_test_ids[@]}; do
        [[ ${test_id} =~ ^"${top_level_test_id}/"* ]] && { inner_level_test_ids[$idx]=${test_id}; let idx++; }
    done
    echo ${inner_level_test_ids[@]}
}

# Validates the given test ids against the valid list
validate_given_test_ids() {
    local given_test_ids=${1}
    for given_test_id in ${given_test_ids[@]}; do
        [[ $(is_test_id_valid $given_test_id) = true ]] || { echo "Invalid test id '${given_test_id}' is specified"; exit 1; }
    done
}

# Checks whether the given test id is a valid one (or) not
is_test_id_valid() {
    local given_test_id=${1}
    for valid_test_id in ${valid_test_ids[@]}; do
        [[ "$given_test_id" = "$valid_test_id" ]] &&  { echo true; return 0; }
    done
    echo false
}

# Find the job id using the unique id that is generated while submitting the job
find_job_id(){
    local last_build=${1}
    local given_unique_id=${2}
    local latest_last_build=(`get_last_build_number`)

    # nothing running and nothing new completed
    [[ "${last_build}" = "${latest_last_build}" ]] && { echo ${last_build}; return 1; }

    # look into the newly completed run
    local i=$((last_build+1))
    while [ ${i} -le ${latest_last_build} ]; do
        local unique_id_location="${CTS_REMOTE_QUEUE_URL}/${i}/api/xml?xpath=//action/parameter[name='"'UNIQUE_ID'"']/value/text()"
        local unique_id=$(curl -g ${unique_id_location} 2> /dev/null)
        [[ "${unique_id}" == "${given_unique_id}" ]] && { echo ${i}; return 0; }
        let i++
    done

    # not found
    return 1
}

# Gets the build number of the last cts remote queue job which includes the currently on-going runs too
get_last_build_number(){
    local url="${CTS_REMOTE_QUEUE_URL}/api/xml?xpath=//lastBuild/number/text()"
    curl -g "${url}" 2> /dev/null
    [[ ${?} -eq 0 ]] || exit 1
}

print_result() {
    local build_number=${1}
    echo "----------------------------------------------------------------------"
    [[ ! -z ${build_number} ]] \
        && { echo "CTS RQ triggered successfully. Please find the RQ link below."; echo "${CTS_REMOTE_QUEUE_URL}/${build_number}"; } \
        || { echo "Issue in CTS RQ client. Please check your git settings."; }
    echo "----------------------------------------------------------------------"
}

############################ Execution starts from here ############################
main "$@"

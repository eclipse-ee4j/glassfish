#!/bin/bash
set -e;
on_exit () {
    EXIT_CODE=$?
    set +e;
    ps -lAf;
    asadmin stop-domain --force --kill;
    exit $EXIT_CODE;
}
trap on_exit EXIT

env|sort && asadmin start-domain --debug=${AS_DEBUG_PORT_ENABLED} --verbose

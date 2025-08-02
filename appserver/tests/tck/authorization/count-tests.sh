#!/bin/bash
find "$1" -name "TEST-*.xml" -path "*/target/surefire-reports/*" -o -path "*/target/failsafe-reports/*"\
| xargs grep "tests=" \
| awk -F'"' '{sum += $6} END {print "Total number of executed tests:", sum}'

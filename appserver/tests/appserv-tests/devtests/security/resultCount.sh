#!/bin/sh
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



FILES="$APS_HOME/test_resultsValid.xml $APS_HOME/security-gtest-results.xml"

TOTAL=799
PASSED=0
FAILED=0
for i in $FILES
do
    echo "input file=$i"
    P=`grep "\"pass\"" $i |  wc -l`
    F=`grep "\"fail\"" $i |  wc -l`
    PASSED=`expr $PASSED + $P`
    FAILED=`expr $FAILED + $F`
done
TOTAL_RUN=`expr $PASSED + $FAILED `
DNR=`expr $TOTAL - $TOTAL_RUN `

echo ""
echo "************************"
echo "PASSED=   $PASSED"
echo "------------  ========="
echo "FAILED=   $FAILED"
echo "------------  ========="
echo "DID NOT RUN=   $DNR"
echo "------------  ========="
echo "Total Expected=$TOTAL"
echo "************************"
echo ""

echo "************************">$APS_HOME/devtests/security/count.txt;
date>>$APS_HOME/devtests/security/count.txt;
echo "-----------------------">>$APS_HOME/devtests/security/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/security/count.txt;
echo "------------  =========">>$APS_HOME/devtests/security/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/security/count.txt;
echo "************************">>$APS_HOME/devtests/security/count.txt;

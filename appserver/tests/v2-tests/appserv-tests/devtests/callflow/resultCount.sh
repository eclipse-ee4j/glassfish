#!/bin/sh
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

FILE=$APS_HOME/test_resultsValid.xml
echo "input file=$FILE"

TOTAL=2
PASSED=`grep "pass" $FILE | wc -l`
FAILED=`grep "fail" $FILE | wc -l`
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

echo "************************">>$APS_HOME/devtests/callflow/count.txt;
date>>$APS_HOME/devtests/callflow/count.txt;
echo "-----------------------">>$APS_HOME/devtests/callflow/count.txt;
echo "PASSED=   $PASSED">>$APS_HOME/devtests/callflow/count.txt;
echo "------------  =========">>$APS_HOME/devtests/callflow/count.txt;
echo "FAILED=   $FAILED">>$APS_HOME/devtests/callflow/count.txt;
echo "------------  =========">>$APS_HOME/devtests/callflow/count.txt;
echo "DID NOT RUN=   $DNR">>$APS_HOME/devtests/callflow/count.txt;
echo "------------  =========">>$APS_HOME/devtests/callflow/count.txt;
echo "Total Expected=$TOTAL">>$APS_HOME/devtests/callflow/count.txt;
echo "************************">>$APS_HOME/devtests/callflow/count.txt;

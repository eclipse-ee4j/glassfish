#!/bin/sh
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

echo "S1AS_HOME is => " $S1AS_HOME
echo " APS_HOME is =? " $APS_HOME

$S1AS_HOME/lib/ant/bin/ant run
cd $APS_HOME
gmake stopPB
gmake startPB
cd -
#$S1AS_HOME/bin/asadmin set domain.resources.jdbc-connection-pool.jdbc-pointbase-pool.is_connection_validation_required=true
$S1AS_HOME/lib/ant/bin/ant run

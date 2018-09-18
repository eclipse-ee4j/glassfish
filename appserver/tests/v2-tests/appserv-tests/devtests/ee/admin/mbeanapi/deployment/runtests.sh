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

#
# This script assumes that the appserv-tests directory is a subdir of the main build-dir
#

s1as=/ee
java=${s1as}/jdk/bin/java

base=../../../../../..
cp1="${base}/publish/JDK1.4_DBG.OBJ/admin-core/mbeanapi/lib/mbeanapi.jar"
cp2="${base}/publish/JDK1.4_DBG.OBJ/jmx/lib/jmxri.jar"
cp3="${base}/publish/JDK1.4_DBG.OBJ/rjmx-ri/jmxremote.jar"
cp4="${base}/appserv-tests/devtests/ee/admin/mbeanapi/deployment/build"
cp=${cp1}:${cp2}:${cp3}:${cp4}

${java} -cp ${cp} -ea   com.sun.enterprise.admin.mbeanapi.deployment.DeploymentTestsAuto

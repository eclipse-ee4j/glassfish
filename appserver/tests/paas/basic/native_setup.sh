#
# Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

GF_HOME=${GF_HOME:-$S1AS_HOME}
echo "Your GlassFish is at $GF_HOME"
$GF_HOME/bin/asadmin start-domain --debug
$GF_HOME/bin/asadmin create-ims-config-native
#$GF_HOME/bin/asadmin create-server-pool --virtualization Native --subnet 192.168.1.102/250 --portName "br0" Native
$GF_HOME/bin/asadmin stop-domain
#$GF_HOME/bin/asadmin create-template --indexes ServiceType=JavaEE,VirtualizationType=Native Native
#$GF_HOME/bin/asadmin create-template --indexes ServiceType=Database,VirtualizationType=Native DBNative

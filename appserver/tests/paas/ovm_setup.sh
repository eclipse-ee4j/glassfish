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
export PATH=$GF_HOME/bin:$PATH
TEMPLATES_DIR=/tmp
CONNECTION_STRING=http://adminUser:adminPassword@hostName:port;rootUser:rootPassword
POOL_SUBNET=xx.xx.xxx.xx/xx
ORACLE_USER=oracle
ORACLE_GROUP=oracleGroup

asadmin start-domain domain1

asadmin create-ims-config-ovm --connectionstring $CONNECTION_STRING  ovm
asadmin create-server-pool --subnet $POOL_SUBNET --portname "foobar" --virtualization ovm pool2

asadmin create-template --files $TEMPLATES_DIR/GLASSFISH_TINY.tgz --indexes ServiceType=JavaEE,VirtualizationType=OVM GLASSFISH_TINY
asadmin create-template-user --virtualization ovm --userid glassfish --groupid glassfish --template GLASSFISH_TINY glassfish

asadmin create-template --files $TEMPLATES_DIR/ORACLEDB.tgz --indexes ServiceType=Database,VirtualizationType=OVM ORACLE_DATABASE
asadmin create-template-user --virtualization ovm --userid $ORACLE_USER --groupid $ORACLE_GROUP --template ORACLE_DATABASE oracle

#asadmin create-template --files $TEMPLATES_DIR/DERBY_DATABASE.tgz --indexes ServiceType=Database,VirtualizationType=OVM DERBY_DATABASE
#asadmin create-template-user --virtualization ovm --userid glassfish --groupid glassfish --template DERBY_DATABASE glassfish

#asadmin create-template --files $TEMPLATES_DIR/OTD_LARGE.tgz --properties vendor-name=otd --indexes ServiceType=LB,VirtualizationType=OVM otd-new
#asadmin create-template-user --virtualization ovm --userid 1000 --groupid 1000 --template otd-new cloud

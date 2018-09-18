#
# Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
TEMPLATES_DIR=/space
$GF_HOME/bin/asadmin start-domain --debug
$GF_HOME/bin/asadmin create-ims-config-libvirt kvm
$GF_HOME/bin/asadmin set virtualizations.libvirt-virtualization.kvm.template-cache-size=0
$GF_HOME/bin/asadmin stop-domain
$GF_HOME/bin/asadmin start-domain --debug
$GF_HOME/bin/asadmin create-server-pool --virtualization kvm --subnet 192.168.122.70/250 --portName "virbr0" cloud

$GF_HOME/bin/asadmin create-template --virtualization kvm --files $TEMPLATES_DIR/glassfish.img,$TEMPLATES_DIR/glassfish.xml --indexes ServiceType=JavaEE,VirtualizationType=libvirt glassfish
$GF_HOME/bin/asadmin create-template-user --virtualization kvm --userid 1000 --groupid 1000 --template glassfish cloud

$GF_HOME/bin/asadmin create-template --virtualization kvm --files $TEMPLATES_DIR/glassfish.img,$TEMPLATES_DIR/glassfish.xml --indexes ServiceType=Database,VirtualizationType=libvirt javadb
$S1AS_HOME/bin/asadmin create-template-user --virtualization kvm --userid 1000 --groupid 1000 --template javadb cloud

#$GF_HOME/bin/asadmin create-template --virtualization kvm --files $TEMPLATES_DIR/MySQL.img,$TEMPLATES_DIR/MySQL.xml --indexes ServiceType=Database,VirtualizationType=libvirt,product-vendor=MySQL MySQL
#$S1AS_HOME/bin/asadmin create-template-user --virtualization kvm --userid 1000 --groupid 1000 --template MySQL mysqluser

#$GF_HOME/bin/asadmin create-template --virtualization kvm --files $TEMPLATES_DIR/oracledb.img,$TEMPLATES_DIR/oracledb.xml --indexes ServiceType=Database,VirtualizationType=libvirt oracledb
#$S1AS_HOME/bin/asadmin create-template-user --virtualization kvm --userid 1000 --groupid 1000 --template oracledb shalinikvm

$GF_HOME/bin/asadmin create-template --virtualization kvm --files $TEMPLATES_DIR/apache.img,$TEMPLATES_DIR/apache.xml --indexes ServiceType=LB,VirtualizationType=libvirt apachemodjk
$S1AS_HOME/bin/asadmin create-template-user --virtualization kvm --userid 1000 --groupid 1000 --template apachemodjk cloud

$GF_HOME/bin/asadmin create-machine --serverPool cloud --networkName localhost local
$GF_HOME/bin/asadmin create-machine-user --serverPool cloud --machine local --userId 1000 --groupId 1000 shalini

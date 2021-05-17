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

# Extract domain name from config.properties and start the domain
for x in `cat $APS_HOME/config.properties`
do
    varval=`echo $x |cut -d'=' -f1`

    if [ $varval = "admin.user" ];
    then
        AS_ADMIN_USER=`echo $x |cut -d'=' -f2`
    fi

    if [ $varval = "admin.password" ];
    then
        AS_ADMIN_PASSWORD=`echo $x |cut -d'=' -f2`
    fi

    if [ $varval = "admin.domain" ];
    then
        AS_ADMIN_DOMAIN=`echo $x |cut -d'=' -f2`
    fi
done

${S1AS_HOME}/bin/asadmin start-domain --user ${AS_ADMIN_USER} --password ${AS_ADMIN_PASSWORD} ${AS_ADMIN_DOMAIN}

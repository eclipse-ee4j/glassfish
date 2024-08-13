/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.connectors.service;

import com.sun.enterprise.connectors.authentication.AuthenticationService;
import com.sun.enterprise.connectors.authentication.BasicPasswordAuthenticationService;
import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;

import java.util.Set;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * This is Security administration service. Performs funtionality of
 * security map creation, deletion.
 *
 * @author Srikanth P
 */


public class ConnectorSecurityAdminServiceImpl extends ConnectorService {

    /**
     * Default constructor
     */
    public ConnectorSecurityAdminServiceImpl() {
    }


    /**
     * Obtain the authentication service associated with rar module.
     * Currently only the BasicPassword authentication is supported.
     *
     * @param rarName  Rar module Name
     * @param poolInfo Name of the pool. Used for creation of
     *                 BasicPasswordAuthenticationService
     * @return AuthenticationService
     */
    public AuthenticationService getAuthenticationService(String rarName,
                                                          PoolInfo poolInfo) {

        ConnectorDescriptor cd = _registry.getDescriptor(rarName);
        OutboundResourceAdapter obra = cd.getOutboundResourceAdapter();
        Set authMechs = obra.getAuthMechanisms();
        for (Object authMech : authMechs) {
            AuthMechanism authMechanism = (AuthMechanism) authMech;
            String mech = authMechanism.getAuthMechType();
            if (mech.equals("BasicPassword")) {
                return new BasicPasswordAuthenticationService(rarName, poolInfo);
            }
        }
        return null;
    }
}

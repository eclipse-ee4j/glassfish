/*
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

/**
 *    This generated bean class SunConnector matches the DTD element sun-connector
 *
 *    Generated on Mon May 13 13:36:49 PDT 2002
 *
 *    This class matches the root element of the DTD,
 *    and is the root of the following bean graph:
 *
 *      ResourceAdapter
 *        [attr: JndiName CDATA #REQUIRED ]
 *        [attr: MaxPoolSize CDATA 32]
 *        [attr: SteadyPoolSize CDATA 4]
 *        [attr: MaxWaitTimeInMillis CDATA 10000]
 *        [attr: IdleTimeoutInSeconds CDATA 1000]
 *             Description? - String
 *             PropertyElement[0,n] - Boolean
 *               [attr: Name CDATA #REQUIRED ]
 *               [attr: Value CDATA #REQUIRED ]
 *      RoleMap?
 *        [attr: MapId CDATA #REQUIRED ]
 *             Description? - String
 *             MapElement[0,n]
 *                    Principal[1,n]
 *                      [attr: UserName CDATA #REQUIRED ]
 *                           Description? - String
 *                    BackendPrincipal - Boolean
 *                      [attr: UserName CDATA #REQUIRED ]
 *                      [attr: Password CDATA #REQUIRED ]
 *                      [attr: Credential CDATA #REQUIRED ]
 *
 */

package com.sun.enterprise.deployment.runtime.connector;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

/**
 * This class was based on the schema2beans generated one modified
 * to remove its dependencies on schema2beans libraries.

 * @author  Jerome Dochez
 * @version
 */
public class SunConnector extends RuntimeDescriptor {

    static public final String RESOURCE_ADAPTER = "ResourceAdapter"; // NOI18N
    static public final String ROLE_MAP = "RoleMap"; // NOI18N

    // This attribute is mandatory
    public void setResourceAdapter(ResourceAdapter value) {
        this.setValue(RESOURCE_ADAPTER, value);
    }


    //
    public ResourceAdapter getResourceAdapter() {
        return (ResourceAdapter) this.getValue(RESOURCE_ADAPTER);
    }


    // This attribute is optional
    public void setRoleMap(RoleMap value) {
        this.setValue(ROLE_MAP, value);
    }


    //
    public RoleMap getRoleMap() {
        return (RoleMap) this.getValue(ROLE_MAP);
    }


    // This method verifies that the mandatory properties are set
    public boolean verify() {
        return true;
    }
}

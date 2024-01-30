/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.integration;

import java.security.PermissionCollection;

public interface DDPermissionsLoader {

    String SET_EE_POLICY = "createPolicy.eepermissions";

    /**
     * Pass the declared permission collection from the module handler to the classloader
     *
     * @param declaredPc the declared permission collection obtained from permissions.xml file throws AccessControlException if
     * caller has no privilege
     */
    void addDeclaredPermissions(PermissionCollection declaredPc) throws SecurityException;

    /**
     * Pass the EE permission to the classloader
     *
     * @param eePc EE permissions throws AccessControlException if caller has no privilege
     */
    void addEEPermissions(PermissionCollection eePc) throws SecurityException;

}

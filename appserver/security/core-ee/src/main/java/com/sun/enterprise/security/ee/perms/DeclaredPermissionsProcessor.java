/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.perms;

import java.net.MalformedURLException;
import java.security.PermissionCollection;

import org.glassfish.api.deployment.DeploymentContext;

public class DeclaredPermissionsProcessor extends PermissionsProcessor {

    private PermissionCollection orginalDeclaredPc;
    private PermissionCollection declaredPc;

    public DeclaredPermissionsProcessor(SMGlobalPolicyUtil.CommponentType type, DeploymentContext dc, PermissionCollection declPc)
            throws SecurityException {
        super(type, dc);
        orginalDeclaredPc = declPc;
        convertPathDeclaredPerms();
    }

    /**
     * get the declared permissions which have the file path adjusted for the right module
     *
     * @return adjusted declared permissions
     */
    public PermissionCollection getAdjustedDeclaredPermissions() {
        return declaredPc;
    }

    // conver the path for permissions
    private void convertPathDeclaredPerms() throws SecurityException {

        // revise the filepermission's path
        try {
            declaredPc = processPermisssonsForPath(orginalDeclaredPc, context);
        } catch (MalformedURLException e) {
            throw new SecurityException(e);
        }

    }
}

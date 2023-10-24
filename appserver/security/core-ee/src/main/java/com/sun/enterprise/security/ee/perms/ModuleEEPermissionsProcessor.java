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
import java.util.logging.Level;

import org.glassfish.api.deployment.DeploymentContext;

public class ModuleEEPermissionsProcessor extends PermissionsProcessor {

    private PermissionCollection eePc;

    public ModuleEEPermissionsProcessor(SMGlobalPolicyUtil.CommponentType type, DeploymentContext dc) throws SecurityException {
        super(type, dc);

        try {
            convertEEPermissionPaths();
        } catch (MalformedURLException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * get the EE permissions which have the file path adjusted for the right module
     *
     * @return adjusted EE permissions
     */
    public PermissionCollection getAdjustedEEPermission() {
        return eePc;
    }

    // conver the path for permissions
    private void convertEEPermissionPaths() throws MalformedURLException {
        // get server suppled default policy
        PermissionCollection defWarPc = SMGlobalPolicyUtil.getEECompGrantededPerms(type);

        // revise the filepermission's path
        eePc = processPermisssonsForPath(defWarPc, context);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Revised permissions = " + eePc);
        }

    }

}

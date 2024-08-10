/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.security.ee.perms.SMGlobalPolicyUtil.CommponentType;

import java.net.MalformedURLException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.api.deployment.DeploymentContext;

public class EarEEPermissionsProcessor extends PermissionsProcessor {

    // map recording the 'Jakarta EE component type' to its EE adjusted granted permissions
    private static final Map<CommponentType, PermissionCollection> compTypeToEEGarntsMap = new HashMap<>();

    public EarEEPermissionsProcessor(DeploymentContext dc) throws SecurityException {

        super(SMGlobalPolicyUtil.CommponentType.ear, dc);

        try {
            convertEEPermissionPaths(CommponentType.ejb);
            convertEEPermissionPaths(CommponentType.war);
            convertEEPermissionPaths(CommponentType.rar);
            convertEEPermissionPaths(CommponentType.car);

            // combine all ee permissions then assign to ear
            combineAllEEPermisssonsForEar();

        } catch (MalformedURLException e) {
            throw new SecurityException(e);
        }

    }

    /**
     * get the EE permissions which have the file path adjusted for the right module
     *
     * @return adjusted EE permissions
     */
    public PermissionCollection getAdjustedEEPermission(CommponentType type) {
        return compTypeToEEGarntsMap.get(type);
    }

    public Map<CommponentType, PermissionCollection> getAllAdjustedEEPermission() {
        return compTypeToEEGarntsMap;
    }

    // conver the path for permissions
    private void convertEEPermissionPaths(CommponentType cmpType) throws MalformedURLException {
        // get server suppled default policy
        PermissionCollection defWarPc = SMGlobalPolicyUtil.getEECompGrantededPerms(cmpType);

        // revise the filepermission's path
        PermissionCollection eePc = processPermisssonsForPath(defWarPc, context);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Revised permissions = " + eePc);
        }

        compTypeToEEGarntsMap.put(cmpType, eePc);
    }

    private PermissionCollection combineAllEEPermisssonsForEar() {

        if (compTypeToEEGarntsMap == null) {
            return null;
        }

        Permissions allEEPerms = new Permissions();

        addPermissions(allEEPerms, getAdjustedEEPermission(CommponentType.war));
        addPermissions(allEEPerms, getAdjustedEEPermission(CommponentType.ejb));
        addPermissions(allEEPerms, getAdjustedEEPermission(CommponentType.rar));
        // addPermissions(allEEPerms, getAdjustedEEPermission(CommponentType.car));

        compTypeToEEGarntsMap.put(CommponentType.ear, allEEPerms);

        return allEEPerms;
    }

    private void addPermissions(Permissions combined, PermissionCollection toAdd) {

        if (toAdd == null) {
            return;
        }

        Enumeration<Permission> enumAdd = toAdd.elements();
        while (enumAdd.hasMoreElements()) {
            Permission p = enumAdd.nextElement();
            combined.add(p);
        }

    }

}

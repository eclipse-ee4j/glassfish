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

import com.sun.enterprise.security.integration.DDPermissionsLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.PermissionCollection;
import java.security.PrivilegedExceptionAction;

import javax.xml.stream.XMLStreamException;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;

public class PermsArchiveDelegate {

    /**
     * Get the application or module packaged permissions
     *
     * @param type the type of the module, this is used to check the configured restriction for the type
     * @param context the deployment context
     * @return the module or app declared permissions
     * @throws SecurityException if permissions.xml has syntax failure, or failed for restriction check
     */
    public static PermissionCollection getDeclaredPermissions(SMGlobalPolicyUtil.CommponentType type, DeploymentContext context)
            throws SecurityException {

        try {
            File base = new File(context.getSource().getURI());

            XMLPermissionsHandler pHdlr = new XMLPermissionsHandler(base, type);

            PermissionCollection declaredPerms = pHdlr.getAppDeclaredPermissions();

            // further process the permissions for file path adjustment
            DeclaredPermissionsProcessor dpp = new DeclaredPermissionsProcessor(type, context, declaredPerms);

            PermissionCollection revisedWarDeclaredPerms = dpp.getAdjustedDeclaredPermissions();

            return revisedWarDeclaredPerms;
        } catch (XMLStreamException | SecurityException | FileNotFoundException e) {
            throw new SecurityException(e);
        }

    }

    /**
     * Get the EE permissions for the spcified module type
     *
     * @param type module type
     * @param dc the deployment context
     * @return the ee permissions
     */
    public static PermissionCollection processEEPermissions(SMGlobalPolicyUtil.CommponentType type, DeploymentContext dc) {

        ModuleEEPermissionsProcessor eePp = new ModuleEEPermissionsProcessor(type, dc);

        PermissionCollection eePc = eePp.getAdjustedEEPermission();

        return eePc;
    }

    /**
     * Get the declared permissions and EE permissions, then add them to the classloader
     *
     * @param type module type
     * @param context deployment context
     * @param classloader throws AccessControlException if caller has no privilege
     */
    public static void processModuleDeclaredAndEEPemirssions(SMGlobalPolicyUtil.CommponentType type, DeploymentContext context,
            ClassLoader classloader) throws SecurityException {

            if (!(classloader instanceof DDPermissionsLoader)) {
                return;
            }

            if (!(context instanceof ExtendedDeploymentContext)) {
                return;
            }

            DDPermissionsLoader ddcl = (DDPermissionsLoader) classloader;

            if (((ExtendedDeploymentContext) context).getParentContext() == null) {

                PermissionCollection declPc = getDeclaredPermissions(type, context);
                ddcl.addDeclaredPermissions(declPc);
            }

            PermissionCollection eePc = processEEPermissions(type, context);

            ddcl.addEEPermissions(eePc);

    }

    public static class SetPermissionsAction implements PrivilegedExceptionAction<Object> {

        private SMGlobalPolicyUtil.CommponentType type;
        private DeploymentContext context;
        private ClassLoader cloader;

        public SetPermissionsAction(SMGlobalPolicyUtil.CommponentType type, DeploymentContext dc, ClassLoader cl) {
            this.type = type;
            this.context = dc;
            this.cloader = cl;
        }

        @Override
        public Object run() throws SecurityException {

            processModuleDeclaredAndEEPemirssions(type, context, cloader);
            return null;
        }
    }

}

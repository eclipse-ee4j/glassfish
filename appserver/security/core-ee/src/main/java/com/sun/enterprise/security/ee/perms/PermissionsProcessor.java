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

import com.sun.logging.LogDomains;

import java.io.File;
import java.io.FilePermission;
import java.net.MalformedURLException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;

public class PermissionsProcessor {

    public static final String CURRENT_FOLDER = "*";

    public static final String TEMP_FOLDER = "SERVLET-CONTEXT-TEMPDIR";

    protected DeploymentContext context;
    protected SMGlobalPolicyUtil.CommponentType type;

    protected static final Logger logger = Logger.getLogger(LogDomains.SECURITY_LOGGER);

    public PermissionsProcessor(SMGlobalPolicyUtil.CommponentType type, DeploymentContext dc) throws SecurityException {

        this.type = type;
        this.context = dc;

    }

    protected static PermissionCollection processPermisssonsForPath(PermissionCollection originalPC, DeploymentContext dc)
            throws MalformedURLException {

        if (originalPC == null) {
            return originalPC;
        }

        Permissions revisedPC = new Permissions();

        Enumeration<Permission> pcEnum = originalPC.elements();
        while (pcEnum.hasMoreElements()) {
            Permission perm = pcEnum.nextElement();
            if (perm instanceof FilePermission) {
                processFilePermission(revisedPC, dc, (FilePermission) perm);
            } else {
                revisedPC.add(perm);
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Revised permissions = " + revisedPC);
        }

        return revisedPC;
    }

    // for file permission, make the necessary path change, then add permssion to classloader
    protected static void processFilePermission(PermissionCollection revisedPC, DeploymentContext dc, FilePermission fp)
            throws MalformedURLException {

        if (isFilePermforCurrentDir(fp)) {
            addFilePermissionsForCurrentDir(revisedPC, dc, fp);
        } else if (isFilePermforTempDir(fp)) {
            convertTempDirPermission(revisedPC, dc, fp);
        } else {
            revisedPC.add(fp);
        }
    }

    // check if a FilePermssion with target path as the "current"
    protected static boolean isFilePermforCurrentDir(FilePermission fp) {

        if (fp == null) {
            return false;
        }

        String name = fp.getName();
        if (!CURRENT_FOLDER.equals(name)) {
            return false;
        }

        return true;
    }

    // check if a FilePermssion with target path as the "servlet temp dir"
    protected static boolean isFilePermforTempDir(FilePermission fp) {

        if (fp == null) {
            return false;
        }

        String name = fp.getName();
        if (!TEMP_FOLDER.equals(name)) {
            return false;
        }

        return true;
    }

    // add the current folder for the file permission
    protected static void addFilePermissionsForCurrentDir(PermissionCollection revisedPC, DeploymentContext context, FilePermission perm)
            throws MalformedURLException {

        if (!isFilePermforCurrentDir(perm)) {
            // not recognized, add it as is
            revisedPC.add(perm);
            return;
        }

        String actions = perm.getActions();

        String rootDir = context.getSource().getURI().toURL().toString();
        Permission rootDirPerm = new FilePermission(rootDir, actions);
        revisedPC.add(rootDirPerm);
        Permission rootPerm = new FilePermission(rootDir + File.separator + "-", actions);
        revisedPC.add(rootPerm);

        if (context.getScratchDir("ejb") != null) {
            String ejbTmpDir = context.getScratchDir("ejb").toURI().toURL().toString();
            Permission ejbDirPerm = new FilePermission(ejbTmpDir, actions);
            revisedPC.add(ejbDirPerm);
            Permission ejbPerm = new FilePermission(ejbTmpDir + File.separator + "-", actions);
            revisedPC.add(ejbPerm);
        }

        if (context.getScratchDir("jsp") != null) {
            String jspdir = context.getScratchDir("jsp").toURI().toURL().toString();
            Permission jpsDirPerm = new FilePermission(jspdir, actions);
            revisedPC.add(jpsDirPerm);
            Permission jpsPerm = new FilePermission(jspdir + File.separator + "-", actions);
            revisedPC.add(jpsPerm);
        }
    }

    // convert 'temp' dir to the absolute path for permission of 'temp' path
    protected static Permission convertTempDirPermission(PermissionCollection revisedPC, DeploymentContext context, FilePermission perm)
            throws MalformedURLException {

        if (!isFilePermforTempDir(perm)) {
            return perm;
        }

        String actions = perm.getActions();

        if (context.getScratchDir("jsp") != null) {
            String jspdir = context.getScratchDir("jsp").toURI().toURL().toString();
            Permission jspDirPerm = new FilePermission(jspdir, actions);
            revisedPC.add(jspDirPerm);
            Permission jspPerm = new FilePermission(jspdir + File.separator + "-", actions);
            revisedPC.add(jspPerm);
            return jspPerm;
        }

        return perm;
    }

}

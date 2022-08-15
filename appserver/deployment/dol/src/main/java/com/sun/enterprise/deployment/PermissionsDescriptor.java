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

package com.sun.enterprise.deployment;

import com.sun.enterprise.security.integration.PermissionCreator;

import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.deployment.common.RootDeploymentDescriptor;

public class PermissionsDescriptor extends RootDeploymentDescriptor {

    private RootDeploymentDescriptor parent;

    private PermissionCollection declaredPerms;


    public PermissionsDescriptor() {

    }

    public RootDeploymentDescriptor getParent() {
        return parent;
    }

    public void setParent(RootDeploymentDescriptor parent) {
        this.parent = parent;
    }


    @Override
    public String getModuleID() {
        throw new RuntimeException();
    }

    @Override
    public String getDefaultSpecVersion() {

        return "7";
    }

    @Override
    public boolean isEmpty() {
        return declaredPerms != null &&
                declaredPerms.elements().hasMoreElements();
    }

    @Override
    public ArchiveType getModuleType() {
        throw new RuntimeException();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (parent == null) {
            return null;
        }
        return parent.getClassLoader();
    }

    @Override
    public boolean isApplication() {
        return false;
    }


    public void addPermissionItemdescriptor(PermissionItemDescriptor permItem) {
        permItem.setParent(this);
        addPermission(permItem);
    }

    public PermissionCollection getDeclaredPermissions() {
        return declaredPerms;
    }

    private void addPermission(PermissionItemDescriptor permItem)  {
        if (permItem == null) {
            return;
        }

        String classname = permItem.getPermissionClassName();
        String target = permItem.getTargetName();
        String actions = permItem.getActions();

        try {
            Permission pm = PermissionCreator.getInstance(classname, target, actions);
            if (pm != null) {
                if (declaredPerms == null) {
                    declaredPerms = new Permissions();
                }
                this.declaredPerms.add(pm);
            }
        } catch (ClassNotFoundException e) {
            throw new SecurityException(e);
        } catch (NoSuchMethodException e) {
            throw new SecurityException(e);
        } catch (InstantiationException e) {
            throw new SecurityException(e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(e);
        } catch (InvocationTargetException e) {
            throw new SecurityException(e);
        }
    }

}

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

import org.glassfish.deployment.common.Descriptor;


public class PermissionItemDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private String permClassName;
    private String targetName;
    private String actions;

    private PermissionsDescriptor parent;


    public PermissionItemDescriptor() {

    }

    public PermissionsDescriptor getParent() {
        return parent;
    }

    protected void setParent(PermissionsDescriptor parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Parent was already set to " + this.parent.getModuleID()
                + ", it cannot be set to " + parent.getModuleID());
        }
        this.parent = parent;
    }


    public String getPermissionClassName () {
        return permClassName;
    }

    public void setPermissionClassName (String permClassName) {
        this.permClassName = permClassName;
    }


    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {

        this.targetName = targetName;
    }


    public String getActions() {
        return this.actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }
}

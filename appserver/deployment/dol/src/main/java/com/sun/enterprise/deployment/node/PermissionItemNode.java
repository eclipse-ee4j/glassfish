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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.PermissionItemDescriptor;
import com.sun.enterprise.deployment.xml.DeclaredPermissionsTagNames;

import java.util.HashMap;
import java.util.Map;

public class PermissionItemNode extends DeploymentDescriptorNode {


    private Map<String, String> dispatchTable;

    private PermissionItemDescriptor pemItemDes;


    public PermissionItemNode() {

        if (handlers != null) handlers.clear();
        initDispatchTable();
    }



    private void initDispatchTable() {
        assert(dispatchTable == null);

        Map<String, String> table = new HashMap<String, String>();
        table.put(DeclaredPermissionsTagNames.PERM_CLASS_NAME, "setPermissionClassName");
        table.put(DeclaredPermissionsTagNames.PERM_TARGET_NAME, "setTargetName");
        table.put(DeclaredPermissionsTagNames.PERM_ACTIONS, "setActions");
        this.dispatchTable = table;
    }


    @Override
    public PermissionItemDescriptor getDescriptor() {
        if (pemItemDes == null)
            pemItemDes = new PermissionItemDescriptor();

        return pemItemDes;
    }

    @Override
    protected Map getDispatchTable() {
        return dispatchTable;
    }

}

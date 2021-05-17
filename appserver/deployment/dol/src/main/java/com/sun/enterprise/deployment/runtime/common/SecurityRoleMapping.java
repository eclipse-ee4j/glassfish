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

package com.sun.enterprise.deployment.runtime.common;

import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the in memory representation of the security-role-mapping
 * information.  Note that we are keeping just the literal Strings
 * in this object.  The implementation of Principal is not instantiated
 * here.  This is because 1) the dol should avoid loading any classes
 * as the classloaders used for deployment and runtime can be different.
 * 2) verifier uses this information and it has not access to the rolemaper
 * on the server.
 *
 * @author Jerome Dochez
 */
public class SecurityRoleMapping extends RuntimeDescriptor {

    private String roleName = null; //mandatory element
    private List<PrincipalNameDescriptor> principals =
                        new ArrayList<PrincipalNameDescriptor>();
    private List<String> groups = new ArrayList<String>();

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String name) {
        roleName = name;
    }

    public List<PrincipalNameDescriptor> getPrincipalNames() {
        return principals;
    }

    public void addPrincipalName(PrincipalNameDescriptor p) {
        principals.add(p);
    }

    public List<String> getGroupNames() {
        return groups;
    }

    public void addGroupName(String g) {
        groups.add(g);
    }

    /**
     *@deprecated
     *This method needs to be removed once the custom principal is fully
     *supported. We keep it for now for backward compatiblity in API.  Note
     *that this method only returns the name of the principals, not their
     *class-names.  Use with caution!
     */
    public String[] getPrincipalName() {
        String[] names = new String[principals.size()];
        for (int i = 0; i < principals.size(); i++) {
            names[i] = principals.get(i).getName();
        }
        return names;
    }
}

/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 * This is the in memory representation of the security-role-mapping information.
 *
 * @author Jerome Dochez
 */
public class SecurityRoleMapping extends RuntimeDescriptor {

    private static final long serialVersionUID = 1L;
    private String roleName; // mandatory element
    private final List<PrincipalNameDescriptor> principals = new ArrayList<>();
    private final List<String> groups = new ArrayList<>();

    public String getRoleName() {
        return roleName;
    }


    public void setRoleName(String name) {
        roleName = name;
    }


    public List<PrincipalNameDescriptor> getPrincipalNames() {
        return principals;
    }


    public void addPrincipalName(PrincipalNameDescriptor principalNameDescriptor) {
        principals.add(principalNameDescriptor);
    }


    public List<String> getGroupNames() {
        return groups;
    }


    public void addGroupName(String groupName) {
        groups.add(groupName);
    }
}

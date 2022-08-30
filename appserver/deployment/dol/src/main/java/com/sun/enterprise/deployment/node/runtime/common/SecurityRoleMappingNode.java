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

package com.sun.enterprise.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.common.PrincipalNameDescriptor;
import com.sun.enterprise.deployment.runtime.common.SecurityRoleMapping;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.List;

import org.w3c.dom.Node;

/**
 * This node handles all the role mapping information
 *
 * @author Jerome Dochez
 */
public class SecurityRoleMappingNode extends RuntimeDescriptorNode<SecurityRoleMapping> {

    @Override
    protected SecurityRoleMapping createDescriptor() {
        return new SecurityRoleMapping();
    }


    public SecurityRoleMappingNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.PRINCIPAL_NAME), PrincipalNameNode.class,
            "addPrincipalName");
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        SecurityRoleMapping srm = (SecurityRoleMapping) getDescriptor();
        if (RuntimeTagNames.ROLE_NAME.equals(element.getQName())) {
            srm.setRoleName(value);
        } else if (RuntimeTagNames.GROUP_NAME.equals(element.getQName())) {
            srm.addGroupName(value);
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, SecurityRoleMapping descriptor) {
        Node roleMapping = appendChild(parent, nodeName);

        // role-name
        appendTextChild(roleMapping, RuntimeTagNames.ROLE_NAME, descriptor.getRoleName());

        // principal-name+
        PrincipalNameNode principal = new PrincipalNameNode();
        List<PrincipalNameDescriptor> principals = descriptor.getPrincipalNames();
        for (PrincipalNameDescriptor principal2 : principals) {
            principal.writeDescriptor(roleMapping, RuntimeTagNames.PRINCIPAL_NAME, principal2);
        }

        // group+
        List<String> groups = descriptor.getGroupNames();
        for (String group : groups) {
            appendTextChild(roleMapping, RuntimeTagNames.GROUP_NAME, group);
        }
        return roleMapping;
    }
}

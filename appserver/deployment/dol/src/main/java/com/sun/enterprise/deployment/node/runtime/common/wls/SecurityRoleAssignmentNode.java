/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime.common.wls;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.common.wls.SecurityRoleAssignment;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.List;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This node handles all the role mapping information for weblogic-application.xml
 *
 * @author Sudarsan Sridhar
 */
public class SecurityRoleAssignmentNode extends RuntimeDescriptorNode<SecurityRoleAssignment> {

    @Override
    protected SecurityRoleAssignment createDescriptor() {
        return new SecurityRoleAssignment();
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        SecurityRoleAssignment sra = getDescriptor();
        if (RuntimeTagNames.ROLE_NAME.equals(element.getQName())) {
            sra.setRoleName(value);
        } else if (RuntimeTagNames.PRINCIPAL_NAME.equals(element.getQName())) {
            sra.addPrincipalName(value);
        } else if (RuntimeTagNames.EXTERNALLY_DEFINED.equals(element.getQName())) {
            sra.setExternallyDefined();
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, SecurityRoleAssignment descriptor) {
        Node roleMapping = appendChild(parent, nodeName);

        // role-name
        appendTextChild(roleMapping, RuntimeTagNames.ROLE_NAME, descriptor.getRoleName());

        // externally-defined
        if (descriptor.isExternallyDefined()) {
            appendChild(roleMapping, RuntimeTagNames.EXTERNALLY_DEFINED);
        }

        // principal-name+
        List<String> principals = descriptor.getPrincipalNames();
        for (String principal : principals) {
            appendTextChild(roleMapping, RuntimeTagNames.PRINCIPAL_NAME, principal);
        }
        return roleMapping;
    }


    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof WebBundleDescriptor) {
            WebBundleDescriptor webBundleDescriptor = (WebBundleDescriptor) parentDesc;
            // security-role-assignment*
            SunWebApp sunWebApp = webBundleDescriptor.getSunDescriptor();
            if (sunWebApp != null) {
                SecurityRoleAssignment[] securityRoleAssignments = sunWebApp.getSecurityRoleAssignments();
                for (SecurityRoleAssignment securityRoleAssignment : securityRoleAssignments) {
                    writeDescriptor(parent, nodeName, securityRoleAssignment);
                }
            }
        }
        return parent;
    }
}

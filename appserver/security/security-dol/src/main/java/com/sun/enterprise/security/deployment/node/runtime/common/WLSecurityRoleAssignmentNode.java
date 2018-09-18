/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.common.WLSecurityRoleAssignment;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Node;

import java.util.List;

/**
 * This node handles all the role mapping information for weblogic-application.xml
 *
 * @author  Sudarsan Sridhar
 * @version 
 */
public class WLSecurityRoleAssignmentNode extends RuntimeDescriptorNode {

    public WLSecurityRoleAssignmentNode() {
    }

    /**
     * receives notification of the value for a particular tag
     *                                               
     * @param element the xml element                
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        WLSecurityRoleAssignment sra = (WLSecurityRoleAssignment) getDescriptor();
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

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name 
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, WLSecurityRoleAssignment descriptor) {
        Node roleMapping = appendChild(parent, nodeName);

        //role-name
        appendTextChild(roleMapping, RuntimeTagNames.ROLE_NAME, descriptor.getRoleName());

        //externally-defined
        if (descriptor.isExternallyDefined()){
            appendChild(roleMapping, RuntimeTagNames.EXTERNALLY_DEFINED);
        }

        //principal-name+
        List<String> principals = descriptor.getPrincipalNames();
        for (int i = 0; i < principals.size(); i++) {
            appendTextChild(roleMapping, RuntimeTagNames.PRINCIPAL_NAME, principals.get(i));
        }
        return roleMapping;
    }
}

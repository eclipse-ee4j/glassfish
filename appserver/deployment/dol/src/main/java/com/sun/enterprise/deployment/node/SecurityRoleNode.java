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

package com.sun.enterprise.deployment.node;

import java.util.Map;

import com.sun.enterprise.deployment.SecurityRoleDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.security.common.Role;
import org.w3c.dom.Node;

/**
 *
 * @author  Jerome Dochez
 * @version
 */
public class SecurityRoleNode extends DeploymentDescriptorNode<SecurityRoleDescriptor> {

    private SecurityRoleDescriptor descriptor;

    @Override
    public SecurityRoleDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new SecurityRoleDescriptor();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.ROLE_NAME, "setName");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, Role descriptor) {
        Node roleNode = appendChild(parent, nodeName);
        appendTextChild(roleNode, TagNames.DESCRIPTION, descriptor.getDescription());
        appendTextChild(roleNode, TagNames.ROLE_NAME, descriptor.getName());
        return roleNode;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, SecurityRoleDescriptor descriptor) {
        Node roleNode = appendChild(parent, nodeName);
        appendTextChild(roleNode, TagNames.DESCRIPTION, descriptor.getDescription());
        appendTextChild(roleNode, TagNames.ROLE_NAME, descriptor.getName());
        return roleNode;
    }
}

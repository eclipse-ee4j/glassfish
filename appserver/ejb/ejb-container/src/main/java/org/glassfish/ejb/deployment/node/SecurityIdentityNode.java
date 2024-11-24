/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.RunAsNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.TagNames.RUNAS_SPECIFIED_IDENTITY;
import static org.glassfish.ejb.deployment.EjbTagNames.USE_CALLER_IDENTITY;

/**
 * This node handles all information relative to security-indentity tag
 *
 * @author Jerome Dochez
 */
public class SecurityIdentityNode extends DeploymentDescriptorNode<Descriptor> {

    public SecurityIdentityNode() {
        registerElementHandler(new XMLElement(RUNAS_SPECIFIED_IDENTITY), RunAsNode.class);
    }


    @Override
    public Descriptor getDescriptor() {
        return getParentNodeDescriptor();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table =  new HashMap<>();
        table.put(USE_CALLER_IDENTITY, "setUsesCallerIdentity");
        table.put(RUNAS_SPECIFIED_IDENTITY, "setRunAsIdentity");
        return table;
    }


    public EjbDescriptor getParentNodeDescriptor() {
        return (EjbDescriptor) super.getParentNode().getDescriptor();
    }


    /**
     * @param parent parent node
     * @param nodeName name of this node under the parent node.
     * @param descriptor parent descriptor.
     * @return new {@link Node}
     */
    public static Node writeSecureIdentity(Node parent, String nodeName, EjbDescriptor descriptor) {
        Node secureIdentityNode = appendChild(parent, nodeName);
        appendTextChild(secureIdentityNode, TagNames.DESCRIPTION, descriptor.getSecurityIdentityDescription());
        if (Boolean.TRUE.equals(descriptor.getUsesCallerIdentity())) {
            Node useCaller = secureIdentityNode.getOwnerDocument().createElement(USE_CALLER_IDENTITY);
            secureIdentityNode.appendChild(useCaller);
        } else if (Boolean.FALSE.equals(descriptor.getUsesCallerIdentity())) {
            RunAsNode runAs = new RunAsNode();
            runAs.writeDescriptor(secureIdentityNode, RUNAS_SPECIFIED_IDENTITY, descriptor.getRunAsIdentity());
        }
        return secureIdentityNode;
    }
}

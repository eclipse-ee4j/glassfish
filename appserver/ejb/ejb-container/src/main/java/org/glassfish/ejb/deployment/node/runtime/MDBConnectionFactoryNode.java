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

package org.glassfish.ejb.deployment.node.runtime;

import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.DefaultResourcePrincipalNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.glassfish.ejb.deployment.descriptor.runtime.MdbConnectionFactoryDescriptor;
import org.w3c.dom.Node;

public class MDBConnectionFactoryNode extends DeploymentDescriptorNode<MdbConnectionFactoryDescriptor> {

    private MdbConnectionFactoryDescriptor descriptor;

    public MDBConnectionFactoryNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.DEFAULT_RESOURCE_PRINCIPAL),
            DefaultResourcePrincipalNode.class);
    }


    @Override
    public MdbConnectionFactoryDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MdbConnectionFactoryDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map getDispatchTable() {
        Map dispatchTable = super.getDispatchTable();
        dispatchTable.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return dispatchTable;
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof ResourcePrincipalDescriptor) {
            descriptor.setDefaultResourcePrincipal((ResourcePrincipalDescriptor) newDescriptor);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, MdbConnectionFactoryDescriptor mcf) {
        Node mcfNode = super.writeDescriptor(parent, nodeName, mcf);
        appendTextChild(mcfNode, RuntimeTagNames.JNDI_NAME, mcf.getJndiName());
        if (mcf.getDefaultResourcePrincipal() != null) {
            DefaultResourcePrincipalNode subNode = new DefaultResourcePrincipalNode();
            subNode.writeDescriptor(mcfNode, RuntimeTagNames.DEFAULT_RESOURCE_PRINCIPAL,
                mcf.getDefaultResourcePrincipal());
        }
        return mcfNode;
    }
}

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

import java.util.HashMap;
import java.util.Map;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling the runtime resource
 * adapter configuration for the message-driven bean
 *
 * @author  Qingqing Ouyang
 * @version
 */
public class MDBResourceAdapterNode extends DeploymentDescriptorNode {

    public MDBResourceAdapterNode() {
        registerElementHandler(
                new XMLElement(RuntimeTagNames.ACTIVATION_CONFIG),
                ActivationConfigNode.class);
                //"setRuntimeActivationConfigDescriptor");
    }

    @Override
    public Object getDescriptor() {
        return getParentNode().getDescriptor();
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table =  new HashMap();
        table.put(RuntimeTagNames.RESOURCE_ADAPTER_MID, "setResourceAdapterMid");
        return table;
    }

    public Node writeDescriptor( Node parent,
            String nodeName, EjbMessageBeanDescriptor descriptor) {
        Node raNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(raNode, RuntimeTagNames.RESOURCE_ADAPTER_MID,
                descriptor.getResourceAdapterMid());

        ActivationConfigNode activationConfigNode = new ActivationConfigNode();
        activationConfigNode.writeDescriptor
            (raNode, RuntimeTagNames.ACTIVATION_CONFIG,
             descriptor.getRuntimeActivationConfigDescriptor());
        return raNode;
    }
}

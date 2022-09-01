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

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.InboundResourceAdapter;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;


/**
 * This node is responsible for handling the Connector DTD related inbound-resourceadapter XML tag
 *
 * @author Sheetal Vartak
 */
public class InBoundRANode extends DeploymentDescriptorNode<InboundResourceAdapter> {

    private InboundResourceAdapter descriptor;

    public static Node writeInboundResourceAdapter(Node connectorNode, InboundResourceAdapter descriptor) {
        Node inBoundNode = appendChild(connectorNode, ConnectorTagNames.INBOUND_RESOURCE_ADAPTER);
        Node msgAdapter = appendChild(inBoundNode, ConnectorTagNames.MSG_ADAPTER);
        MessageListenerNode.writeMessageListeners(msgAdapter, descriptor.getMessageListeners());
        return connectorNode;
    }


    /**
     * Default constructor
     */
    public InBoundRANode() {
        registerElementHandler(new XMLElement(ConnectorTagNames.MSG_LISTENER), MessageListenerNode.class);
    }


    /**
     * Doesn't do anything.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        return super.getDispatchTable();
    }


    @Override
    public InboundResourceAdapter getDescriptor() {
        if (descriptor == null) {
            // the descriptor associated with the InBoundRANode is a InboundResourceAdapter
            // This descriptor is available with the parent node of the InBoundRANode
            descriptor = DescriptorFactory.getDescriptor(getXMLPath());
            ((ConnectorDescriptor) (getParentNode().getDescriptor())).setInboundResourceAdapter(descriptor);

        }
        return descriptor;
    }


    @Override
    public void addDescriptor(Object obj) {
        if (obj instanceof MessageListener) {
            descriptor.addMessageListener((MessageListener) obj);
        }
    }
}

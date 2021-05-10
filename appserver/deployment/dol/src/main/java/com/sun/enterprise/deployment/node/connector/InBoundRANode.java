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

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.InboundResourceAdapter;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import java.util.Map;


/**
 * This node is responsible for handling the Connector DTD related inbound-resourceadapter XML tag
 *
 * @author  Sheetal Vartak
 * @version
 */
public class InBoundRANode extends DeploymentDescriptorNode {

    private InboundResourceAdapter descriptor = null;

    // default constructor
    public InBoundRANode() {
        registerElementHandler(new XMLElement(ConnectorTagNames.MSG_LISTENER), MessageListenerNode.class);
    }


    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
    }


    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        return table;
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        if (descriptor == null) {
            // the descriptor associated with the InBoundRANode is a InboundResourceAdapter
            // This descriptor is available with the parent node of the InBoundRANode
            descriptor = (InboundResourceAdapter) DescriptorFactory.getDescriptor(getXMLPath());
            ((ConnectorDescriptor) (getParentNode().getDescriptor())).setInboundResourceAdapter(descriptor);

        }
        return descriptor;
    }


    /**
     * Adds a new DOL descriptor instance to the descriptor instance associated with
     * this XMLNode
     *
     * @param descriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object obj) {
        if (obj instanceof MessageListener) {
            descriptor.addMessageListener((MessageListener) obj);
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node connectorNode, Descriptor descriptor) {
        Node inBoundNode = appendChild(connectorNode, ConnectorTagNames.INBOUND_RESOURCE_ADAPTER);
        appendInBoundNode(inBoundNode, ((ConnectorDescriptor) descriptor).getInboundResourceAdapter());
        return connectorNode;
    }


    /**
     * method to add the child nodes of INBOUND_RESOURCE_ADAPTER
     */
    private void appendInBoundNode(Node inBoundNode, InboundResourceAdapter conDesc) {
        Node msgAdapter = appendChild(inBoundNode, ConnectorTagNames.MSG_ADAPTER);

        MessageListenerNode msgListener = new MessageListenerNode();
        msgListener.writeDescriptor(msgAdapter, conDesc);
    }

}

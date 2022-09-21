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

import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling the Connector DTD related message-listener XML tag
 *
 * @author Sheetal Vartak
 */
public class MessageListenerNode extends DeploymentDescriptorNode<MessageListener> {

    private MessageListener msgListener;

    public static Node writeMessageListeners(Node parent, Set<MessageListener> msgListeners) {
        if (msgListeners.isEmpty()) {
            throw new RuntimeException("There must be at least one messagelistener for this inbound resource adapter");
        }
        for (MessageListener listener : msgListeners) {
            Node msgListenerNode = appendChild(parent, ConnectorTagNames.MSG_LISTENER);
            appendTextChild(msgListenerNode, ConnectorTagNames.MSG_LISTENER_TYPE, listener.getMessageListenerType());
            ActivationSpecNode.writeMessageListener(msgListenerNode, listener);
        }
        return parent;
    }


    public MessageListenerNode() {
        registerElementHandler(new XMLElement(ConnectorTagNames.ACTIVATION_SPEC), ActivationSpecNode.class);
    }


    @Override
    public MessageListener getDescriptor() {
        if (msgListener == null) {
            msgListener = (MessageListener) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return msgListener;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.MSG_LISTENER_TYPE, "setMessageListenerType");
        return table;
    }
}

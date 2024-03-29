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

import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling the Connector DTD related activationspec XML tag
 *
 * @author Sheetal Vartak
 */
public class ActivationSpecNode extends DeploymentDescriptorNode<MessageListener> {

    private MessageListener msgListener;

    public static Node writeMessageListener(Node parent, MessageListener msgListener) {
        Node actSpecNode = appendChild(parent, ConnectorTagNames.ACTIVATION_SPEC);
        appendTextChild(actSpecNode, ConnectorTagNames.ACTIVATION_SPEC_CLASS, msgListener.getActivationSpecClass());
        RequiredConfigNode.write(actSpecNode, msgListener);
        ConfigPropertyNode.write(actSpecNode, msgListener);
        return parent;
    }


    public ActivationSpecNode() {
        registerElementHandler(new XMLElement(ConnectorTagNames.REQUIRED_CONFIG_PROP), RequiredConfigNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
    }


    @Override
    public MessageListener getDescriptor() {
        if (msgListener == null) {
            msgListener = (MessageListener) getParentNode().getDescriptor();
        }
        return msgListener;
    }


    @Override
    public void addDescriptor(Object descriptor) {
        if (descriptor instanceof ConnectorConfigProperty) {
            msgListener.addConfigProperty((ConnectorConfigProperty) descriptor);
        } else if (descriptor instanceof EnvironmentProperty) {
            msgListener.addRequiredConfigProperty((EnvironmentProperty) descriptor);
        }
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.ACTIVATION_SPEC_CLASS, "setActivationSpecClass");
        return table;
    }
}

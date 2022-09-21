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

package com.sun.enterprise.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityDescriptor;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles message-security-binding element
 *
 */
public class MessageSecurityBindingNode extends DeploymentDescriptorNode<MessageSecurityBindingDescriptor> {

    private MessageSecurityBindingDescriptor descriptor;

    public MessageSecurityBindingNode() {
        registerElementHandler(new XMLElement(WebServicesTagNames.MESSAGE_SECURITY), MessageSecurityNode.class,
            "addMessageSecurityDescriptor");
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public MessageSecurityBindingDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MessageSecurityBindingDescriptor();
        }
        return descriptor;
    }

    /**
     * parsed an attribute of an element
     *
     * @param the element name
     * @param the attribute name
     * @param the attribute value
     * @return true if the attribute was processed
     */
    @Override
    protected boolean setAttributeValue(XMLElement elementName,
        XMLElement attributeName, String value) {
        if (attributeName.getQName().equals(WebServicesTagNames.AUTH_LAYER)) {
            descriptor.setAttributeValue(MessageSecurityBindingDescriptor.AUTH_LAYER, value);
            return true;
        } else if (attributeName.getQName().equals(
            WebServicesTagNames.PROVIDER_ID)) {
            descriptor.setAttributeValue(MessageSecurityBindingDescriptor.PROVIDER_ID, value);
            return true;
        }
        return false;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName,
        MessageSecurityBindingDescriptor messageSecurityBindingDesc) {
        Element messageSecurityBindingNode = (Element) super.writeDescriptor(parent, nodeName, messageSecurityBindingDesc);

        // message-security
        List<MessageSecurityDescriptor> messageSecDescs = messageSecurityBindingDesc.getMessageSecurityDescriptors();
        if (!messageSecDescs.isEmpty()) {
            MessageSecurityNode messageSecurityNode = new MessageSecurityNode();
            for (MessageSecurityDescriptor msDescriptor : messageSecDescs) {
                messageSecurityNode.writeDescriptor(messageSecurityBindingNode, WebServicesTagNames.MESSAGE_SECURITY,
                    msDescriptor);
            }
        }

        // auth-layer
        if (messageSecurityBindingDesc.getAttributeValue(MessageSecurityBindingDescriptor.AUTH_LAYER) != null) {
            setAttribute(messageSecurityBindingNode, WebServicesTagNames.AUTH_LAYER,
                messageSecurityBindingDesc.getAttributeValue(MessageSecurityBindingDescriptor.AUTH_LAYER));
        }

        // provider-id
        if (messageSecurityBindingDesc.getAttributeValue(MessageSecurityBindingDescriptor.PROVIDER_ID) != null) {
            setAttribute(messageSecurityBindingNode, WebServicesTagNames.PROVIDER_ID,
                messageSecurityBindingDesc.getAttributeValue(MessageSecurityBindingDescriptor.PROVIDER_ID));
        }

        return messageSecurityBindingNode;
    }
}

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

package com.sun.enterprise.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.common.MessageDescriptor;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityDescriptor;
import com.sun.enterprise.deployment.runtime.common.ProtectionDescriptor;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This node handles message-security element
 *
 */
public class MessageSecurityNode extends DeploymentDescriptorNode {

    MessageSecurityDescriptor descriptor = null;

    public MessageSecurityNode() {
        registerElementHandler(new XMLElement(
            WebServicesTagNames.MESSAGE), MessageNode.class,
            "addMessageDescriptor");
        registerElementHandler(new XMLElement(
            WebServicesTagNames.REQUEST_PROTECTION), ProtectionNode.class,
            "setRequestProtectionDescriptor");
        registerElementHandler(new XMLElement(
            WebServicesTagNames.RESPONSE_PROTECTION), ProtectionNode.class,
            "setResponseProtectionDescriptor");
    }

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
       if (descriptor == null) {
            descriptor = new MessageSecurityDescriptor();
        }
        return descriptor;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
        MessageSecurityDescriptor messageSecurityDesc) {
        Node messageSecurityNode = super.writeDescriptor(parent, nodeName,
           messageSecurityDesc);

        ArrayList messageDescs =
            messageSecurityDesc.getMessageDescriptors();
        if (!messageDescs.isEmpty()) {
            MessageNode messageNode = new MessageNode();
            for (Iterator messageIterator = messageDescs.iterator();
                messageIterator.hasNext();) {
                MessageDescriptor messageDesc =
                    (MessageDescriptor) messageIterator.next();
                messageNode.writeDescriptor(messageSecurityNode,
                    WebServicesTagNames.MESSAGE, messageDesc);
            }
        }

        // request-protection
        ProtectionDescriptor requestProtectionDesc =
            messageSecurityDesc.getRequestProtectionDescriptor();
        if (requestProtectionDesc != null) {
            ProtectionNode requestProtectionNode = new ProtectionNode();
            requestProtectionNode.writeDescriptor(messageSecurityNode,
                WebServicesTagNames.REQUEST_PROTECTION, requestProtectionDesc);
        }

        // response-protection
        ProtectionDescriptor responseProtectionDesc =
            messageSecurityDesc.getResponseProtectionDescriptor();
        if (responseProtectionDesc != null) {
            ProtectionNode responseProtectionNode = new ProtectionNode();
            responseProtectionNode.writeDescriptor(messageSecurityNode,
                WebServicesTagNames.RESPONSE_PROTECTION,
                    responseProtectionDesc);
        }

        return messageSecurityNode;
    }
}

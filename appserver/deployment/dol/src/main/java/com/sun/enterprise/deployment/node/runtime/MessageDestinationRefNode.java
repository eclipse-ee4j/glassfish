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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling runtime descriptor
 * message-destination-ref tag
 *
 */
public class MessageDestinationRefNode extends DeploymentDescriptorNode<MessageDestinationReferenceDescriptor> {

    private MessageDestinationReferenceDescriptor descriptor;

    /**
     * writes all the runtime information for JMS destination references
     *
     * @param parent node to add the runtime xml info
     * @param descriptor the J2EE component containing message destination references
     */
    public static void writeMessageDestinationReferences(Node parent, MessageDestinationReferenceContainer descriptor) {
        // message-destination-ref*
        Iterator<MessageDestinationReferenceDescriptor> msgDestRefs = descriptor
            .getMessageDestinationReferenceDescriptors().iterator();
        if (!msgDestRefs.hasNext()) {
            return;
        }
        MessageDestinationRefNode messageDestinationRefNode = new MessageDestinationRefNode();
        while (msgDestRefs.hasNext()) {
            MessageDestinationReferenceDescriptor next = msgDestRefs.next();
            messageDestinationRefNode.write(parent, TagNames.MESSAGE_DESTINATION_REFERENCE, next);
        }
    }


    @Override
    public MessageDestinationReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MessageDestinationReferenceDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.MESSAGE_DESTINATION_REFERENCE_NAME.equals(element.getQName())) {
            XMLNode<?> parentNode = getParentNode();
            Object parentDesc = null;
            // in case of web
            if (parentNode.getDescriptor() instanceof WebBundleDescriptor) {
                parentDesc = parentNode.getDescriptor();
            // in case of appclient and ejb
            } else {
                parentDesc = getParentNode().getDescriptor();
            }

            if (parentDesc instanceof MessageDestinationReferenceContainer) {
                try {
                    descriptor = ((MessageDestinationReferenceContainer) parentDesc)
                        .getMessageDestinationReferenceByName(value);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }

            }
        } else {
            super.setElementValue(element, value);
        }
    }


    private Node write(Node parent, String nodeName, MessageDestinationReferenceDescriptor msgDestRef) {
        Node msgDestRefNode = super.writeDescriptor(parent, nodeName, msgDestRef);
        appendTextChild(msgDestRefNode, TagNames.MESSAGE_DESTINATION_REFERENCE_NAME, msgDestRef.getName());
        appendTextChild(msgDestRefNode, RuntimeTagNames.JNDI_NAME, msgDestRef.getJndiName());
        return msgDestRefNode;
    }
}

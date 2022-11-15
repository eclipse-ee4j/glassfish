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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This class handles all information related to the message-destination-ref
 * xml tag
 *
 * @author  Kenneth Saks
 */
public class MessageDestinationRefNode extends DeploymentDescriptorNode<MessageDestinationReferenceDescriptor> {

    private MessageDestinationReferenceDescriptor descriptor;

    public MessageDestinationRefNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.INJECTION_TARGET), InjectionTargetNode.class,
            "addInjectionTarget");
    }

    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.MESSAGE_DESTINATION_REFERENCE_NAME, "setName");
        table.put(TagNames.MESSAGE_DESTINATION_TYPE, "setDestinationType");
        table.put(TagNames.MESSAGE_DESTINATION_USAGE, "setUsage");
        table.put(TagNames.MESSAGE_DESTINATION_LINK, "setMessageDestinationLinkName");
        table.put(TagNames.MAPPED_NAME, "setMappedName");
        table.put(TagNames.LOOKUP_NAME, "setLookupName");
        return table;
    }

    @Override
    public MessageDestinationReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MessageDestinationReferenceDescriptor();
        }
        return descriptor;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, MessageDestinationReferenceDescriptor desc) {
        Node msgDestRefNode = appendChild(parent, nodeName);

        writeLocalizedDescriptions(msgDestRefNode, desc);

        appendTextChild(msgDestRefNode, TagNames.MESSAGE_DESTINATION_REFERENCE_NAME, desc.getName());
        appendTextChild(msgDestRefNode, TagNames.MESSAGE_DESTINATION_TYPE, desc.getDestinationType());
        appendTextChild(msgDestRefNode, TagNames.MESSAGE_DESTINATION_USAGE, desc.getUsage());
        appendTextChild(msgDestRefNode, TagNames.MESSAGE_DESTINATION_LINK, desc.getMessageDestinationLinkName());
        appendTextChild(msgDestRefNode, TagNames.MAPPED_NAME, desc.getMappedName());

        if (desc.isInjectable()) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : desc.getInjectionTargets()) {
                ijNode.writeDescriptor(msgDestRefNode, TagNames.INJECTION_TARGET, target);
            }
        }

        appendTextChild(msgDestRefNode, TagNames.LOOKUP_NAME, desc.getLookupName());
        return msgDestRefNode;
    }
}

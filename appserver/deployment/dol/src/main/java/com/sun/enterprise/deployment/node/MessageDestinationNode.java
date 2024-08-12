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

import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This class handles all information related to the message-destination xml tag
 *
 * @author Kenneth Saks
 */
public class MessageDestinationNode extends DisplayableComponentNode<MessageDestinationDescriptor> {

    @Override
    protected MessageDestinationDescriptor createDescriptor() {
        return new MessageDestinationDescriptor();
    }


    /**
     * All sub-implementation of this class can use a dispatch table to
     * map xml element to method name on the descriptor class for setting
     * the element value.
     *
     * @return map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.MESSAGE_DESTINATION_NAME, "setName");
        table.put(TagNames.MAPPED_NAME, "setMappedName");
        table.put(TagNames.LOOKUP_NAME, "setLookupName");
        return table;
    }


    /**
     * Write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, MessageDestinationDescriptor desc) {
        Node msgDestNode = super.writeDescriptor(parent, nodeName, desc);

        writeDisplayableComponentInfo(msgDestNode, desc);

        appendTextChild(msgDestNode, TagNames.MESSAGE_DESTINATION_NAME, desc.getName());
        appendTextChild(msgDestNode, TagNames.MAPPED_NAME, desc.getMappedName());
        appendTextChild(msgDestNode, TagNames.LOOKUP_NAME, desc.getLookupName());

        return msgDestNode;
    }
}

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

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * This class handles a name-value property
 *
 * @author Jerome Dochez
 */
public class NameValuePairNode extends DeploymentDescriptorNode {

   /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.NAME_VALUE_PAIR_NAME, "setName");
        table.put(TagNames.NAME_VALUE_PAIR_VALUE, "setValue");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, NameValuePairDescriptor descriptor) {
        Node envEntryNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(envEntryNode, TagNames.NAME_VALUE_PAIR_NAME, descriptor.getName());
        appendTextChild(envEntryNode, TagNames.NAME_VALUE_PAIR_VALUE, descriptor.getValue());
        return envEntryNode;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param iterator on the descriptors to write
     * @return the DOM tree top node
     */
    public void writeDescriptor(Node parent, String nodeName, Iterator props) {
        if (props == null) {
            return;
        }

        while (props.hasNext()) {
            NameValuePairDescriptor aProp = (NameValuePairDescriptor) props.next();
            writeDescriptor(parent, nodeName, aProp);
        }
    }
}

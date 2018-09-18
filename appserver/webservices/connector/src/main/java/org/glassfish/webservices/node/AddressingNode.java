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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import com.sun.enterprise.deployment.Addressing;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node does xml marshalling to/from web service addressing elements
 *
 * @author Bhakti Mehta
 */
public class AddressingNode extends DisplayableComponentNode {

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.ADDRESSING);


    public AddressingNode() {
        super();
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    @Override
    protected Addressing createDescriptor() {
       return new Addressing();
   }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.ADDRESSING_ENABLED, "setEnabled");
        table.put(WebServicesTagNames.ADDRESSING_REQUIRED, "setRequired");
        table.put(WebServicesTagNames.ADDRESSING_RESPONSES, "setResponses");

        return table;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        Addressing addressing = (Addressing) getDescriptor();
        if (WebServicesTagNames.ADDRESSING_ENABLED.equals(qname)) {
            addressing.setEnabled(Boolean.valueOf(value));
        } else if (WebServicesTagNames.ADDRESSING_REQUIRED.equals(qname)) {
            addressing.setRequired(Boolean.valueOf(value));
        } else if (WebServicesTagNames.ADDRESSING_RESPONSES.equals(qname)) {
            addressing.setResponses(value);
        } else super.setElementValue(element, value);
    }

    /**
     * write the method descriptor class to a query-method DOM tree and
     * return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param addressing the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                Addressing addressing) {
        Node wshNode = super.writeDescriptor(parent, nodeName, addressing);

        writeDisplayableComponentInfo(wshNode, addressing);
        appendTextChild(wshNode,
                WebServicesTagNames.ADDRESSING_ENABLED,
                Boolean.valueOf(addressing.isEnabled()).toString());
        appendTextChild(wshNode,
                WebServicesTagNames.ADDRESSING_REQUIRED,
                Boolean.valueOf(addressing.isRequired()).toString());
        appendTextChild(wshNode,
                WebServicesTagNames.ADDRESSING_RESPONSES,
                addressing.getResponses());




        return wshNode;
    }


}

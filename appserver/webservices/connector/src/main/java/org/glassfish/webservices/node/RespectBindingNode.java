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
import com.sun.enterprise.deployment.RespectBinding;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node does xml marshalling to/from web service respect-binding elements
 *
 * @author Bhakti Mehta
 */
public class RespectBindingNode extends DisplayableComponentNode {

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.RESPECT_BINDING);


    public RespectBindingNode() {
        super();
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    @Override
    protected RespectBinding createDescriptor() {
       return new RespectBinding();
    }


    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.RESPECT_BINDING_ENABLED, "setEnabled");

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
        RespectBinding rb = (RespectBinding) getDescriptor();
        if (WebServicesTagNames.RESPECT_BINDING_ENABLED.equals(qname)) {
            rb.setEnabled( Boolean.valueOf(value));
        } else super.setElementValue(element, value);
    }

    /**
     * write the method descriptor class to a query-method DOM tree and
     * return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                RespectBinding rb) {
        Node wshNode = super.writeDescriptor(parent, nodeName, rb);

        writeDisplayableComponentInfo(wshNode, rb);
        appendTextChild(wshNode,
                WebServicesTagNames.RESPECT_BINDING_ENABLED,
                Boolean.valueOf(rb.isEnabled()).toString());

        return wshNode;
    }


}

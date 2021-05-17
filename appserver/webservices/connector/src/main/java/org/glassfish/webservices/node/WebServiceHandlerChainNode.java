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

import com.sun.enterprise.deployment.WebServiceHandlerChain;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WebServiceHandlerChainNode extends DisplayableComponentNode {

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.HANDLER_CHAIN);

    public WebServiceHandlerChainNode() {
        super();
        registerElementHandler
            (new XMLElement(WebServicesTagNames.HANDLER),
             WebServiceHandlerNode.class, "addHandler");
    }

    @Override
    protected WebServiceHandlerChain createDescriptor() {
        return new WebServiceHandlerChain();
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.SERVICE_NAME_PATTERN, "setServiceNamePattern");
        table.put(WebServicesTagNames.PORT_NAME_PATTERN, "setPortNamePattern");
        table.put(WebServicesTagNames.PROTOCOL_BINDINGS, "setProtocolBindings");
        return table;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        super.setElementValue(element, value);
    }

    /**
     * write the method descriptor class to a query-method DOM tree and
     * return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param handler the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                WebServiceHandlerChain handler) {
        Node wshNode = super.writeDescriptor(parent, nodeName, handler);

        if(handler.getServiceNamePattern() != null) {
            appendTextChild(wshNode,
                        WebServicesTagNames.SERVICE_NAME_PATTERN,
                        handler.getServiceNamePattern());
        }
        if(handler.getPortNamePattern() != null) {
            appendTextChild(wshNode,
                        WebServicesTagNames.PORT_NAME_PATTERN,
                        handler.getPortNamePattern());
        }
        if(handler.getProtocolBindings() != null) {
            appendTextChild(wshNode,
                        WebServicesTagNames.PROTOCOL_BINDINGS,
                        handler.getProtocolBindings());
        }
        WebServiceHandlerNode handlerNode = new WebServiceHandlerNode();
        handlerNode.writeWebServiceHandlers(wshNode, handler.getHandlers());
        return wshNode;
    }

    public void writeWebServiceHandlerChains(Node parent, List handlerChain) {
        // If there are HanderChains, add the <handler-chains> node before adding
        // individual <handler-chain> nodes
        if(handlerChain.size() != 0) {
            parent = super.writeDescriptor(parent, WebServicesTagNames.HANDLER_CHAINS, null);
        }
        for(Iterator iter = handlerChain.iterator(); iter.hasNext();) {
            WebServiceHandlerChain next = (WebServiceHandlerChain) iter.next();
            writeDescriptor(parent, WebServicesTagNames.HANDLER_CHAIN, next);
        }
    }

}

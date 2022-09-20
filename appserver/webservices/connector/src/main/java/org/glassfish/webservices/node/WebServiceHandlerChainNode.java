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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_CHAIN;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_CHAINS;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PORT_NAME_PATTERN;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PROTOCOL_BINDINGS;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_NAME_PATTERN;

public class WebServiceHandlerChainNode extends DisplayableComponentNode<WebServiceHandlerChain> {

    private final static XMLElement TAG = new XMLElement(HANDLER_CHAIN);

    public WebServiceHandlerChainNode() {
        registerElementHandler(new XMLElement(HANDLER), WebServiceHandlerNode.class, "addHandler");
    }


    @Override
    protected WebServiceHandlerChain createDescriptor() {
        return new WebServiceHandlerChain();
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(SERVICE_NAME_PATTERN, "setServiceNamePattern");
        table.put(PORT_NAME_PATTERN, "setPortNamePattern");
        table.put(PROTOCOL_BINDINGS, "setProtocolBindings");
        return table;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        super.setElementValue(element, value);
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, WebServiceHandlerChain handler) {
        Node wshNode = super.writeDescriptor(parent, nodeName, handler);

        if (handler.getServiceNamePattern() != null) {
            appendTextChild(wshNode, SERVICE_NAME_PATTERN, handler.getServiceNamePattern());
        }
        if (handler.getPortNamePattern() != null) {
            appendTextChild(wshNode, PORT_NAME_PATTERN, handler.getPortNamePattern());
        }
        if (handler.getProtocolBindings() != null) {
            appendTextChild(wshNode, PROTOCOL_BINDINGS, handler.getProtocolBindings());
        }
        WebServiceHandlerNode handlerNode = new WebServiceHandlerNode();
        handlerNode.writeWebServiceHandlers(wshNode, handler.getHandlers());
        return wshNode;
    }


    public void writeWebServiceHandlerChains(Node parent, List<WebServiceHandlerChain> handlerChain) {
        // If there are HanderChains, add the <handler-chains> node before adding
        // individual <handler-chain> nodes
        if (!handlerChain.isEmpty()) {
            parent = super.writeDescriptor(parent, HANDLER_CHAINS, null);
        }
        for (WebServiceHandlerChain next : handlerChain) {
            writeDescriptor(parent, HANDLER_CHAIN, next);
        }
    }
}

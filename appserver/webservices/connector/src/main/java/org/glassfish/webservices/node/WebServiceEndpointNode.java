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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.glassfish.webservices.connector.LogUtils;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.WebServicesTagNames.ADDRESSING;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.EJB_LINK;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.ENABLE_MTOM;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_CHAIN;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.MTOM_THRESHOLD;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PORT_COMPONENT;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PORT_COMPONENT_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PROTOCOL_BINDING;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.RESPECT_BINDING;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_IMPL_BEAN;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVLET_LINK;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.WSDL_PORT;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.WSDL_SERVICE;

/**
 * This node handles the web service endpoint definition
 *
 * @author Jerome Dochez
 */
public class WebServiceEndpointNode extends DisplayableComponentNode<WebServiceEndpoint> {

    private static final Logger LOG = LogUtils.getLogger();
    private static final XMLElement TAG = new XMLElement(PORT_COMPONENT);

    /** Creates a new instance of WebServiceEndpointNode */
    public WebServiceEndpointNode() {
        registerElementHandler(new XMLElement(HANDLER), WebServiceHandlerNode.class, "addHandler");
        registerElementHandler(new XMLElement(ADDRESSING), AddressingNode.class, "setAddressing");
        registerElementHandler(new XMLElement(RESPECT_BINDING), RespectBindingNode.class, "setRespectBinding");
        registerElementHandler(new XMLElement(HANDLER_CHAIN), WebServiceHandlerChainNode.class, "addHandlerChain");
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    protected WebServiceEndpoint createDescriptor() {
        return new WebServiceEndpoint();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(PORT_COMPONENT_NAME, "setEndpointName");
        table.put(SERVICE_ENDPOINT_INTERFACE, "setServiceEndpointInterface");
        table.put(PROTOCOL_BINDING, "setProtocolBinding");
        table.put(ENABLE_MTOM, "setMtomEnabled");
        table.put(MTOM_THRESHOLD, "setMtomThreshold");
        return table;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        String elementName = element.getQName();
        WebServiceEndpoint endpoint = getDescriptor();
        if (EJB_LINK.equals(elementName)) {
            endpoint.setEjbLink(value);
        } else if (SERVLET_LINK.equals(elementName)) {
            endpoint.setWebComponentLink(value);
        } else if (WSDL_PORT.equals(elementName)) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);
            if (namespaceUri == null) {
                LOG.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE, new Object[] {prefix, value});
            } else {
                QName wsdlPort = new QName(namespaceUri, localPart);
                endpoint.setWsdlPort(wsdlPort, prefix);
            }
        } else if (WSDL_SERVICE.equals(elementName)) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);
            if (namespaceUri == null) {
                LOG.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE, new Object[] {prefix, value});
            } else {
                QName wsdlSvc = new QName(namespaceUri, localPart);
                endpoint.setWsdlService(wsdlSvc, prefix);
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, WebServiceEndpoint descriptor) {
        Node wseNode = super.writeDescriptor(parent, nodeName, descriptor);

        writeDisplayableComponentInfo(wseNode, descriptor);

        appendTextChild(wseNode, PORT_COMPONENT_NAME, descriptor.getEndpointName());

        QName wsdlService = descriptor.getWsdlService();
        if (wsdlService != null && !wsdlService.getLocalPart().isEmpty()) {
            appendQNameChild(WSDL_SERVICE, wseNode, wsdlService.getNamespaceURI(), wsdlService.getLocalPart(),
                descriptor.getWsdlServiceNamespacePrefix());
        }

        QName wsdlPort = descriptor.getWsdlPort();
        if (wsdlPort != null && !wsdlPort.getLocalPart().isEmpty()) {
            appendQNameChild(WSDL_PORT, wseNode, wsdlPort.getNamespaceURI(), wsdlPort.getLocalPart(),
                descriptor.getWsdlPortNamespacePrefix());
        }

        appendTextChild(wseNode, ENABLE_MTOM, descriptor.getMtomEnabled());
        appendTextChild(wseNode, MTOM_THRESHOLD, descriptor.getMtomThreshold());
        // TODO add addressing etc here
        if (descriptor.hasUserSpecifiedProtocolBinding()) {
            appendTextChild(wseNode, PROTOCOL_BINDING, descriptor.getProtocolBinding());
        }

        appendTextChild(wseNode, SERVICE_ENDPOINT_INTERFACE, descriptor.getServiceEndpointInterface());

        if (descriptor.implementedByWebComponent()) {
            Node linkNode = appendChild(wseNode, SERVICE_IMPL_BEAN);
            appendTextChild(linkNode, SERVLET_LINK, descriptor.getWebComponentLink());
        } else if (descriptor.implementedByEjbComponent()) {
            Node linkNode = appendChild(wseNode, SERVICE_IMPL_BEAN);
            appendTextChild(linkNode, EJB_LINK, descriptor.getEjbLink());
        } else {
            LOG.log(Level.INFO, LogUtils.WS_NOT_TIED_TO_COMPONENT, descriptor.getEndpointName());
        }

        WebServiceHandlerNode handlerNode = new WebServiceHandlerNode();
        handlerNode.writeWebServiceHandlers(wseNode, descriptor.getHandlers());

        WebServiceHandlerChainNode handlerChainNode = new WebServiceHandlerChainNode();
        handlerChainNode.writeWebServiceHandlerChains(wseNode, descriptor.getHandlerChain());
        return wseNode;
    }
}

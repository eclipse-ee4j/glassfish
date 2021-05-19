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

/*
 * WebServiceEndpointNode.java
 *
 * Created on March 21, 2002, 4:16 PM
 */

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.webservices.connector.LogUtils;

/**
 * This node handles the web service endpoint definition
 *
 * @author Jerome Dochez
 */
public class WebServiceEndpointNode extends DisplayableComponentNode {

    private static final Logger logger = LogUtils.getLogger();

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.PORT_COMPONENT);

    /** Creates a new instance of WebServiceEndpointNode */
    public WebServiceEndpointNode() {
        super();
        registerElementHandler
            (new XMLElement(WebServicesTagNames.HANDLER),
             WebServiceHandlerNode.class, "addHandler");
        registerElementHandler
            (new XMLElement(WebServicesTagNames.ADDRESSING),
             AddressingNode.class, "setAddressing");
        registerElementHandler
            (new XMLElement(WebServicesTagNames.RESPECT_BINDING),
             RespectBindingNode.class, "setRespectBinding");
        registerElementHandler
            (new XMLElement(WebServicesTagNames.HANDLER_CHAIN),
             WebServiceHandlerChainNode.class, "addHandlerChain");
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    @Override
    protected WebServiceEndpoint createDescriptor() {
        return new WebServiceEndpoint();
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.PORT_COMPONENT_NAME, "setEndpointName");
        table.put(WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE,
                  "setServiceEndpointInterface");
        table.put(WebServicesTagNames.PROTOCOL_BINDING, "setProtocolBinding");
        table.put(WebServicesTagNames.ENABLE_MTOM, "setMtomEnabled");
        table.put(WebServicesTagNames.MTOM_THRESHOLD, "setMtomThreshold");
        return table;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        String elementName = element.getQName();
        WebServiceEndpoint endpoint = (WebServiceEndpoint) getDescriptor();
        if (WebServicesTagNames.EJB_LINK.equals(elementName)) {
            endpoint.setEjbLink(value);
        } else if (WebServicesTagNames.SERVLET_LINK.equals(elementName)) {
            endpoint.setWebComponentLink(value);
        } else if (WebServicesTagNames.WSDL_PORT.equals(elementName)) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);
            if( namespaceUri == null) {
                logger.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE,
                    new Object[] {prefix , value });
            } else {
                QName wsdlPort = new QName(namespaceUri, localPart);
                endpoint.setWsdlPort(wsdlPort, prefix);
            }
        } else if(WebServicesTagNames.WSDL_SERVICE.equals(elementName)) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);
            if( namespaceUri == null) {
                logger.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE,
                    new Object[] {prefix , value });
            } else {
                QName wsdlSvc = new QName(namespaceUri, localPart);
                endpoint.setWsdlService(wsdlSvc, prefix);
            }
        } else super.setElementValue(element, value);
    }

    /**
     * write the method descriptor class to a query-method DOM tree
     * and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                WebServiceEndpoint descriptor) {
        Node wseNode = super.writeDescriptor(parent, nodeName, descriptor);

        writeDisplayableComponentInfo(wseNode, descriptor);

        appendTextChild(wseNode,
                        WebServicesTagNames.PORT_COMPONENT_NAME,
                        descriptor.getEndpointName());

        QName wsdlService = descriptor.getWsdlService();
        if((wsdlService!=null) &&
            (wsdlService.getLocalPart().length() != 0)) {
            appendQNameChild(WebServicesTagNames.WSDL_SERVICE, wseNode,
                         wsdlService.getNamespaceURI(), wsdlService.getLocalPart(),
                         descriptor.getWsdlServiceNamespacePrefix());
        }

        QName wsdlPort = descriptor.getWsdlPort();
        if((wsdlPort!=null) &&
            (wsdlPort.getLocalPart().length() != 0)) {
            appendQNameChild(WebServicesTagNames.WSDL_PORT, wseNode,
                         wsdlPort.getNamespaceURI(), wsdlPort.getLocalPart(),
                         descriptor.getWsdlPortNamespacePrefix());
        }

        appendTextChild(wseNode,
                        WebServicesTagNames.ENABLE_MTOM,
                        descriptor.getMtomEnabled());
        appendTextChild(wseNode,
                        WebServicesTagNames.MTOM_THRESHOLD,
                        descriptor.getMtomThreshold());
        //TODO add addressing etc here
        if(descriptor.hasUserSpecifiedProtocolBinding()) {
            appendTextChild(wseNode,
                        WebServicesTagNames.PROTOCOL_BINDING,
                        descriptor.getProtocolBinding());
        }

        appendTextChild(wseNode,
                        WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE,
                        descriptor.getServiceEndpointInterface());

        if( descriptor.implementedByWebComponent() ) {
            Node linkNode =
                appendChild(wseNode, WebServicesTagNames.SERVICE_IMPL_BEAN);
            appendTextChild(linkNode, WebServicesTagNames.SERVLET_LINK,
                            descriptor.getWebComponentLink());
        } else if( descriptor.implementedByEjbComponent() ) {
            Node linkNode =
                appendChild(wseNode, WebServicesTagNames.SERVICE_IMPL_BEAN);
            appendTextChild(linkNode, WebServicesTagNames.EJB_LINK,
                            descriptor.getEjbLink());
        } else {
            logger.log(Level.INFO, LogUtils.WS_NOT_TIED_TO_COMPONENT,
                    descriptor.getEndpointName());
        }

        WebServiceHandlerNode handlerNode = new WebServiceHandlerNode();
        handlerNode.writeWebServiceHandlers(wseNode,
                                            descriptor.getHandlers());

        WebServiceHandlerChainNode handlerChainNode = new WebServiceHandlerChainNode();
        handlerChainNode.writeWebServiceHandlerChains(wseNode, descriptor.getHandlerChain());
        return wseNode;
    }
}

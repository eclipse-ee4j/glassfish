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

package com.sun.enterprise.deployment.node.ws;

import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.ws.ReliabilityConfig;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.HTTP_RESPONSE_BUFFERSIZE;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.RELIABILITY_CONFIG;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.SERVICE_ENDPOINT_ADDRESS;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.STREAM_ATTACHMENTS;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.VALIDATE_REQUEST;
import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.WSDL;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.TRANSPORT_GUARANTEE;

/**
 * This node represents port-component in weblogic-webservices.xml
 *
 * @author Rama Pulavarthi
 */
public class WLWebServiceEndpointNode extends DeploymentDescriptorNode<WebServiceEndpoint> {

    private WebServiceEndpoint descriptor;

    private static final XMLElement TAG = new XMLElement(WebServicesTagNames.PORT_COMPONENT);

    private static final List<String> UNSUPPORTED_TAGS = new ArrayList<>();
    static {
        UNSUPPORTED_TAGS.add(WLWebServicesTagNames.DEPLOYMENT_LISTENER_LIST);
        UNSUPPORTED_TAGS.add(WLWebServicesTagNames.TRANSACTION_TIMEOUT);
        UNSUPPORTED_TAGS.add(WLWebServicesTagNames.CALLBACK_PROTOCOL);
        UNSUPPORTED_TAGS.add(WLWebServicesTagNames.HTTP_FLUSH_RESPONSE);
    }

    public WLWebServiceEndpointNode() {
        registerElementHandler(new XMLElement(WSDL), WSDLNode.class);
        registerElementHandler(new XMLElement(SERVICE_ENDPOINT_ADDRESS), ServiceEndpointAddressNode.class);
        registerElementHandler(new XMLElement(RELIABILITY_CONFIG), ReliabilityConfigNode.class);
        for (String unsupportedTag : UNSUPPORTED_TAGS) {
            registerElementHandler(new XMLElement(unsupportedTag), WLUnSupportedNode.class);
        }
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.AUTH_METHOD, "setAuthMethod");
        table.put(RuntimeTagNames.REALM, "setRealm");
        table.put(TRANSPORT_GUARANTEE, "setTransportGuarantee");
        table.put(STREAM_ATTACHMENTS, "setStreamAttachments");
        table.put(VALIDATE_REQUEST, "setValidateRequest");
        table.put(HTTP_RESPONSE_BUFFERSIZE, "setHttpResponseBufferSize");
        return table;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        String elementName = element.getQName();
        if (WebServicesTagNames.PORT_COMPONENT_NAME.equals(elementName)) {
            WebService webservice = (WebService) getParentNode().getDescriptor();
            descriptor = webservice.getEndpointByName(value);
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public DeploymentDescriptorNode<?> getHandlerFor(XMLElement element) {
        final String elementName = element.getQName();
        final DeploymentDescriptorNode<?> node;
        if (UNSUPPORTED_TAGS.contains(element.getQName())) {
            node = new WLUnSupportedNode(element);
        } else if (WSDL.equals(elementName)) {
            node = new WSDLNode(descriptor);
            node.setParentNode(this);
        } else if (SERVICE_ENDPOINT_ADDRESS.equals(elementName)) {
            node = new ServiceEndpointAddressNode(descriptor);
            node.setParentNode(this);
        } else if(RELIABILITY_CONFIG.equals(elementName)) {
            node = new ReliabilityConfigNode();
            node.setParentNode(this);
        } else {
            node = null;
        }
        return node;
    }


    @Override
    public WebServiceEndpoint getDescriptor() {
        return descriptor;
    }


    @Override
    public void addDescriptor(Object childdescriptor) {
        if (childdescriptor instanceof ReliabilityConfig) {
            descriptor.setReliabilityConfig(((ReliabilityConfig) childdescriptor));
        }
    }


    public Node writeDescriptor(Node parent, String nodeName, WebServiceEndpoint descriptor) {
        Node wseNode = super.writeDescriptor(parent, nodeName, descriptor);

        appendTextChild(wseNode, WebServicesTagNames.PORT_COMPONENT_NAME, descriptor.getEndpointName());

        // login config only makes sense for ejbs. For web components,
        // this info is described in web application itself.
        if (descriptor.implementedByEjbComponent() && descriptor.hasAuthMethod()) {
            Node loginConfigNode = appendChild(wseNode, RuntimeTagNames.LOGIN_CONFIG);
            appendTextChild(loginConfigNode, RuntimeTagNames.AUTH_METHOD, descriptor.getAuthMethod());
            appendTextChild(loginConfigNode, RuntimeTagNames.REALM, descriptor.getRealm());
        }

        if (descriptor.getTransportGuarantee() != null) {
            appendTextChild(wseNode, TRANSPORT_GUARANTEE, descriptor.getTransportGuarantee());
        }

        if (descriptor.getWsdlExposed() != null) {
            new WSDLNode(descriptor).writeDescriptor(wseNode, descriptor);
        }

        if (descriptor.getStreamAttachments() != null) {
            appendTextChild(wseNode, STREAM_ATTACHMENTS, descriptor.getStreamAttachments());
        }

        if (descriptor.getValidateRequest() != null) {
            appendTextChild(wseNode, VALIDATE_REQUEST, descriptor.getValidateRequest());
        }
        if (descriptor.getHttpResponseBufferSize() != null) {
            appendTextChild(wseNode, HTTP_RESPONSE_BUFFERSIZE, descriptor.getHttpResponseBufferSize());

        }
        if (descriptor.getReliabilityConfig() != null) {
            ReliabilityConfigNode rmConfigNode = new ReliabilityConfigNode();
            rmConfigNode.writeDescriptor(wseNode, descriptor.getReliabilityConfig());
        }

        return wseNode;
    }

    /**
     * This node represents
     * <wsdl>
     * <exposed/>
     * </wsdl>
     * <p/>
     * inside port-component
     */
    public static class WSDLNode extends DeploymentDescriptorNode<WebServiceEndpoint> {

        private final XMLElement tag = new XMLElement(WSDL);
        private final WebServiceEndpoint descriptor;

        public WSDLNode(WebServiceEndpoint descriptor) {
            this.descriptor = descriptor;
        }


        @Override
        protected XMLElement getXMLRootTag() {
            return tag;
        }


        @Override
        public WebServiceEndpoint getDescriptor() {
            return descriptor;
        }


        @Override
        protected Map<String, String> getDispatchTable() {
            Map<String, String> table = super.getDispatchTable();
            table.put(WLWebServicesTagNames.WSDL_EXPOSED, "setWsdlExposed");
            return table;
        }


        @Override
        public Node writeDescriptor(Node parent, WebServiceEndpoint descriptor) {
            if (descriptor.getWsdlExposed() != null) {
                Document doc = getOwnerDocument(parent);
                Element wsdl = doc.createElement(WSDL);
                Element exposed = doc.createElement(WLWebServicesTagNames.WSDL_EXPOSED);
                exposed.appendChild(doc.createTextNode(descriptor.getWsdlExposed()));
                wsdl.appendChild(exposed);
                parent.appendChild(wsdl);
                return wsdl;
            }
            return null;
        }
    }


    /**
     * This node represents
     * <service-endpoint-address>
     * <webservice-contextpath/>
     * <webservice-serviceuri/>
     * </service-endpoint-address>
     * <p/>
     * inside port-component
     */
    public static class ServiceEndpointAddressNode extends DeploymentDescriptorNode<WebServiceEndpoint> {

        private final XMLElement tag = new XMLElement(SERVICE_ENDPOINT_ADDRESS);
        private final WebServiceEndpoint descriptor;

        private String contextPath = "";

        public ServiceEndpointAddressNode(WebServiceEndpoint descriptor) {
            this.descriptor = descriptor;
        }


        @Override
        protected XMLElement getXMLRootTag() {
            return tag;
        }


        @Override
        public WebServiceEndpoint getDescriptor() {
            return descriptor;
        }


        @Override
        public void setElementValue(XMLElement element, String value) {
            String elementName = element.getQName();
            if (WLWebServicesTagNames.WEBSERVICE_CONTEXTPATH.equals(elementName)) {
                // contextPath is ignored for servlet endpoints as they get it from web.xml
                if (descriptor.implementedByEjbComponent()) {
                    contextPath = value;
                }
            } else if (WLWebServicesTagNames.WEBSERVICE_SERVICEURI.equals(elementName)) {
                String serviceuri = value;
                serviceuri = (serviceuri.startsWith("/") ? "" : "/") + serviceuri;
                descriptor.setEndpointAddressUri(contextPath + serviceuri);
            } else {
                super.setElementValue(element, value);
            }
        }


        @Override
        public Node writeDescriptor(Node parent, WebServiceEndpoint descriptor) {
            String ctxtPath;
            String serviceUri;
            String endpointAddressUri = descriptor.getEndpointAddressUri();
            if (descriptor.implementedByEjbComponent()) {
                ctxtPath = endpointAddressUri.substring(0, endpointAddressUri.lastIndexOf("/") - 1);
                serviceUri = endpointAddressUri.substring(endpointAddressUri.lastIndexOf("/"));
            } else {
                // for servlet endpoint, use web application context root
                ctxtPath = descriptor.getWebComponentImpl().getWebBundleDescriptor().getContextRoot();
                serviceUri = endpointAddressUri;
            }
            Document doc = getOwnerDocument(parent);
            Element serviceEndpointAddress = doc.createElement(SERVICE_ENDPOINT_ADDRESS);

            Element ctxtPathEl = doc.createElement(WLWebServicesTagNames.WEBSERVICE_CONTEXTPATH);
            ctxtPathEl.appendChild(doc.createTextNode(ctxtPath));
            serviceEndpointAddress.appendChild(ctxtPathEl);

            Element serviceuriEl = doc.createElement(WLWebServicesTagNames.WEBSERVICE_SERVICEURI);
            serviceuriEl.appendChild(doc.createTextNode(serviceUri));
            serviceEndpointAddress.appendChild(serviceuriEl);

            parent.appendChild(serviceEndpointAddress);
            return serviceEndpointAddress;
        }
    }
}

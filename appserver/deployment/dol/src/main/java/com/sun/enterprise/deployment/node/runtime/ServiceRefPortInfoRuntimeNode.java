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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.NameValuePairNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.common.MessageSecurityBindingNode;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling runtime info for
 * a service reference wsdl port.
 *
 * @author  Kenneth Saks
 * @version
 */
public class ServiceRefPortInfoRuntimeNode extends DeploymentDescriptorNode {

    private String namespaceUri;

    public ServiceRefPortInfoRuntimeNode() {
        super();
        registerElementHandler
            (new XMLElement(WebServicesTagNames.STUB_PROPERTY),
             NameValuePairNode.class, "addStubProperty");
        registerElementHandler
            (new XMLElement(WebServicesTagNames.CALL_PROPERTY),
             NameValuePairNode.class, "addCallProperty");
        registerElementHandler(new XMLElement(WebServicesTagNames.MESSAGE_SECURITY_BINDING), MessageSecurityBindingNode.class, "setMessageSecurityBinding");
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE,
                  "setServiceEndpointInterface");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */

    public void setElementValue(XMLElement element, String value) {
        String name = element.getQName();
        if (WebServicesTagNames.NAMESPACE_URI.equals(name)) {
            namespaceUri = value;
        } else if (WebServicesTagNames.LOCAL_PART.equals(name)) {
            ServiceRefPortInfo desc = (ServiceRefPortInfo)
                getDescriptor();
            QName wsdlPort = new QName(namespaceUri, value);
            desc.setWsdlPort(wsdlPort);
            namespaceUri = null;
        } else super.setElementValue(element, value);

    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                ServiceRefPortInfo desc) {
        Node serviceRefPortInfoRuntimeNode =
            super.writeDescriptor(parent, nodeName, desc);

        appendTextChild(serviceRefPortInfoRuntimeNode,
                        WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE,
                        desc.getServiceEndpointInterface());

        QName port = desc.getWsdlPort();

        if( port != null ) {
            Node wsdlPortNode = appendChild(serviceRefPortInfoRuntimeNode,
                                            WebServicesTagNames.WSDL_PORT);
            appendTextChild(wsdlPortNode,
                            WebServicesTagNames.NAMESPACE_URI,
                            port.getNamespaceURI());
            appendTextChild(wsdlPortNode,
                            WebServicesTagNames.LOCAL_PART,
                            port.getLocalPart());
        }

        // stub-property*

        NameValuePairNode nameValueNode = new NameValuePairNode();

        Set stubProperties = desc.getStubProperties();
        for(Iterator iter = stubProperties.iterator(); iter.hasNext();) {
            NameValuePairDescriptor next = (NameValuePairDescriptor)iter.next();
            nameValueNode.writeDescriptor
                (serviceRefPortInfoRuntimeNode,
                 WebServicesTagNames.STUB_PROPERTY, next);
        }

        // call-property*
        for(Iterator iter = desc.getCallProperties().iterator();
            iter.hasNext();) {
            NameValuePairDescriptor next = (NameValuePairDescriptor)iter.next();
            nameValueNode.writeDescriptor
                (serviceRefPortInfoRuntimeNode,
                 WebServicesTagNames.CALL_PROPERTY, next);
        }

        // message-security-binding
        MessageSecurityBindingDescriptor messageSecBindingDesc =
            desc.getMessageSecurityBinding();
        if (messageSecBindingDesc != null) {
            MessageSecurityBindingNode messageSecBindingNode =
                new MessageSecurityBindingNode();
            messageSecBindingNode.writeDescriptor(serviceRefPortInfoRuntimeNode, WebServicesTagNames.MESSAGE_SECURITY_BINDING, messageSecBindingDesc);
        }

        return serviceRefPortInfoRuntimeNode;
    }

}

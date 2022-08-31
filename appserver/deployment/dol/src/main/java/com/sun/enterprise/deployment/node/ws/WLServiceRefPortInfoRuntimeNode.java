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

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.NameValuePairNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.node.ws.WLWebServicesTagNames.SERVICE_REFERENCE_PORT_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.CALL_PROPERTY;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.STUB_PROPERTY;

/**
 * This node is responsible for handling runtime info for a service reference wsdl port from
 * weblogic DD.
 *
 * @author Rama Pulavarthi
 */
public class WLServiceRefPortInfoRuntimeNode extends DeploymentDescriptorNode<ServiceRefPortInfo> {

    private ServiceRefPortInfo descriptor;

    public WLServiceRefPortInfoRuntimeNode() {
        registerElementHandler(new XMLElement(STUB_PROPERTY), NameValuePairNode.class, "addStubProperty");
        registerElementHandler(new XMLElement(CALL_PROPERTY), NameValuePairNode.class, "addCallProperty");
    }


    @Override
    public ServiceRefPortInfo getDescriptor() {
        return descriptor;
    }


    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        String name = element.getQName();
        if (SERVICE_REFERENCE_PORT_NAME.equals(name)) {
            ServiceReferenceDescriptor serviceRef = ((ServiceReferenceDescriptor) getParentNode().getDescriptor());
            // WLS-DD does not provide a way to specify ns uri of the port, so use the service ns uri
            String namespaceUri = serviceRef.getServiceNamespaceUri();
            QName wsdlPort = new QName(namespaceUri, value);
            descriptor = serviceRef.getPortInfoByPort(wsdlPort);
        } else {
            super.setElementValue(element, value);
        }

    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param desc the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, ServiceRefPortInfo desc) {
        Node serviceRefPortInfoRuntimeNode = super.writeDescriptor(parent, nodeName, desc);
        QName port = desc.getWsdlPort();
        if (port != null) {
            appendTextChild(serviceRefPortInfoRuntimeNode, SERVICE_REFERENCE_PORT_NAME, port.getLocalPart());

            // stub-property*
            NameValuePairNode nameValueNode = new NameValuePairNode();

            Set<NameValuePairDescriptor> stubProperties = desc.getStubProperties();
            for (NameValuePairDescriptor element : stubProperties) {
                nameValueNode.writeDescriptor(serviceRefPortInfoRuntimeNode, STUB_PROPERTY, element);
            }

            // call-property*
            for (NameValuePairDescriptor element : desc.getCallProperties()) {
                nameValueNode.writeDescriptor(serviceRefPortInfoRuntimeNode, CALL_PROPERTY, element);
            }

        }
        return serviceRefPortInfoRuntimeNode;
    }
}

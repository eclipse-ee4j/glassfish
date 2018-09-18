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

package com.sun.enterprise.deployment.node.ws;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.NameValuePairNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.common.MessageSecurityBindingNode;
import com.sun.enterprise.deployment.runtime.common.MessageSecurityBindingDescriptor;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This node is responsible for handling runtime info for a service reference wsdl port from weblogic DD.
 *
 * @author Rama Pulavarthi
 */
public class WLServiceRefPortInfoRuntimeNode extends DeploymentDescriptorNode {
    ServiceRefPortInfo descriptor = null;

    public WLServiceRefPortInfoRuntimeNode() {
        super();
        registerElementHandler
                (new XMLElement(WebServicesTagNames.STUB_PROPERTY),
                        NameValuePairNode.class, "addStubProperty");
        registerElementHandler
                (new XMLElement(WebServicesTagNames.CALL_PROPERTY),
                        NameValuePairNode.class, "addCallProperty");
    }

    @Override
    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value   it's associated value
     */

    public void setElementValue(XMLElement element, String value) {
        String name = element.getQName();
        if (WLWebServicesTagNames.SERVICE_REFERENCE_PORT_NAME.equals(name)) {
            ServiceReferenceDescriptor serviceRef = ((ServiceReferenceDescriptor) getParentNode().getDescriptor());
            //WLS-DD does not provide a way to specify ns uri of the port, so use the service ns uri
            String namespaceUri = serviceRef.getServiceNamespaceUri();
            QName wsdlPort = new QName(namespaceUri, value);
            descriptor = serviceRef.getPortInfoByPort(wsdlPort);
        } else super.setElementValue(element, value);

    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent   node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param desc     the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                ServiceRefPortInfo desc) {
        Node serviceRefPortInfoRuntimeNode =
                super.writeDescriptor(parent, nodeName, desc);

        QName port = desc.getWsdlPort();

        if (port != null) {
            appendTextChild(serviceRefPortInfoRuntimeNode,
                    WLWebServicesTagNames.SERVICE_REFERENCE_PORT_NAME,
                    port.getLocalPart());


            // stub-property*
            NameValuePairNode nameValueNode = new NameValuePairNode();

            Set stubProperties = desc.getStubProperties();
            for (Iterator iter = stubProperties.iterator(); iter.hasNext();) {
                NameValuePairDescriptor next = (NameValuePairDescriptor) iter.next();
                nameValueNode.writeDescriptor
                        (serviceRefPortInfoRuntimeNode,
                                WebServicesTagNames.STUB_PROPERTY, next);
            }

            // call-property*
            for (Iterator iter = desc.getCallProperties().iterator();
                 iter.hasNext();) {
                NameValuePairDescriptor next = (NameValuePairDescriptor) iter.next();
                nameValueNode.writeDescriptor
                        (serviceRefPortInfoRuntimeNode,
                                WebServicesTagNames.CALL_PROPERTY, next);
            }

        }
        return serviceRefPortInfoRuntimeNode;
    }

}

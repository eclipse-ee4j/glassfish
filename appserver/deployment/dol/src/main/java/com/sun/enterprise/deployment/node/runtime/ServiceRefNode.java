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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.NameValuePairNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This node is responsible for handling runtime info for
 * service references
 *
 * @author  Kenneth Saks
 * @version
 */
public class ServiceRefNode extends DeploymentDescriptorNode {

    private ServiceReferenceDescriptor descriptor;

    public ServiceRefNode() {
        super();
        registerElementHandler
            (new XMLElement(WebServicesTagNames.PORT_INFO),
             ServiceRefPortInfoRuntimeNode.class);
        registerElementHandler
            (new XMLElement(WebServicesTagNames.CALL_PROPERTY),
             NameValuePairNode.class, "addCallProperty");
    }

    public void addDescriptor(Object desc) {
        if( desc instanceof ServiceRefPortInfo ) {
            ServiceRefPortInfo newPortInfo = (ServiceRefPortInfo) desc;
            ServiceReferenceDescriptor serviceRef =
                (ServiceReferenceDescriptor) getDescriptor();
            serviceRef.addRuntimePortInfo(newPortInfo);
        }
    }

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.SERVICE_IMPL_CLASS,
                  "setServiceImplClassName");
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
        if (WebServicesTagNames.SERVICE_REF_NAME.equals(name)) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof JndiNameEnvironment) {
                descriptor = ((JndiNameEnvironment) parentDesc).
                    getServiceReferenceByName(value);
            } else if (parentDesc instanceof WebBundleDescriptor) {
                WebBundleDescriptor desc = (WebBundleDescriptor)parentDesc;
                descriptor = desc.getServiceReferenceByName(value);
            }
        } else if (WebServicesTagNames.NAMESPACE_URI.equals(name)) {
            // Any service-qname set in standard DD takes precedence
            if( descriptor.getServiceNamespaceUri() == null ) {
                descriptor.setServiceNamespaceUri(value);
            }
        } else if (WebServicesTagNames.LOCAL_PART.equals(name)) {
            // Any service-qname set in standard DD takes precedence
            if( descriptor.getServiceLocalPart() == null ) {
                descriptor.setServiceLocalPart(value);
            }
        } else if (WebServicesTagNames.WSDL_OVERRIDE.equals(name)) {
            try {
                URL url = new URL(value);
                descriptor.setWsdlOverride(url);
            } catch(MalformedURLException mue) {
                DOLUtils.getDefaultLogger().log(Level.INFO,
                  "Warning : Invalid wsdl override url=" + value, mue);
            }
        } else {
            super.setElementValue(element, value);
        }

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
                                ServiceReferenceDescriptor serviceRef) {
        Node serviceRefNode =
            super.writeDescriptor(parent, nodeName, serviceRef);

        appendTextChild(serviceRefNode, WebServicesTagNames.SERVICE_REF_NAME,
                        serviceRef.getName());

        ServiceRefPortInfoRuntimeNode portInfoRuntimeNode =
            new ServiceRefPortInfoRuntimeNode();

        Set portsInfo = serviceRef.getPortsInfo();
        for(Iterator iter = portsInfo.iterator(); iter.hasNext();) {
            ServiceRefPortInfo next = (ServiceRefPortInfo) iter.next();
            portInfoRuntimeNode.writeDescriptor
                (serviceRefNode, WebServicesTagNames.PORT_INFO, next);
        }

        NameValuePairNode nameValueNode = new NameValuePairNode();
        for(Iterator iter = serviceRef.getCallProperties().iterator();
            iter.hasNext();) {
            NameValuePairDescriptor next = (NameValuePairDescriptor)iter.next();
            nameValueNode.writeDescriptor
                (serviceRefNode, WebServicesTagNames.CALL_PROPERTY, next);
        }

        if( serviceRef.hasWsdlOverride() ) {
            URL wsdlOverride = serviceRef.getWsdlOverride();
            appendTextChild(serviceRefNode, WebServicesTagNames.WSDL_OVERRIDE,
                            wsdlOverride.toExternalForm());
        }

        appendTextChild(serviceRefNode, WebServicesTagNames.SERVICE_IMPL_CLASS,
                        serviceRef.getServiceImplClassName());

        if( serviceRef.hasServiceName() ) {
            Node serviceNameNode = appendChild
                (serviceRefNode, WebServicesTagNames.SERVICE_QNAME);
            QName serviceName = serviceRef.getServiceName();
            appendTextChild(serviceNameNode,
                            WebServicesTagNames.NAMESPACE_URI,
                            serviceName.getNamespaceURI());
            appendTextChild(serviceNameNode,
                            WebServicesTagNames.LOCAL_PART,
                            serviceName.getLocalPart());
        }

        return serviceRefNode;
    }

    /**
     * writes all the runtime information for service references
     *
     * @param parent node to add the runtime xml info
     * @param the J2EE component containing service references
     */
    public static void writeServiceReferences(Node parent,
                                              JndiNameEnvironment descriptor) {
        Iterator serviceRefs =
            descriptor.getServiceReferenceDescriptors().iterator();
        if (serviceRefs.hasNext()) {
            ServiceRefNode serviceRefNode = new ServiceRefNode();
            while (serviceRefs.hasNext()) {
                ServiceReferenceDescriptor next =
                    (ServiceReferenceDescriptor) serviceRefs.next();
                // Only write runtime service-ref entry if there IS
                // some runtime info...
                if( next.hasServiceImplClassName() ||
                    !next.getPortsInfo().isEmpty() ||
                    !next.getCallProperties().isEmpty() ||
                    next.hasWsdlOverride()) {
                    serviceRefNode.writeDescriptor
                        (parent, WebServicesTagNames.SERVICE_REF, next);
                }
            }
        }
    }

}

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

import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.InjectionTargetNode;
import com.sun.enterprise.deployment.node.JndiEnvRefNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.webservices.connector.LogUtils;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.TagNames.INJECTION_TARGET;
import static com.sun.enterprise.deployment.xml.TagNames.LOOKUP_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.ADDRESSING;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.ENABLE_MTOM;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.HANDLER_CHAIN;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.JAXRPC_MAPPING_FILE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.MAPPED_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PORT_COMPONENT_LINK;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.PORT_COMPONENT_REF;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.RESPECT_BINDING;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_INTERFACE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_QNAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_REF;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_REF_NAME;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.SERVICE_REF_TYPE;
import static com.sun.enterprise.deployment.xml.WebServicesTagNames.WSDL_FILE;

/**
 * This node is responsible for loading web services
 * reference information
 *
 * @author  Kenneth Saks
 */
@Service(name="service-ref")
@PerLookup
public class ServiceReferenceNode extends DisplayableComponentNode<ServiceReferenceDescriptor>
    implements JndiEnvRefNode<ServiceReferenceDescriptor> {

    private static final Logger logger = LogUtils.getLogger();

    private ServiceRefPortInfo  portInfo;

    /** Creates a new instance of ServiceReferenceNode */
    public ServiceReferenceNode() {
        super();
        registerElementHandler(new XMLElement(HANDLER), WebServiceHandlerNode.class, "addHandler");
        registerElementHandler(new XMLElement(HANDLER_CHAIN), WebServiceHandlerChainNode.class, "addHandlerChain");
        registerElementHandler(new XMLElement(ADDRESSING), AddressingNode.class, "setAddressing");
        registerElementHandler(new XMLElement(RESPECT_BINDING), RespectBindingNode.class, "setRespectBinding");
        registerElementHandler(new XMLElement(INJECTION_TARGET), InjectionTargetNode.class, "addInjectionTarget");
    }


    /**
     * all sub-implementation of this class can use a dispatch table
     * to map xml element to method name on the descriptor class for
     * setting the element value.
     *
     * @return map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(SERVICE_REF_NAME, "setName");
        table.put(SERVICE_INTERFACE, "setServiceInterface");
        table.put(WSDL_FILE, "setWsdlFileUri");
        table.put(JAXRPC_MAPPING_FILE, "setMappingFileUri");
        table.put(MAPPED_NAME, "setMappedName");
        table.put(LOOKUP_NAME, "setLookupName");
        table.put(SERVICE_REF_TYPE, "setInjectionTargetType");
        return table;
    }


    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        String qname = element.getQName();
        if (SERVICE_ENDPOINT_INTERFACE.equals(qname)) {
            portInfo = getDescriptor().getPortInfoBySEI(value);
            if (portInfo == null) {
                portInfo = getDescriptor().addContainerManagedPort(value);
            }
        } else if (SERVICE_QNAME.equals(qname)) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);
            if (namespaceUri == null) {
                logger.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE,
                    new Object[] {prefix, getDescriptor().getName()});
            } else {
                QName serviceName = new QName(namespaceUri, localPart);
                getDescriptor().setServiceName(serviceName, prefix);
            }
        } else if (ENABLE_MTOM.equals(qname)) {
            portInfo.setMtomEnabled(value);
            // } //TODO implement this else if(ADDRESSING.equals(qname)) {
            // portInfo.setAddressing(value);
        } else if (PORT_COMPONENT_LINK.equals(qname)) {
            // set link name. link resolution will be performed during
            // validation stage.
            portInfo.setPortComponentLinkName(value);
            portInfo = null;
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDeploymentDescriptor(Node parent, ServiceReferenceDescriptor descriptor) {
        Node serviceRefNode = super.writeDescriptor(parent, SERVICE_REF, descriptor);
        writeDisplayableComponentInfo(serviceRefNode, descriptor);
        appendTextChild(serviceRefNode, SERVICE_REF_NAME, descriptor.getName());
        appendTextChild(serviceRefNode, SERVICE_INTERFACE, descriptor.getServiceInterface());
        appendTextChild(serviceRefNode, LOOKUP_NAME, descriptor.getLookupName());
        appendTextChild(serviceRefNode, SERVICE_REF_TYPE, descriptor.getInjectionTargetType());
        appendTextChild(serviceRefNode, WSDL_FILE, descriptor.getWsdlFileUri());
        appendTextChild(serviceRefNode, JAXRPC_MAPPING_FILE, descriptor.getMappingFileUri());

        if (descriptor.hasServiceName()) {
            QName serviceName = descriptor.getServiceName();
            appendQNameChild(SERVICE_QNAME, serviceRefNode, serviceName.getNamespaceURI(), serviceName.getLocalPart(),
                descriptor.getServiceNameNamespacePrefix());
        }

        for (ServiceRefPortInfo element : descriptor.getPortsInfo()) {
            String sei = element.getServiceEndpointInterface();
            Node portComponentRefNode = appendChild(serviceRefNode, PORT_COMPONENT_REF);
            appendTextChild(portComponentRefNode, SERVICE_ENDPOINT_INTERFACE, sei);
            appendTextChild(portComponentRefNode, ENABLE_MTOM, element.getMtomEnabled());
            if (descriptor.getAddressing() != null) {
                AddressingNode adNode = new AddressingNode();
                adNode.writeDescriptor(portComponentRefNode, ADDRESSING, descriptor.getAddressing());
            }
            appendTextChild(portComponentRefNode, PORT_COMPONENT_LINK, element.getPortComponentLinkName());
        }

        WebServiceHandlerNode handlerNode = new WebServiceHandlerNode();
        handlerNode.writeWebServiceHandlers(serviceRefNode, descriptor.getHandlers());

        WebServiceHandlerChainNode handlerChainNode = new WebServiceHandlerChainNode();
        handlerChainNode.writeWebServiceHandlerChains(serviceRefNode, descriptor.getHandlerChain());

        appendTextChild(serviceRefNode, MAPPED_NAME, descriptor.getMappedName());

        if (descriptor.isInjectable()) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : descriptor.getInjectionTargets()) {
                ijNode.writeDescriptor(serviceRefNode, INJECTION_TARGET, target);
            }
        }

        return serviceRefNode;

    }


    @Override
    public ServiceReferenceDescriptor getDescriptor() {
        return super.getDescriptor();

    }


    @Override
    public String getTagName() {
        return SERVICE_REF;
    }
}

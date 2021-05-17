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
 * ServiceReferenceNode.java
 *
 * Created on March 21, 2002, 2:38 PM
 */

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.ServiceRefPortInfo;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.JndiEnvRefNode;
import com.sun.enterprise.deployment.node.InjectionTargetNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;


import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.webservices.connector.LogUtils;

/**
 * This node is responsible for loading web services
 * reference information
 *
 * @author  Kenneth Saks
 */

@Service(name="service-ref")
@PerLookup
public class ServiceReferenceNode extends DisplayableComponentNode implements JndiEnvRefNode<ServiceReferenceDescriptor> {

    private static final Logger logger = LogUtils.getLogger();

    private ServiceRefPortInfo  portInfo = null;

    /** Creates a new instance of ServiceReferenceNode */
    public ServiceReferenceNode() {
        super();
        registerElementHandler
                (new XMLElement(WebServicesTagNames.HANDLER),
                        WebServiceHandlerNode.class, "addHandler");
        registerElementHandler
                (new XMLElement(WebServicesTagNames.HANDLER_CHAIN),
                        WebServiceHandlerChainNode.class, "addHandlerChain");
        registerElementHandler
                (new XMLElement(WebServicesTagNames.ADDRESSING),
                        AddressingNode.class, "setAddressing");
        registerElementHandler
                (new XMLElement(WebServicesTagNames.RESPECT_BINDING),
                        RespectBindingNode.class, "setRespectBinding");
        registerElementHandler
                (new XMLElement(TagNames.INJECTION_TARGET),
                        InjectionTargetNode.class, "addInjectionTarget");
    }

    /**
     * all sub-implementation of this class can use a dispatch table
     * to map xml element to method name on the descriptor class for
     * setting the element value.
     *
     * @return map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.SERVICE_REF_NAME, "setName");
        table.put(WebServicesTagNames.SERVICE_INTERFACE, "setServiceInterface");
        table.put(WebServicesTagNames.WSDL_FILE, "setWsdlFileUri");
        table.put(WebServicesTagNames.JAXRPC_MAPPING_FILE, "setMappingFileUri");
        table.put(WebServicesTagNames.MAPPED_NAME, "setMappedName");
        table.put(TagNames.LOOKUP_NAME, "setLookupName");
        table.put(WebServicesTagNames.SERVICE_REF_TYPE, "setInjectionTargetType");
        return table;
    }

    private ServiceReferenceDescriptor getServiceReferenceDescriptor() {
        return (ServiceReferenceDescriptor) getDescriptor();
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
        if (WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE.equals(qname)) {
            portInfo = getServiceReferenceDescriptor().getPortInfoBySEI(value);
            if( portInfo == null  ) {
                portInfo = getServiceReferenceDescriptor().
                    addContainerManagedPort(value);
            }
        } else if( WebServicesTagNames.SERVICE_QNAME.equals(qname) ) {
            String prefix = getPrefixFromQName(value);
            String localPart = getLocalPartFromQName(value);
            String namespaceUri = resolvePrefix(element, prefix);
            if( namespaceUri == null) {
                logger.log(Level.SEVERE, LogUtils.INVALID_DESC_MAPPING_FAILURE,
                    new Object[] { prefix , getServiceReferenceDescriptor().getName()});
            } else {
                QName serviceName = new QName(namespaceUri, localPart);
                getServiceReferenceDescriptor().setServiceName
                    (serviceName, prefix);
            }
        } else if(WebServicesTagNames.ENABLE_MTOM.equals(qname)) {
            portInfo.setMtomEnabled(value);
        //} //TODO implement this   else if(WebServicesTagNames.ADDRESSING.equals(qname)) {
          //  portInfo.setAddressing(value);
        } else if( WebServicesTagNames.PORT_COMPONENT_LINK.equals(qname) ) {
            // set link name.  link resolution will be performed during
            // validation stage.
            portInfo.setPortComponentLinkName(value);
            portInfo = null;
        } else {
            super.setElementValue(element, value);
        }
    }

    public Node writeDeploymentDescriptor( Node parent,ServiceReferenceDescriptor descriptor) {

        Node serviceRefNode =
                super.writeDescriptor(parent, WebServicesTagNames.SERVICE_REF, descriptor);
        writeDisplayableComponentInfo(serviceRefNode, descriptor);
        appendTextChild(serviceRefNode, WebServicesTagNames.SERVICE_REF_NAME,
                descriptor.getName());
        appendTextChild(serviceRefNode, WebServicesTagNames.SERVICE_INTERFACE,
                descriptor.getServiceInterface());
        appendTextChild(serviceRefNode, TagNames.LOOKUP_NAME,
                descriptor.getLookupName());
        appendTextChild(serviceRefNode, WebServicesTagNames.SERVICE_REF_TYPE,
                descriptor.getInjectionTargetType());
        appendTextChild(serviceRefNode, WebServicesTagNames.WSDL_FILE,
                descriptor.getWsdlFileUri());
        appendTextChild(serviceRefNode, WebServicesTagNames.JAXRPC_MAPPING_FILE,
                descriptor.getMappingFileUri());

        if( descriptor.hasServiceName() ) {
            QName serviceName = descriptor.getServiceName();
            appendQNameChild(WebServicesTagNames.SERVICE_QNAME, serviceRefNode,
                    serviceName.getNamespaceURI(),
                    serviceName.getLocalPart(),
                    descriptor.getServiceNameNamespacePrefix());
        }


        for(Iterator iter = descriptor.getPortsInfo().iterator();
            iter.hasNext();) {
            ServiceRefPortInfo next = (ServiceRefPortInfo) iter.next();
            String sei = next.getServiceEndpointInterface();
            Node portComponentRefNode = appendChild(
                    serviceRefNode, WebServicesTagNames.PORT_COMPONENT_REF);
            appendTextChild(portComponentRefNode,
                    WebServicesTagNames.SERVICE_ENDPOINT_INTERFACE,
                    sei);
            appendTextChild(portComponentRefNode,
                    WebServicesTagNames.ENABLE_MTOM,
                    next.getMtomEnabled());
            if (descriptor.getAddressing() != null) {
                AddressingNode adNode = new AddressingNode();
                adNode.writeDescriptor(portComponentRefNode,
                        WebServicesTagNames.ADDRESSING,
                        descriptor.getAddressing());
            }
            appendTextChild(portComponentRefNode,
                    WebServicesTagNames.PORT_COMPONENT_LINK,
                    next.getPortComponentLinkName());
        }

        WebServiceHandlerNode handlerNode = new WebServiceHandlerNode();
        handlerNode.writeWebServiceHandlers(serviceRefNode,
                descriptor.getHandlers());

        WebServiceHandlerChainNode handlerChainNode =
                new WebServiceHandlerChainNode();
        handlerChainNode.writeWebServiceHandlerChains(serviceRefNode,
                descriptor.getHandlerChain());

        appendTextChild(serviceRefNode, WebServicesTagNames.MAPPED_NAME,
                descriptor.getMappedName());

        if (descriptor.isInjectable()) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : descriptor.getInjectionTargets()) {
                ijNode.writeDescriptor(serviceRefNode, TagNames.INJECTION_TARGET, target);
            }
        }


        return serviceRefNode;

    }

    public ServiceReferenceDescriptor getDescriptor(){
        return (ServiceReferenceDescriptor) super.getDescriptor();

    }

    public String getTagName() {
        return WebServicesTagNames.SERVICE_REF;
    }





}

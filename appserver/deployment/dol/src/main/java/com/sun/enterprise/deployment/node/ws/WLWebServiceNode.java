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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.*;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

/**
 * This node represents webservice-description node in weblogic-webservices.xml
 *
 * @author Rama Pulavarthi
 */
public class WLWebServiceNode extends DisplayableComponentNode {

    private WebService descriptor = null;
    private String serviceDescriptionName;
    private final static XMLElement tag =
            new XMLElement(WLWebServicesTagNames.WEB_SERVICE);

    public WLWebServiceNode() {
        registerElementHandler(new XMLElement(WLWebServicesTagNames.PORT_COMPONENT),
                WLWebServiceEndpointNode.class);

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
        //table.put(WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME,"setName");
        table.put(WLWebServicesTagNames.WEBSERVICE_TYPE, "setType");
        return table;
    }


    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value   it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (WLWebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME.equals
                (element.getQName())) {
            WebServicesDescriptor webServices = (WebServicesDescriptor) getParentNode().getDescriptor();
            descriptor = webServices.getWebServiceByName(value);
            serviceDescriptionName = value;
        } else {
            if (descriptor == null) {
                    DOLUtils.getDefaultLogger().severe
                            ("Warning : WebService descriptor cannot be found webservice-description-name "
                                    + serviceDescriptionName);
                    throw new RuntimeException("DeploymentException: WebService descriptor cannot be found for webservice-description-name:" +
                            serviceDescriptionName +" specified in weblogic-webservices.xml");
            }

            if (WLWebServicesTagNames.WSDL_PUBLISH_FILE.equals
                    (element.getQName())) {
                URL url = null;
                try {
                    url = new URL(value);
                } catch (MalformedURLException e) {
                    try {
                        //try file
                        url = new File(value).toURI().toURL();
                    } catch (MalformedURLException mue) {
                        DOLUtils.getDefaultLogger().log(Level.INFO,
                                "Warning : Invalid final wsdl url=" + value, mue);
                    }
                }
                if (url != null) {
                    descriptor.setClientPublishUrl(url);
                }
            } else {
                super.setElementValue(element, value);
            }
        }
    }

    @Override
    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * Adds  a new DOL descriptor instance to the descriptor
     * instance associated with this XMLNode
     *
     * @param descriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object descriptor) {
        WebServiceEndpoint endpoint = (WebServiceEndpoint) descriptor;
        WebService webService = (WebService) getDescriptor();
        webService.addEndpoint(endpoint);
    }

    public Node writeDescriptor(Node parent, String nodeName,
                                WebService descriptor) {
        Node topNode =
                super.writeDescriptor(parent, nodeName, descriptor);

        //TODO is this needed?
        //writeDisplayableComponentInfo(topNode, descriptor);

        appendTextChild(topNode,
                WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME,
                descriptor.getName());
        appendTextChild(topNode, WLWebServicesTagNames.WEBSERVICE_TYPE,
                descriptor.getType());
        if (descriptor.getClientPublishUrl() != null) {
            appendTextChild(topNode, WLWebServicesTagNames.WSDL_PUBLISH_FILE,
                descriptor.getClientPublishUrl().toString());
        }

        WLWebServiceEndpointNode endpointNode = new WLWebServiceEndpointNode();
        for (WebServiceEndpoint next : descriptor.getEndpoints()) {
            endpointNode.writeDescriptor
                    (topNode, WebServicesTagNames.PORT_COMPONENT, next);
        }

        return topNode;
    }

}

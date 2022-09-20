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

import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node is responsible for loading web services reference information
 *
 * @author  Kenneth Saks
 */
public class WebServiceNode extends DisplayableComponentNode<WebService> {

    private final static XMLElement TAG = new XMLElement(WebServicesTagNames.WEB_SERVICE);

    public WebServiceNode() {
        registerElementHandler(new XMLElement(WebServicesTagNames.PORT_COMPONENT), WebServiceEndpointNode.class);
    }


    @Override
    protected WebService createDescriptor() {
        return new WebService();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME, "setName");
        table.put(WebServicesTagNames.WSDL_FILE, "setWsdlFileUri");
        table.put(WebServicesTagNames.JAXRPC_MAPPING_FILE, "setMappingFileUri");
        return table;
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return TAG;
    }


    @Override
    public void addDescriptor(Object descriptor) {
        WebServiceEndpoint endpoint = (WebServiceEndpoint) descriptor;
        WebService webService = getDescriptor();
        webService.addEndpoint(endpoint);
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, WebService descriptor) {
        Node topNode = super.writeDescriptor(parent, nodeName, descriptor);

        writeDisplayableComponentInfo(topNode, descriptor);

        appendTextChild(topNode, WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME, descriptor.getName());
        appendTextChild(topNode, WebServicesTagNames.WSDL_FILE, descriptor.getWsdlFileUri());
        appendTextChild(topNode, WebServicesTagNames.JAXRPC_MAPPING_FILE, descriptor.getMappingFileUri());

        WebServiceEndpointNode endpointNode = new WebServiceEndpointNode();
        for (WebServiceEndpoint next : descriptor.getEndpoints()) {
            endpointNode.writeDescriptor(topNode, WebServicesTagNames.PORT_COMPONENT, next);
        }

        return topNode;
    }
}

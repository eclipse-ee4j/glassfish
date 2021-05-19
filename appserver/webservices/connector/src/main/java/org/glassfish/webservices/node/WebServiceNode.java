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

package org.glassfish.webservices.node;

import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node is responsible for loading web services
 * reference information
 *
 * @author  Kenneth Saks
 */
public class WebServiceNode extends DisplayableComponentNode {

    private final static XMLElement tag =
        new XMLElement(WebServicesTagNames.WEB_SERVICE);

    public WebServiceNode() {
        super();
        registerElementHandler
            (new XMLElement(WebServicesTagNames.PORT_COMPONENT),
             WebServiceEndpointNode.class);
    }

    /**
     * initilizer method after instance creation
     */
    protected void init() {
    }

    @Override
    protected WebService createDescriptor() {
        return new WebService();
    }
    /**
     * all sub-implementation of this class can use a dispatch table
     * to map xml element to method name on the descriptor class for
     * setting the element value.
     *
     * @return map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME,
                  "setName");
        table.put(WebServicesTagNames.WSDL_FILE, "setWsdlFileUri");
        table.put(WebServicesTagNames.JAXRPC_MAPPING_FILE, "setMappingFileUri");
        return table;
    }

    /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }

    /**
     * Adds  a new DOL descriptor instance to the descriptor
     * instance associated with this XMLNode
     *
     * @param descriptor the new descriptor
     */
    public void addDescriptor(Object descriptor) {
        WebServiceEndpoint endpoint = (WebServiceEndpoint) descriptor;
        WebService webService = (WebService) getDescriptor();
        webService.addEndpoint(endpoint);
    }

    /**
     * write the method descriptor class to a query-method DOM tree and
     * return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
                                WebService descriptor) {
        Node topNode =
            super.writeDescriptor(parent, nodeName, descriptor);

        writeDisplayableComponentInfo(topNode, descriptor);

        appendTextChild(topNode,
                        WebServicesTagNames.WEB_SERVICE_DESCRIPTION_NAME,
                        descriptor.getName());
        appendTextChild(topNode, WebServicesTagNames.WSDL_FILE,
                        descriptor.getWsdlFileUri());
        appendTextChild(topNode, WebServicesTagNames.JAXRPC_MAPPING_FILE,
                        descriptor.getMappingFileUri());

        WebServiceEndpointNode endpointNode = new WebServiceEndpointNode();
        for(WebServiceEndpoint next : descriptor.getEndpoints()) {
            endpointNode.writeDescriptor
                (topNode, WebServicesTagNames.PORT_COMPONENT, next);
        }

        return topNode;
    }

}

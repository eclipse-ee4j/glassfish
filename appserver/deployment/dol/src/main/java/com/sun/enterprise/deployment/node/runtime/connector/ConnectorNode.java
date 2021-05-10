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

package com.sun.enterprise.deployment.node.runtime.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeBundleNode;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.deployment.runtime.connector.RoleMap;
import com.sun.enterprise.deployment.runtime.connector.SunConnector;
import com.sun.enterprise.deployment.xml.DTDRegistry;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * This node handles the sun-connector runtime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class ConnectorNode extends RuntimeBundleNode<ConnectorDescriptor> {

    protected SunConnector connector = null;

    /**
     * Initialize the child handlers
     */
    public ConnectorNode(ConnectorDescriptor descriptor) {
        super(descriptor);

        // we do not care about our standard DDS handles
        handlers = null;

        registerElementHandler(new XMLElement(RuntimeTagNames.RESOURCE_ADAPTER), ResourceAdapterNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.ROLE_MAP), RoleMapNode.class);
    }


    /**
     * Adds a new DOL descriptor instance to the descriptor instance
     * associated with this XMLNode
     *
     * @param newDescriptor the new descriptor
     */
    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof ResourceAdapter) {
            getSunConnectorDescriptor().setResourceAdapter((ResourceAdapter) newDescriptor);
        } else if (newDescriptor instanceof RoleMap) {
            getSunConnectorDescriptor().setRoleMap((RoleMap) newDescriptor);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }


    /**
     * @return the DOCTYPE that should be written to the XML file
     */
    @Override
    public String getDocType() {
        return DTDRegistry.SUN_CONNECTOR_100_DTD_PUBLIC_ID;
    }


    /**
     * @return the SystemID of the XML file
     */
    @Override
    public String getSystemID() {
        return DTDRegistry.SUN_CONNECTOR_100_DTD_SYSTEM_ID;
    }


    /**
     * @return NULL for all runtime nodes.
     */
    @Override
    public List<String> getSystemIDs() {
        return null;
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return new XMLElement(RuntimeTagNames.S1AS_CONNECTOR_RUNTIME_TAG);
    }


    /**
     * register this node as a root node capable of loading entire DD files
     *
     * @param publicIDToDTD is a mapping between xml Public-ID to DTD
     * @return the doctype tag name
     */
    public static String registerBundle(Map publicIDToDTD) {
        publicIDToDTD.put(DTDRegistry.SUN_CONNECTOR_100_DTD_PUBLIC_ID, DTDRegistry.SUN_CONNECTOR_100_DTD_SYSTEM_ID);
        return RuntimeTagNames.S1AS_CONNECTOR_RUNTIME_TAG;
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    public SunConnector getSunConnectorDescriptor() {
        if (connector == null) {
            connector = new SunConnector();
            descriptor.setSunDescriptor(connector);
        }
        return connector;
    }


    @Override
    public ConnectorDescriptor getDescriptor() {
        return descriptor;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName name for the descriptor
     * @param connector the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, ConnectorDescriptor connector) {
        Node connectorNode = super.writeDescriptor(parent, nodeName, connector);

        // resource-adapter
        SunConnector sunDesc = connector.getSunDescriptor();
        if (sunDesc != null) {
            ResourceAdapterNode ran = new ResourceAdapterNode();
            ran.writeDescriptor(connectorNode, RuntimeTagNames.RESOURCE_ADAPTER, sunDesc.getResourceAdapter());

            // role-map ?
            if (sunDesc.getRoleMap() != null) {
                RoleMapNode rmn = new RoleMapNode();
                rmn.writeDescriptor(connectorNode, RuntimeTagNames.ROLE_MAP, sunDesc.getRoleMap());
            }
        }
        return connectorNode;
    }
}

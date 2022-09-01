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

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;

import org.w3c.dom.Node;


/**
 * This class is responsible for encapsulating all information specific to the Connector DTD
 *
 * @author Sheetal Vartak
 */
public final class RANode extends DeploymentDescriptorNode<ConnectorDescriptor> {

    // Descriptor class we are using
    private ConnectorDescriptor descriptor;

    // default constructor
    public RANode() {
        register();
    }


    public RANode(XMLElement element) {
        this.setXMLRootTag(element);
        register();
    }


    private void register() {
        // check for the version of DTD
        registerElementHandler(new XMLElement(ConnectorTagNames.OUTBOUND_RESOURCE_ADAPTER), OutBoundRANode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.INBOUND_RESOURCE_ADAPTER), InBoundRANode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class, "addConfigProperty");
        registerElementHandler(new XMLElement(ConnectorTagNames.ADMIN_OBJECT), AdminObjectNode.class, "addAdminObject");
        registerElementHandler(new XMLElement(ConnectorTagNames.SECURITY_PERMISSION), SecurityPermissionNode.class,
            "addSecurityPermission");
    }


    @Override
    public ConnectorDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = (ConnectorDescriptor) getParentNode().getDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.RESOURCE_ADAPTER_CLASS, "setResourceAdapterClass");
        return table;
    }


    public static Node writeConnectorDescriptor(Node connectorNode, ConnectorDescriptor connDescriptor) {
        Node raNode = appendChild(connectorNode, ConnectorTagNames.RESOURCE_ADAPTER);

        appendTextChild(raNode, ConnectorTagNames.RESOURCE_ADAPTER_CLASS, connDescriptor.getResourceAdapterClass());
        ConfigPropertyNode.write(raNode, connDescriptor);
        if (connDescriptor.getOutBoundDefined()) {
            OutBoundRANode.writeOutboundResourceAdapter(raNode, connDescriptor.getOutboundResourceAdapter());
        }
        if (connDescriptor.getInBoundDefined()) {
            InBoundRANode.writeInboundResourceAdapter(raNode, connDescriptor.getInboundResourceAdapter());
        }
        AdminObjectNode.writeAdminObjects(raNode, connDescriptor.getAdminObjects());
        SecurityPermissionNode.writeSecurityPermissions(raNode, connDescriptor.getSecurityPermissions());
        return connectorNode;
    }
}

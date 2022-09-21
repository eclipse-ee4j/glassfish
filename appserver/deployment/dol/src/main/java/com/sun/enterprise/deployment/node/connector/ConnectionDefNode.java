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

import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONNECTION_DEFINITION;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONNECTION_FACTORY_IMPL;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONNECTION_FACTORY_INTF;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONNECTION_IMPL;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONNECTION_INTF;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.MANAGED_CONNECTION_FACTORY;

/**
 * This node signifies the connection-definition tag in Connector DTD
 *
 * @author Sheetal Vartak
 */
public class ConnectionDefNode extends DeploymentDescriptorNode<ConnectionDefDescriptor> {

    private ConnectionDefDescriptor descriptor;

    public static Node writeConnectionDefDescriptors(Node parent, Set<ConnectionDefDescriptor> connectionDefs) {
        for (ConnectionDefDescriptor con : connectionDefs) {
            Node conNode = appendChild(parent, CONNECTION_DEFINITION);
            appendTextChild(conNode, MANAGED_CONNECTION_FACTORY, con.getManagedConnectionFactoryImpl());

            ConfigPropertyNode.write(conNode, con);

            appendTextChild(conNode, CONNECTION_FACTORY_INTF, con.getConnectionFactoryIntf());
            appendTextChild(conNode, CONNECTION_FACTORY_IMPL, con.getConnectionFactoryImpl());
            appendTextChild(conNode, CONNECTION_INTF, con.getConnectionIntf());
            appendTextChild(conNode, CONNECTION_IMPL, con.getConnectionImpl());
        }
        return parent;
    }


    // default constructor...for normal operation in case of 1.5 DTD
    public ConnectionDefNode() {
        register();
    }


    public ConnectionDefNode(XMLElement element) {
        this.setXMLRootTag(element);
        register();
    }


    @Override
    public ConnectionDefDescriptor getDescriptor() {
        if (descriptor == null) {
            // the descriptor associated with the ConnectionDefNode is a ConnectionDefDescriptor
            // This descriptor is available with the parent node of the ConnectionDefNode
            descriptor = DescriptorFactory.getDescriptor(getXMLPath());
            ((OutboundResourceAdapter) (getParentNode().getDescriptor())).addConnectionDefDescriptor(descriptor);

        }
        return descriptor;
    }


    @Override
    public void addDescriptor(Object obj) {
        if (obj instanceof ConnectorConfigProperty) {
            descriptor.addConfigProperty((ConnectorConfigProperty) obj);
        }
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(MANAGED_CONNECTION_FACTORY, "setManagedConnectionFactoryImpl");
        table.put(CONNECTION_FACTORY_INTF, "setConnectionFactoryIntf");
        table.put(CONNECTION_FACTORY_IMPL, "setConnectionFactoryImpl");
        table.put(CONNECTION_INTF, "setConnectionIntf");
        table.put(CONNECTION_IMPL, "setConnectionImpl");
        return table;
    }


    /**
     * method for registering the handlers with the various tags
     */
    private void register() {
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
    }
}

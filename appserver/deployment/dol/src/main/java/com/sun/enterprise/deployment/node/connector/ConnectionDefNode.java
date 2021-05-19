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
 * EjbRelationRole.java
 *
 * Created on February 1, 2002, 3:07 PM
 */

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Iterator;
import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This node signifies the connection-definition tag in Connector DTD
 *
 * @author Sheetal Vartak
 * @version
 */
public class ConnectionDefNode extends DeploymentDescriptorNode {

    ConnectionDefDescriptor descriptor = null;

    public final static XMLElement tag = new XMLElement(ConnectorTagNames.CONNECTION_DEFINITION);

    // default constructor...for normal operation in case of 1.5 DTD
    public ConnectionDefNode() {
        register();
    }


    public ConnectionDefNode(XMLElement element) {
        this.setXMLRootTag(element);
        register();
    }


    /**
     * method for registering the handlers with the various tags
     */
    private void register() {
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        if (descriptor == null) {
            // the descriptor associated with the ConnectionDefNode is a ConnectionDefDescriptor
            // This descriptor is available with the parent node of the ConnectionDefNode
            descriptor = (ConnectionDefDescriptor) DescriptorFactory.getDescriptor(getXMLPath());
            ((OutboundResourceAdapter) (getParentNode().getDescriptor())).addConnectionDefDescriptor(descriptor);

        }
        return descriptor;
    }


    /**
     * Adds a new DOL descriptor instance to the descriptor instance associated with
     * this XMLNode
     *
     * @param obj the new descriptor
     */
    @Override
    public void addDescriptor(Object obj) {
        if (obj instanceof ConnectorConfigProperty) {
            descriptor.addConfigProperty((ConnectorConfigProperty) obj);
        }
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();

        table.put(ConnectorTagNames.MANAGED_CONNECTION_FACTORY, "setManagedConnectionFactoryImpl");
        table.put(ConnectorTagNames.CONNECTION_FACTORY_INTF, "setConnectionFactoryIntf");
        table.put(ConnectorTagNames.CONNECTION_FACTORY_IMPL, "setConnectionFactoryImpl");
        table.put(ConnectorTagNames.CONNECTION_INTF, "setConnectionIntf");
        table.put(ConnectorTagNames.CONNECTION_IMPL, "setConnectionImpl");

        return table;
    }



    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        //FIXME : remove the foll line once connector stuff works properly
        //((ConnectionDefDescriptor)getDescriptor()).setOutBoundDefined(true);
        super.startElement(element, attributes);
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param desc the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, Descriptor desc) {
        // connection definition info

        if (!(desc instanceof OutboundResourceAdapter)) {
            throw new IllegalArgumentException(
                getClass() + " cannot handle descriptors of type " + descriptor.getClass());
        }
        Iterator connectionDefs = null;
        connectionDefs = ((OutboundResourceAdapter) desc).getConnectionDefs().iterator();

        // connection-definitions
        for (; connectionDefs.hasNext();) {
            ConnectionDefDescriptor con = (ConnectionDefDescriptor) connectionDefs.next();
            Node conNode = appendChild(parent, ConnectorTagNames.CONNECTION_DEFINITION);
            appendTextChild(conNode, ConnectorTagNames.MANAGED_CONNECTION_FACTORY,
                con.getManagedConnectionFactoryImpl());

            ConfigPropertyNode config = new ConfigPropertyNode();
            conNode = config.writeDescriptor(conNode, con);

            appendTextChild(conNode, ConnectorTagNames.CONNECTION_FACTORY_INTF, con.getConnectionFactoryIntf());
            appendTextChild(conNode, ConnectorTagNames.CONNECTION_FACTORY_IMPL, con.getConnectionFactoryImpl());
            appendTextChild(conNode, ConnectorTagNames.CONNECTION_INTF, con.getConnectionIntf());
            appendTextChild(conNode, ConnectorTagNames.CONNECTION_IMPL, con.getConnectionImpl());
        }
        return parent;
    }
}

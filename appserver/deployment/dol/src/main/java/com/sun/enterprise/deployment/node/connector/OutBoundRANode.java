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
 * OutBoundRANode.java
 *
 * Created on February 1, 2002, 3:07 PM
 */

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.SecurityPermission;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This node signifies the outbound-resourceadapter tag in Connector DTD
 *
 * @author Sheetal Vartak
 * @version
 */
public class OutBoundRANode extends DeploymentDescriptorNode {

    OutboundResourceAdapter descriptor = null;

    public final static XMLElement tag = new XMLElement(ConnectorTagNames.OUTBOUND_RESOURCE_ADAPTER);

    // default constructor...for normal operation in case of 1.5 DTD
    public OutBoundRANode() {
        register();
    }


    public OutBoundRANode(XMLElement element) {
        this.setXMLRootTag(element);
        register();
    }


    /**
     * This method is required for 1.0 DTD so that there will be 1 instance of
     * ConnectionDefDescriptor available
     * I know that this constructor will be called only when it is a 1.0 DD
     * dont want to rely on whether 1.0 or 1.5 spec version
     * So this method is called when the ConnectorNode knows that it is for 1.0 DTD
     */
    public void createConDefDescriptorFor10() {
        ConnectionDefDescriptor conDef = new ConnectionDefDescriptor();
        ((OutboundResourceAdapter) getDescriptor()).addConnectionDefDescriptor(conDef);
    }


    /**
     * method for registering the handlers with the various tags
     */
    private void register() {
        registerElementHandler(new XMLElement(ConnectorTagNames.AUTH_MECHANISM), AuthMechNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.CONNECTION_DEFINITION), ConnectionDefNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.SECURITY_PERMISSION), SecurityPermissionNode.class);
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        if (descriptor == null) {
            // the descriptor associated with the OutBoundRANode is a OutboundResourceAdapter
            // This descriptor is available with the parent node of the OutBoundRANode
            descriptor = (OutboundResourceAdapter) DescriptorFactory.getDescriptor(getXMLPath());
            ((ConnectorDescriptor) (getParentNode().getDescriptor())).setOutboundResourceAdapter(descriptor);
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
        if (obj instanceof AuthMechanism) {
            boolean flag = descriptor.addAuthMechanism((AuthMechanism) obj);
            if (flag == false) {
                DOLUtils.getDefaultLogger().finer("The AuthMechanism object already exists in the Descriptor");
            }
        } else if (obj instanceof ConnectionDefDescriptor) {
            descriptor.addConnectionDefDescriptor((ConnectionDefDescriptor) obj);
        } else if (obj instanceof ConnectorConfigProperty) {
            descriptor.addConfigProperty((ConnectorConfigProperty) obj);
        } else if (obj instanceof SecurityPermission) {
            // security-permission element is a direct sub element of
            // resourceadapter, so set the value in ConnectorDescriptor
            ConnectorDescriptor connDesc = (ConnectorDescriptor) getParentNode().getDescriptor();
            connDesc.addSecurityPermission((SecurityPermission) obj);
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

        table.put(ConnectorTagNames.TRANSACTION_SUPPORT, "setTransactionSupport");
        table.put(ConnectorTagNames.REAUTHENTICATION_SUPPORT, "setReauthenticationSupport");

        /**
         * The following setXXX methods are required for 1.0 DTD. For 1.5 DTD, These methods
         * will never be used since the control will be transferred to ConnectionDefNode
         * classes.
         */
        table.put(ConnectorTagNames.MANAGED_CONNECTION_FACTORY, "setManagedConnectionFactoryImpl");

        table.put(ConnectorTagNames.CONNECTION_FACTORY_INTF, "setConnectionFactoryIntf");
        table.put(ConnectorTagNames.CONNECTION_FACTORY_IMPL, "setConnectionFactoryImpl");
        table.put(ConnectorTagNames.CONNECTION_INTF, "setConnectionIntf");
        table.put(ConnectorTagNames.CONNECTION_IMPL, "setConnectionImpl");

        return table;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param connectorNode parent node for the DOM tree
     * @param descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node connectorNode, Descriptor descriptor) {
        // outbound RA info

        Node raNode = appendChild(connectorNode, ConnectorTagNames.OUTBOUND_RESOURCE_ADAPTER);
        append(raNode, ((ConnectorDescriptor) descriptor).getOutboundResourceAdapter());
        return connectorNode;
    }


    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
    }


    /**
     * method to add the child nodes of RESOURCE_ADAPTER and OUTBOUND_RESOURCE_ADAPTER
     */
    private void append(Node raNode, OutboundResourceAdapter conDesc) {
        ConnectionDefNode conDef = new ConnectionDefNode();
        raNode = conDef.writeDescriptor(raNode, conDesc);

        appendTextChild(raNode, ConnectorTagNames.TRANSACTION_SUPPORT, conDesc.getTransSupport());

        AuthMechNode auth = new AuthMechNode();
        raNode = auth.writeDescriptor(raNode, conDesc);

        appendTextChild(raNode, ConnectorTagNames.REAUTHENTICATION_SUPPORT, conDesc.getReauthenticationSupport());

    }
}

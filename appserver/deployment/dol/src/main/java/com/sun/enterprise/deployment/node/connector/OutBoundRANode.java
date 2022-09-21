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

import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This node signifies the outbound-resourceadapter tag in Connector DTD
 *
 * @author Sheetal Vartak
 */
public class OutBoundRANode extends DeploymentDescriptorNode<OutboundResourceAdapter> {

    private OutboundResourceAdapter descriptor;

    public static Node writeOutboundResourceAdapter(Node connectorNode, OutboundResourceAdapter adapter) {
        Node raNode = appendChild(connectorNode, ConnectorTagNames.OUTBOUND_RESOURCE_ADAPTER);
        ConnectionDefNode.writeConnectionDefDescriptors(raNode, adapter.getConnectionDefs());
        appendTextChild(raNode, ConnectorTagNames.TRANSACTION_SUPPORT, adapter.getTransSupport());
        AuthMechNode.writeAuthMechanisms(raNode, adapter.getAuthMechanisms());
        appendTextChild(raNode, ConnectorTagNames.REAUTHENTICATION_SUPPORT, adapter.getReauthenticationSupport());
        return connectorNode;
    }


    /**
     * Default constructor for normal operation in case of 1.5 DTD
     */
    public OutBoundRANode() {
        register();
    }


    public OutBoundRANode(XMLElement element) {
        this.setXMLRootTag(element);
        register();
    }


    /**
     * Method for registering the handlers with the various tags
     */
    private void register() {
        registerElementHandler(new XMLElement(ConnectorTagNames.AUTH_MECHANISM), AuthMechNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.CONNECTION_DEFINITION), ConnectionDefNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
        registerElementHandler(new XMLElement(ConnectorTagNames.SECURITY_PERMISSION), SecurityPermissionNode.class);
    }


    /**
     * This method is required for 1.0 DTD so that there will be 1 instance of
     * ConnectionDefDescriptor available
     * <p>
     * I know that this constructor will be called only when it is a 1.0 DD
     * dont want to rely on whether 1.0 or 1.5 spec version
     * So this method is called when the ConnectorNode knows that it is for 1.0 DTD
     */
    public void createConDefDescriptorFor10() {
        ConnectionDefDescriptor conDef = new ConnectionDefDescriptor();
        getDescriptor().addConnectionDefDescriptor(conDef);
    }


    @Override
    public OutboundResourceAdapter getDescriptor() {
        if (descriptor == null) {
            // the descriptor associated with the OutBoundRANode is a OutboundResourceAdapter
            // This descriptor is available with the parent node of the OutBoundRANode
            descriptor = (OutboundResourceAdapter) DescriptorFactory.getDescriptor(getXMLPath());
            ((ConnectorDescriptor) (getParentNode().getDescriptor())).setOutboundResourceAdapter(descriptor);
        }
        return descriptor;
    }


    @Override
    public void addDescriptor(Object obj) {
        if (obj instanceof AuthMechanism) {
            boolean flag = descriptor.addAuthMechanism((AuthMechanism) obj);
            if (!flag) {
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


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();

        table.put(ConnectorTagNames.TRANSACTION_SUPPORT, "setTransactionSupport");
        table.put(ConnectorTagNames.REAUTHENTICATION_SUPPORT, "setReauthenticationSupport");

        // The following setXXX methods are required for 1.0 DTD. For 1.5 DTD, These methods
        // will never be used since the control will be transferred to ConnectionDefNode classes.
        table.put(ConnectorTagNames.MANAGED_CONNECTION_FACTORY, "setManagedConnectionFactoryImpl");

        table.put(ConnectorTagNames.CONNECTION_FACTORY_INTF, "setConnectionFactoryIntf");
        table.put(ConnectorTagNames.CONNECTION_FACTORY_IMPL, "setConnectionFactoryImpl");
        table.put(ConnectorTagNames.CONNECTION_INTF, "setConnectionIntf");
        table.put(ConnectorTagNames.CONNECTION_IMPL, "setConnectionImpl");

        return table;
    }


    /**
     * Doesn't do anything.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
    }
}

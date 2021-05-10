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
 * ConnectorNode.java. This class is responsible for encapsulating all information specific to the Connector DTD
 *
 * Created on April 18th, 2002, 4.34 PM
 */

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.ConnectorDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import org.w3c.dom.Node;

import java.util.Map;


/**
 * @author Sheetal Vartak
 * @version
 */
public final class RANode extends DeploymentDescriptorNode {

    // Descriptor class we are using
    private ConnectorDescriptor descriptor = null;
    public static final String VERSION_10 = "1.0";
    public static final String VERSION_15 = "1.5";

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


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        if (descriptor == null) {
            descriptor = (ConnectorDescriptor) getParentNode().getDescriptor();
        }
        return descriptor;
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
        table.put(ConnectorTagNames.RESOURCE_ADAPTER_CLASS, "setResourceAdapterClass");
        return table;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node connectorNode, Descriptor descriptor) {
        if (!(descriptor instanceof ConnectorDescriptor)) {
            throw new IllegalArgumentException(
                getClass() + " cannot handle descriptors of type " + descriptor.getClass());
        }
        ConnectorDescriptor conDesc = (ConnectorDescriptor) descriptor;
        Node raNode = appendChild(connectorNode, ConnectorTagNames.RESOURCE_ADAPTER);

        appendTextChild(raNode, ConnectorTagNames.RESOURCE_ADAPTER_CLASS, conDesc.getResourceAdapterClass());

        // config-property
        ConfigPropertyNode config = new ConfigPropertyNode();
        raNode = config.writeDescriptor(raNode, conDesc);

        if (conDesc.getOutBoundDefined() == true) {
            // outbound RA info
            OutBoundRANode obNode = new OutBoundRANode();
            raNode = obNode.writeDescriptor(raNode, conDesc);
        }

        if (conDesc.getInBoundDefined() == true) {
            // inbound RA info
            InBoundRANode inNode = new InBoundRANode();
            raNode = inNode.writeDescriptor(raNode, conDesc);
        }

        // adminobject
        AdminObjectNode admin = new AdminObjectNode();
        raNode = admin.writeDescriptor(raNode, conDesc);
        // }

        // security-permission*
        SecurityPermissionNode secPerm = new SecurityPermissionNode();
        raNode = secPerm.writeDescriptor(raNode, conDesc);

        return connectorNode;
    }
}

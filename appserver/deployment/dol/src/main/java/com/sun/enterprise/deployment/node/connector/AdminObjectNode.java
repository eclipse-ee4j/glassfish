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

package com.sun.enterprise.deployment.node.connector;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Map;

/**
 * This node is responsible for handling the Connector DTD related auth-mechanism XML tag
 *
 * @author  Sheetal Vartak
 * @version
 */
public class AdminObjectNode extends DeploymentDescriptorNode {

    private AdminObject adminObject = null;

    public AdminObjectNode() {
        register();
    }

    /**
     * method for registering the handlers with the various tags
     */
    private void register() {
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(ConnectorTagNames.ADMIN_OBJECT_INTERFACE, "setAdminObjectInterface");
        table.put(ConnectorTagNames.ADMIN_OBJECT_CLASS, "setAdminObjectClass");
        return table;
    }

    /**
     * Adds  a new DOL descriptor instance to the descriptor instance associated with
     * this XMLNode
     *
     * @param descriptor the new descriptor
     */
    public void addDescriptor(Object descriptor) {
        if (descriptor instanceof ConnectorConfigProperty) {
            adminObject.addConfigProperty((ConnectorConfigProperty)descriptor);
        }
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    public Object getDescriptor() {
        if (adminObject == null) {
            adminObject = (AdminObject) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return adminObject;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, Descriptor descriptor) {
        if (!(descriptor instanceof ConnectorDescriptor)) {
            throw new IllegalArgumentException(getClass() + " cannot handle descriptors of type " + descriptor.getClass());
        }

        //adminObject info
        for (Iterator adminObjects = ((ConnectorDescriptor)descriptor).getAdminObjects().iterator(); adminObjects.hasNext();) {
            AdminObject adminObject = (AdminObject) adminObjects.next();
            Node adminObjectNode = appendChild(parent, ConnectorTagNames.ADMIN_OBJECT);
            appendTextChild(adminObjectNode, ConnectorTagNames.ADMIN_OBJECT_INTERFACE, adminObject.getAdminObjectInterface());
            appendTextChild(adminObjectNode, ConnectorTagNames.ADMIN_OBJECT_CLASS, adminObject.getAdminObjectClass());

            ConfigPropertyNode config = new ConfigPropertyNode();
            adminObjectNode = config.writeDescriptor(adminObjectNode, adminObject);
        }

        return parent;
    }
}

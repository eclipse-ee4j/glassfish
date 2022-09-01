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

import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling the Connector DTD related auth-mechanism XML tag
 *
 * @author Sheetal Vartak
 */
public class AdminObjectNode extends DeploymentDescriptorNode<AdminObject> {

    private AdminObject adminObject;

    public static Node writeAdminObjects(Node parent, Set<AdminObject> adminObjects) {
        for (AdminObject element : adminObjects) {
            Node adminObjectNode = appendChild(parent, ConnectorTagNames.ADMIN_OBJECT);
            appendTextChild(adminObjectNode, ConnectorTagNames.ADMIN_OBJECT_INTERFACE, element.getAdminObjectInterface());
            appendTextChild(adminObjectNode, ConnectorTagNames.ADMIN_OBJECT_CLASS, element.getAdminObjectClass());
            ConfigPropertyNode.write(adminObjectNode, element);
        }
        return parent;
    }


    public AdminObjectNode() {
        registerElementHandler(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConfigPropertyNode.class);
    }


    @Override
    public AdminObject getDescriptor() {
        if (adminObject == null) {
            adminObject = DescriptorFactory.getDescriptor(getXMLPath());
        }
        return adminObject;
    }


    @Override
    public void addDescriptor(Object descriptor) {
        if (descriptor instanceof ConnectorConfigProperty) {
            adminObject.addConfigProperty((ConnectorConfigProperty) descriptor);
        }
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.ADMIN_OBJECT_INTERFACE, "setAdminObjectInterface");
        table.put(ConnectorTagNames.ADMIN_OBJECT_CLASS, "setAdminObjectClass");
        return table;
    }
}

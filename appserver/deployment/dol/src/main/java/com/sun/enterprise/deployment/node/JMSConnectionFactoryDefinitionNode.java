/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.JMSConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.TagNames;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.logging.Level;

public class JMSConnectionFactoryDefinitionNode extends DeploymentDescriptorNode<JMSConnectionFactoryDefinitionDescriptor> {

    public final static XMLElement tag = new XMLElement(TagNames.JMS_CONNECTION_FACTORY);

    private JMSConnectionFactoryDefinitionDescriptor descriptor = null;

    public JMSConnectionFactoryDefinitionNode() {
        registerElementHandler(new XMLElement(TagNames.JMS_CONNECTION_FACTORY_PROPERTY), ResourcePropertyNode.class,
                "addJMSConnectionFactoryPropertyDescriptor");
    }

    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();

        table.put(TagNames.JMS_CONNECTION_FACTORY_DESCRIPTION, "setDescription");
        table.put(TagNames.JMS_CONNECTION_FACTORY_NAME, "setName");
        table.put(TagNames.JMS_CONNECTION_FACTORY_INTERFACE_NAME, "setInterfaceName");
        table.put(TagNames.JMS_CONNECTION_FACTORY_CLASS_NAME, "setClassName");
        table.put(TagNames.JMS_CONNECTION_FACTORY_RESOURCE_ADAPTER, "setResourceAdapter");
        table.put(TagNames.JMS_CONNECTION_FACTORY_USER, "setUser");
        table.put(TagNames.JMS_CONNECTION_FACTORY_PASSWORD, "setPassword");
        table.put(TagNames.JMS_CONNECTION_FACTORY_CLIENT_ID, "setClientId");

        table.put(TagNames.JMS_CONNECTION_FACTORY_TRANSACTIONAL, "setTransactional");
        table.put(TagNames.JMS_CONNECTION_FACTORY_MAX_POOL_SIZE, "setMaxPoolSize");
        table.put(TagNames.JMS_CONNECTION_FACTORY_MIN_POOL_SIZE, "setMinPoolSize");

        return table;
    }

    @LogMessageInfo(
        message = "For jms-connection-factory resource: {0}, there is no application part in its resource adapter name: {1}.",
        level = "WARNING",
        cause = "For embedded resource adapter, its internal format of resource adapter name should contains application name.",
        comment = "For the method writeDescriptor of com.sun.enterprise.deployment.node.JMSConnectionFactoryDefinitionNode."
    )
    private static final String RESOURCE_ADAPTER_NAME_INVALID = "AS-DEPLOYMENT-00024";

    public Node writeDescriptor(Node parent, String nodeName, JMSConnectionFactoryDefinitionDescriptor desc) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_DESCRIPTION, desc.getDescription());
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_NAME, desc.getName());
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_INTERFACE_NAME, desc.getInterfaceName());
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_CLASS_NAME, desc.getClassName());

        // change the resource adapter name from internal format to standard format
        String resourceAdapter = desc.getResourceAdapter();
        if (resourceAdapter != null) {
            int poundIndex = resourceAdapter.indexOf("#");
            if (poundIndex > 0) {
                // the internal format of resource adapter name is "appName#raName", remove the appName part
                resourceAdapter =  resourceAdapter.substring(poundIndex);
            } else if (poundIndex == 0) {
                // the resource adapter name should not be the standard format "#raName" here
                DOLUtils.getDefaultLogger().log(Level.WARNING, RESOURCE_ADAPTER_NAME_INVALID,
                        new Object[] { desc.getName(), desc.getResourceAdapter() });
            } else {
                // the resource adapter name represent the standalone RA in this case.
            }
        }
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_RESOURCE_ADAPTER, resourceAdapter);
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_USER, desc.getUser());
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_PASSWORD, desc.getPassword());
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_CLIENT_ID, desc.getClientId());

        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, desc);

        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_TRANSACTIONAL, String.valueOf(desc.isTransactional()));
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_MAX_POOL_SIZE, desc.getMaxPoolSize());
        appendTextChild(node, TagNames.JMS_CONNECTION_FACTORY_MIN_POOL_SIZE, desc.getMinPoolSize());

        return node;
    }

    public JMSConnectionFactoryDefinitionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new JMSConnectionFactoryDefinitionDescriptor();
        }
        return descriptor;
    }
}

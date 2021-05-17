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

import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.TagNames;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.logging.Level;

public class ConnectionFactoryDefinitionNode extends DeploymentDescriptorNode<ConnectionFactoryDefinitionDescriptor> {
    public final static XMLElement tag = new XMLElement(TagNames.CONNECTION_FACTORY);

    private ConnectionFactoryDefinitionDescriptor descriptor = null;

    public ConnectionFactoryDefinitionNode() {
        registerElementHandler(new XMLElement(TagNames.RESOURCE_PROPERTY), ResourcePropertyNode.class,
                "addConnectionFactoryPropertyDescriptor");
    }

    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.CONNECTION_FACTORY_NAME, "setName");
        table.put(TagNames.CONNECTION_FACTORY_INTERFACE_NAME, "setInterfaceName");
        table.put(TagNames.CONNECTION_FACTORY_ADAPTER, "setResourceAdapter");
        table.put(TagNames.CONNECTION_FACTORY_TRANSACTION_SUPPORT, "setTransactionSupport");
        table.put(TagNames.CONNECTION_FACTORY_MAX_POOL_SIZE, "setMaxPoolSize");
        table.put(TagNames.CONNECTION_FACTORY_MIN_POOL_SIZE, "setMinPoolSize");

        return table;
    }

    @LogMessageInfo(
            message = "For connection-factory resource: {0}, there is no application part in its resource adapter name: {1}.",
            level="WARNING",
            cause = "For embedded resource adapter, its internal format of resource adapter name should contains application name.",
            comment = "For the method writeDescriptor of com.sun.enterprise.deployment.node.ConnectionFactoryDefinitionNode."
            )
    private static final String RESOURCE_ADAPTER_NAME_INVALID = "AS-DEPLOYMENT-00023";

    public Node writeDescriptor(Node parent, String nodeName, ConnectionFactoryDefinitionDescriptor desc) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.CONNECTION_FACTORY_DESCRIPTION, desc.getDescription());
        appendTextChild(node, TagNames.CONNECTION_FACTORY_NAME, desc.getName());
        appendTextChild(node, TagNames.CONNECTION_FACTORY_INTERFACE_NAME, desc.getInterfaceName());

        // change the resource adapter name from internal format to standard format
        String resourceAdapterName = desc.getResourceAdapter();
        int poundIndex = resourceAdapterName.indexOf("#");
        if(poundIndex > 0){
            // the internal format of resource adapter name is "appName#raName", remove the appName part
            resourceAdapterName =  resourceAdapterName.substring(poundIndex);

        }else if(poundIndex == 0){
            // the resource adapter name should not be the standard format "#raName" here
            DOLUtils.getDefaultLogger().log(Level.WARNING, RESOURCE_ADAPTER_NAME_INVALID,
                    new Object[] { desc.getName(), desc.getResourceAdapter() });
        }else{
            // the resource adapter name represent the standalone RA in this case.
        }
        appendTextChild(node, TagNames.CONNECTION_FACTORY_ADAPTER, resourceAdapterName);

        appendTextChild(node, TagNames.CONNECTION_FACTORY_MAX_POOL_SIZE, desc.getMaxPoolSize());
        appendTextChild(node, TagNames.CONNECTION_FACTORY_MIN_POOL_SIZE, desc.getMinPoolSize());
        appendTextChild(node, TagNames.CONNECTION_FACTORY_TRANSACTION_SUPPORT, desc.getTransactionSupport());

        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, desc);

        return node;
    }

    public ConnectionFactoryDefinitionDescriptor getDescriptor() {
        if(descriptor == null){
            descriptor = new ConnectionFactoryDefinitionDescriptor();
        }
        return descriptor;
    }

}

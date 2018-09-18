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

package com.sun.enterprise.deployment.node;

import org.w3c.dom.Node;


import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;

import java.util.Map;

public class DataSourceDefinitionNode extends DeploymentDescriptorNode<DataSourceDefinitionDescriptor> {

    public final static XMLElement tag = new XMLElement(TagNames.DATA_SOURCE);
    private DataSourceDefinitionDescriptor descriptor = null;
    public DataSourceDefinitionNode() {
        registerElementHandler(new XMLElement(TagNames.RESOURCE_PROPERTY), ResourcePropertyNode.class,
                "addDataSourcePropertyDescriptor");
    }

    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();

        table.put(TagNames.DATA_SOURCE_DESCRIPTION, "setDescription");
        table.put(TagNames.DATA_SOURCE_NAME, "setName");
        table.put(TagNames.DATA_SOURCE_CLASS_NAME, "setClassName");
        table.put(TagNames.DATA_SOURCE_SERVER_NAME, "setServerName");
        table.put(TagNames.DATA_SOURCE_PORT_NUMBER, "setPortNumber");
        table.put(TagNames.DATA_SOURCE_DATABASE_NAME, "setDatabaseName");
        table.put(TagNames.DATA_SOURCE_URL, "setUrl");
        table.put(TagNames.DATA_SOURCE_USER, "setUser");
        table.put(TagNames.DATA_SOURCE_PASSWORD, "setPassword");
        //
        table.put(TagNames.DATA_SOURCE_LOGIN_TIMEOUT, "setLoginTimeout");
        table.put(TagNames.DATA_SOURCE_TRANSACTIONAL, "setTransactional");
        table.put(TagNames.DATA_SOURCE_ISOLATION_LEVEL, "setIsolationLevel");
        table.put(TagNames.DATA_SOURCE_INITIAL_POOL_SIZE, "setInitialPoolSize");
        table.put(TagNames.DATA_SOURCE_MAX_POOL_SIZE, "setMaxPoolSize");
        table.put(TagNames.DATA_SOURCE_MIN_POOL_SIZE, "setMinPoolSize");
        table.put(TagNames.DATA_SOURCE_MAX_IDLE_TIME, "setMaxIdleTime");
        table.put(TagNames.DATA_SOURCE_MAX_STATEMENTS, "setMaxStatements");

        return table;
    }


    public Node writeDescriptor(Node parent, String nodeName, DataSourceDefinitionDescriptor dataSourceDesc) {

        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.DATA_SOURCE_DESCRIPTION, dataSourceDesc.getDescription());
        appendTextChild(node, TagNames.DATA_SOURCE_NAME, dataSourceDesc.getName());
        appendTextChild(node, TagNames.DATA_SOURCE_CLASS_NAME, dataSourceDesc.getClassName());
        appendTextChild(node, TagNames.DATA_SOURCE_SERVER_NAME, dataSourceDesc.getServerName());
        appendTextChild(node, TagNames.DATA_SOURCE_PORT_NUMBER, dataSourceDesc.getPortNumber());
        appendTextChild(node, TagNames.DATA_SOURCE_DATABASE_NAME, dataSourceDesc.getDatabaseName());
        appendTextChild(node, TagNames.DATA_SOURCE_URL, dataSourceDesc.getUrl());
        appendTextChild(node, TagNames.DATA_SOURCE_USER, dataSourceDesc.getUser());
        appendTextChild(node, TagNames.DATA_SOURCE_PASSWORD, dataSourceDesc.getPassword());

        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, dataSourceDesc);

        appendTextChild(node, TagNames.DATA_SOURCE_LOGIN_TIMEOUT, String.valueOf(dataSourceDesc.getLoginTimeout()));
        appendTextChild(node, TagNames.DATA_SOURCE_TRANSACTIONAL, String.valueOf(dataSourceDesc.isTransactional()));
        //DD specified Enumeration values are String
        //Annotation uses integer values and hence this mapping is needed
        String isolationLevelString = dataSourceDesc.getIsolationLevelString();
        if(isolationLevelString != null){
            appendTextChild(node, TagNames.DATA_SOURCE_ISOLATION_LEVEL, isolationLevelString);
        }
        appendTextChild(node, TagNames.DATA_SOURCE_INITIAL_POOL_SIZE, dataSourceDesc.getInitialPoolSize());
        appendTextChild(node, TagNames.DATA_SOURCE_MAX_POOL_SIZE, dataSourceDesc.getMaxPoolSize());
        appendTextChild(node, TagNames.DATA_SOURCE_MIN_POOL_SIZE, dataSourceDesc.getMinPoolSize());
        appendTextChild(node, TagNames.DATA_SOURCE_MAX_IDLE_TIME, String.valueOf(dataSourceDesc.getMaxIdleTime()));
        appendTextChild(node, TagNames.DATA_SOURCE_MAX_STATEMENTS, dataSourceDesc.getMaxStatements());

        return node;
    }

    public DataSourceDefinitionDescriptor getDescriptor() {
        if(descriptor == null){
            descriptor = new DataSourceDefinitionDescriptor();
        }
        return descriptor;
    }
}

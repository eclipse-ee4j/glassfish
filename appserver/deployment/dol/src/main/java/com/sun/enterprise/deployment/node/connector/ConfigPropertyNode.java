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
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONFIG_PROPERTY_CONFIDENTIAL;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONFIG_PROPERTY_IGNORE;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONFIG_PROPERTY_NAME;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONFIG_PROPERTY_TYPE;
import static com.sun.enterprise.deployment.xml.ConnectorTagNames.CONFIG_PROPERTY_VALUE;

/**
 * This node is responsible for handling the Connector DTD related config-property XML tag
 *
 * @author Sheetal Vartak
 */
public class ConfigPropertyNode extends DeploymentDescriptorNode<ConnectorConfigProperty> {

    private ConnectorConfigProperty config;

    public static Node write(Node parent, AdminObject descriptor) {
        return write(parent, descriptor.getConfigProperties());
    }


    public static Node write(Node parent, ConnectionDefDescriptor descriptor) {
        return write(parent, descriptor.getConfigProperties());
    }


    public static Node write(Node parent, ConnectorDescriptor descriptor) {
        return write(parent, descriptor.getConfigProperties());
    }


    public static Node write(Node parent, MessageListener descriptor) {
        return write(parent, descriptor.getConfigProperties());
    }


    @Override
    public ConnectorConfigProperty getDescriptor() {
        if (config == null) {
            config = DescriptorFactory.getDescriptor(getXMLPath());
        }
        return config;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(CONFIG_PROPERTY_NAME, "setName");
        table.put(CONFIG_PROPERTY_VALUE, "setValue");
        table.put(CONFIG_PROPERTY_TYPE, "setType");
        table.put(CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES, "setSupportsDynamicUpdates");
        table.put(CONFIG_PROPERTY_IGNORE, "setIgnore");
        table.put(CONFIG_PROPERTY_CONFIDENTIAL, "setConfidential");
        return table;
    }


    private static Node write(Node parent, final Set<ConnectorConfigProperty> configProps) {
        for (ConnectorConfigProperty cfg : configProps) {
            Node configNode = appendChild(parent, ConnectorTagNames.CONFIG_PROPERTY);
            writeLocalizedDescriptions(configNode, cfg);
            appendTextChild(configNode, CONFIG_PROPERTY_NAME, cfg.getName());
            appendTextChild(configNode, CONFIG_PROPERTY_TYPE, cfg.getType());
            appendTextChild(configNode, CONFIG_PROPERTY_VALUE, cfg.getValue());
            appendTextChild(configNode, CONFIG_PROPERTY_IGNORE, cfg.isIgnore());
            appendTextChild(configNode, CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES, cfg.isSupportsDynamicUpdates());
            appendTextChild(configNode, CONFIG_PROPERTY_CONFIDENTIAL, cfg.isConfidential());
        }
        return parent;
    }
}

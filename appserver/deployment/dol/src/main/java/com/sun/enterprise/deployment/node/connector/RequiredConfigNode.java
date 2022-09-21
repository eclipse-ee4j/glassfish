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

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

/**
 * This node is responsible for handling the Connector DTD related required-config-property XML tag
 *
 * @author Sheetal Vartak
 */
public class RequiredConfigNode extends DeploymentDescriptorNode<EnvironmentProperty> {

    private EnvironmentProperty config;

    public static Node write(Node parent, MessageListener descriptor) {
        Set<EnvironmentProperty> configProps = descriptor.getRequiredConfigProperties();
        for (EnvironmentProperty cfg : configProps) {
            Node configNode = appendChild(parent, ConnectorTagNames.REQUIRED_CONFIG_PROP);
            writeLocalizedDescriptions(configNode, cfg);
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_NAME, cfg.getName());
        }
        return parent;
    }


    @Override
    public EnvironmentProperty getDescriptor() {
        if (config == null) {
            config = (EnvironmentProperty) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return config;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.CONFIG_PROPERTY_NAME, "setName");
        return table;
    }
}

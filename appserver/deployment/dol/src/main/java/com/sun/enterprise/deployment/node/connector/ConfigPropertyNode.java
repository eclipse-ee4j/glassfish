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

import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.DescriptorFactory;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;

import java.util.Iterator;
import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling the Connector DTD related config-property XML tag
 *
 * @author  Sheetal Vartak
 * @version
 */
public class ConfigPropertyNode extends DeploymentDescriptorNode<ConnectorConfigProperty> {

    private ConnectorConfigProperty config;

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(ConnectorTagNames.CONFIG_PROPERTY_NAME, "setName");
        table.put(ConnectorTagNames.CONFIG_PROPERTY_VALUE, "setValue");
        table.put(ConnectorTagNames.CONFIG_PROPERTY_TYPE, "setType");
        table.put(ConnectorTagNames.CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES, "setSupportsDynamicUpdates");
        table.put(ConnectorTagNames.CONFIG_PROPERTY_IGNORE, "setIgnore");
        table.put(ConnectorTagNames.CONFIG_PROPERTY_CONFIDENTIAL, "setConfidential");
        return table;
    }


    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public ConnectorConfigProperty getDescriptor() {
        if (config == null) {
            config = (ConnectorConfigProperty) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return config;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, Descriptor descriptor) {
        if (!(descriptor instanceof ConnectorDescriptor)
            && !(descriptor instanceof AdminObject)
            && !(descriptor instanceof ConnectionDefDescriptor)
            && !(descriptor instanceof OutboundResourceAdapter)
            && !(descriptor instanceof MessageListener)) {
            throw new IllegalArgumentException(
                getClass() + " cannot handle descriptors of type " + descriptor.getClass());
        }
        final Iterator<ConnectorConfigProperty> configProps;
        if (descriptor instanceof ConnectorDescriptor) {
            configProps = ((ConnectorDescriptor) descriptor).getConfigProperties().iterator();
        } else if (descriptor instanceof ConnectionDefDescriptor) {
            configProps = ((ConnectionDefDescriptor) descriptor).getConfigProperties().iterator();
        } else if (descriptor instanceof AdminObject) {
            configProps = ((AdminObject) descriptor).getConfigProperties().iterator();
        } else if (descriptor instanceof OutboundResourceAdapter) {
            configProps = ((OutboundResourceAdapter) descriptor).getConfigProperties().iterator();
        } else if (descriptor instanceof MessageListener) {
            configProps = ((MessageListener) descriptor).getConfigProperties().iterator();
        } else {
            return parent;
        }
        // config property info
        while (configProps.hasNext()) {
            ConnectorConfigProperty cfg = configProps.next();
            Node configNode = appendChild(parent, ConnectorTagNames.CONFIG_PROPERTY);
            writeLocalizedDescriptions(configNode, cfg);
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_NAME, cfg.getName());
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_TYPE, cfg.getType());
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_VALUE, cfg.getValue());
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_IGNORE, String.valueOf(cfg.isIgnore()));
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_SUPPORTS_DYNAMIC_UPDATES,
                String.valueOf(cfg.isSupportsDynamicUpdates()));
            appendTextChild(configNode, ConnectorTagNames.CONFIG_PROPERTY_CONFIDENTIAL,
                String.valueOf(cfg.isConfidential()));
        }
        return parent;
    }
}

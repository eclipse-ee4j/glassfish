/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.JMSConnectionFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.JMSDestinationDefinitionDescriptor;
import com.sun.enterprise.deployment.MailSessionDescriptor;
import com.sun.enterprise.deployment.core.ResourcePropertyDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * @author naman
 */
public class ResourcePropertyNode extends DeploymentDescriptorNode<ResourcePropertyDescriptor> {

    private ResourcePropertyDescriptor descriptor;

    @Override
    public ResourcePropertyDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourcePropertyDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.RESOURCE_PROPERTY_NAME, "setName");
        table.put(TagNames.RESOURCE_PROPERTY_VALUE, "setValue");
        return table;
    }


    /**
     * @param node
     * @param desc
     * @return node parameter
     */
    public static Node write(Node node, Descriptor desc) {
        final Properties properties;
        if (desc instanceof MailSessionDescriptor) {
            properties = ((MailSessionDescriptor) desc).getProperties();
        } else if (desc instanceof ConnectionFactoryDefinitionDescriptor) {
            properties = ((ConnectionFactoryDefinitionDescriptor) desc).getProperties();
        } else if (desc instanceof DataSourceDefinitionDescriptor) {
            properties = ((DataSourceDefinitionDescriptor) desc).getProperties();
        } else if (desc instanceof JMSConnectionFactoryDefinitionDescriptor) {
            properties = ((JMSConnectionFactoryDefinitionDescriptor) desc).getProperties();
        } else if (desc instanceof JMSDestinationDefinitionDescriptor) {
            properties = ((JMSDestinationDefinitionDescriptor) desc).getProperties();
        } else {
            properties = null;
        }

        if (properties != null) {
            Set<String> keys = properties.stringPropertyNames();
            for (String name : keys) {
                String value = (String) properties.get(name);
                Node propertyNode = appendChild(node, TagNames.RESOURCE_PROPERTY);
                appendTextChild(propertyNode, TagNames.RESOURCE_PROPERTY_NAME, name);
                appendTextChild(propertyNode, TagNames.RESOURCE_PROPERTY_VALUE, value);
            }
        }
        return node;
    }
}

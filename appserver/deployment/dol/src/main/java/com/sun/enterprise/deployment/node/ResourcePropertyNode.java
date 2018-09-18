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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: naman
 * Date: 7/9/12
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourcePropertyNode extends DeploymentDescriptorNode<ResourcePropertyDescriptor> {

    private ResourcePropertyDescriptor descriptor = null;

    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(TagNames.RESOURCE_PROPERTY_NAME, "setName");
        table.put(TagNames.RESOURCE_PROPERTY_VALUE, "setValue");
        return table;
    }

    public Node writeDescriptor(Node node, Descriptor desc) {

        Properties properties = null;

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
        }

        if (properties != null) {
            Set keys = properties.keySet();

            for (Object key : keys) {
                String name = (String) key;
                String value = (String) properties.get(name);
                Node propertyNode = appendChild(node, TagNames.RESOURCE_PROPERTY);
                appendTextChild(propertyNode, TagNames.RESOURCE_PROPERTY_NAME, name);
                appendTextChild(propertyNode, TagNames.RESOURCE_PROPERTY_VALUE, value);
            }
        }
        return node;
    }


    public ResourcePropertyDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourcePropertyDescriptor();
        }
        return descriptor;
    }
}

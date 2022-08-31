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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Enumeration;
import java.util.Properties;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling property (name, value)
 * DTD elements to java.util.Properties mapping
 *
 * @author Jerome Dochez
 */
public class PropertiesNode extends DeploymentDescriptorNode<Descriptor> {

    private String name;
    private final Properties properties = new Properties();

    @Override
    public Descriptor getDescriptor() {
        return null;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.NAME_VALUE_PAIR_NAME.equals(element.getQName())) {
            name = value;
        } else if (TagNames.NAME_VALUE_PAIR_VALUE.equals(element.getQName())) {
            properties.put(name, value);
        }
    }


    public Node write(Node parent, String nodeName, Properties properties) {
        Node propertiesNode = super.appendChild(parent, nodeName);
        for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements();) {
            Node aProperty = DeploymentDescriptorNode.appendChild(propertiesNode, RuntimeTagNames.PROPERTY);
            String key = (String) keys.nextElement();
            appendTextChild(aProperty, TagNames.NAME_VALUE_PAIR_NAME, key);
            appendTextChild(aProperty, TagNames.NAME_VALUE_PAIR_VALUE, properties.getProperty(key));
        }
        return propertiesNode;
    }
}

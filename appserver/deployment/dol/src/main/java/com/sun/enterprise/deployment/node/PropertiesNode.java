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

/*
 * PropertiesNode.java
 *
 * Created on March 24, 2003, 12:39 PM
 */

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

import java.util.Enumeration;
import java.util.Properties;

/**
 * This node is responsible for handling property (name, value)
 * DTD elements to java.util.Properties mapping
 *
 * @author  Jerome Dochez
 */
public class PropertiesNode extends DeploymentDescriptorNode {

    private String name=null;
    private Properties descriptor= new Properties();

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        return descriptor;
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.NAME_VALUE_PAIR_NAME.equals(element.getQName())) {
            name = value;
        } else if (TagNames.NAME_VALUE_PAIR_VALUE.equals(element.getQName())) {
            descriptor.put(name, value);
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, Properties descriptor) {

        Node propertiesNode = super.appendChild(parent, nodeName);
        for (Enumeration keys = descriptor.propertyNames(); keys.hasMoreElements();) {
            Node aProperty = this.appendChild(propertiesNode, RuntimeTagNames.PROPERTY);
            String key = (String) keys.nextElement();
            appendTextChild(aProperty, TagNames.NAME_VALUE_PAIR_NAME, key);
            appendTextChild(aProperty, TagNames.NAME_VALUE_PAIR_VALUE,
                        descriptor.getProperty(key));
        }
        return propertiesNode;
    }
}

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

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import org.glassfish.web.deployment.runtime.WebProperty;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
* node for web property tag
*
* @author Jerome Dochez
*/
public class WebPropertyNode extends RuntimeDescriptorNode<WebProperty> {

    protected WebProperty descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public WebProperty getDescriptor() {
        if (descriptor==null) {
            descriptor = new WebProperty();
        }
        return descriptor;
    }

    /**
     * parsed an attribute of an element
     *
     * @param elementName the element name
     * @param attributeName the attribute name
     * @param value the attribute value
     * @return true if the attribute was processed
     */
    @Override
    protected boolean setAttributeValue(XMLElement elementName, XMLElement attributeName, String value) {
        RuntimeDescriptor descriptor = getDescriptor();
        if (attributeName.getQName().equals(RuntimeTagNames.NAME)) {
            descriptor.setAttributeValue(WebProperty.NAME, value);
            return true;
        } else if (attributeName.getQName().equals(RuntimeTagNames.VALUE)) {
            descriptor.setAttributeValue(WebProperty.VALUE, value);
            return true;
        }
        return false;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param property the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName,
        WebProperty property) {
        Element propertyElement = (Element) super.writeDescriptor(parent, nodeName, property);

        // description?
        appendTextChild(propertyElement, TagNames.DESCRIPTION, property.getDescription());

        setAttribute(propertyElement, RuntimeTagNames.NAME, property.getAttributeValue(WebProperty.NAME));
        setAttribute(propertyElement, RuntimeTagNames.VALUE, property.getAttributeValue(WebProperty.VALUE));
        return propertyElement;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the array of descriptors to write
     */
    public void writeDescriptor(Node parent, String nodeName, WebProperty[] properties) {
        if (properties == null) {
            return;
        }
        for (int i = 0; i < properties.length; i++) {
            writeDescriptor(parent, nodeName, properties[i]);
        }
    }
}

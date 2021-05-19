/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.web.deployment.runtime.DefaultHelper;
import org.glassfish.web.deployment.runtime.WebProperty;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
* node for default-helper tag
*
* @author Amy Roh
*/
public class DefaultHelperNode extends RuntimeDescriptorNode<DefaultHelper> {

    public DefaultHelperNode() {

        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY),
                               WebPropertyNode.class, "addWebProperty");
    }

    protected DefaultHelper descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public DefaultHelper getDescriptor() {
        if (descriptor==null) {
            descriptor = new DefaultHelper();
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
            descriptor.setAttributeValue(DefaultHelper.NAME, value);
            return true;
        }
        return false;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, DefaultHelper descriptor) {
        Element defaultHelper = (Element) super.writeDescriptor(parent, nodeName, descriptor);

        // property*
        WebProperty[] properties = descriptor.getWebProperty();
        if (properties.length>0) {
            WebPropertyNode wpn = new WebPropertyNode();
            wpn.writeDescriptor(defaultHelper, RuntimeTagNames.PROPERTY, properties);
        }

        // name, class-name attribute
        setAttribute(defaultHelper, RuntimeTagNames.NAME, (String) descriptor.getAttributeValue(DefaultHelper.NAME));

        return defaultHelper;
    }

}

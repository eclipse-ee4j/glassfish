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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;

import org.glassfish.web.deployment.descriptor.AbsoluteOrderingDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This node is responsible for handling the absolute-ordering xml tree
 *
 * @author  Shing Wai Chan
 * @version
 */
public class AbsoluteOrderingNode extends DeploymentDescriptorNode<AbsoluteOrderingDescriptor> {

    public final static XMLElement tag = new XMLElement(WebTagNames.ABSOLUTE_ORDERING);

    protected AbsoluteOrderingDescriptor descriptor = null;

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public AbsoluteOrderingDescriptor getDescriptor() {
        if (descriptor==null) {
            descriptor = new AbsoluteOrderingDescriptor();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebTagNames.COMMON_NAME, "addName");
        return table;
    }

    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        super.startElement(element, attributes);

        if (WebTagNames.OTHERS.equals(element.getQName())) {
            descriptor.addOthers();
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, AbsoluteOrderingDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        for (Object obj : descriptor.getOrdering()) {
            if (obj instanceof String) {
                appendTextChild(myNode, WebTagNames.COMMON_NAME, (String)obj);
            } else { // others
                appendChild(myNode, WebTagNames.OTHERS);
            }
        }
        return myNode;
    }
}

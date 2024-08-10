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
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.web.deployment.runtime.Valve;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Node representing a valve tag.
 */
public class ValveNode extends RuntimeDescriptorNode<Valve> {

    public ValveNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY),
                WebPropertyNode.class, "addWebProperty");
    }

    protected Valve descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Valve getDescriptor() {
        if (descriptor==null) {
            descriptor = new Valve();
        }
        return descriptor;
    }

    @Override
    protected boolean setAttributeValue(XMLElement elementName,
                                        XMLElement attributeName,
                                        String value) {
        Valve descriptor = getDescriptor();
        if (attributeName.getQName().equals(RuntimeTagNames.NAME)) {
            descriptor.setAttributeValue(Valve.NAME, value);
            return true;
        } else if (attributeName.getQName().equals(RuntimeTagNames.CLASS_NAME)) {
            descriptor.setAttributeValue(Valve.CLASS_NAME, value);
            return true;
        }
        return false;
    }

    /**
     * Writes the descriptor class to a DOM tree and returns it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName,
                                Valve descriptor) {

        Element valve = (Element) super.writeDescriptor(
            parent, nodeName, descriptor);

        WebPropertyNode wpn = new WebPropertyNode();

        // sub-element description?
        appendTextChild(valve, RuntimeTagNames.DESCRIPTION, descriptor.getDescription());

        // sub-element property*
        wpn.writeDescriptor(valve, RuntimeTagNames.PROPERTY,
                            descriptor.getWebProperty());

        // attributes classname and name
        setAttribute(valve, RuntimeTagNames.NAME, descriptor.getAttributeValue(Valve.NAME));
        setAttribute(valve, RuntimeTagNames.CLASS_NAME, descriptor.getAttributeValue(Valve.CLASS_NAME));

        return valve;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the array of descriptors to write
     */
    public void writeDescriptor(Node parent, String nodeName, Valve[] valves) {
        if (valves == null) {
            return;
        }
        for (int i = 0; i < valves.length; i++) {
            writeDescriptor(parent, nodeName, valves[i]);
        }
    }
}

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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.security.common.Group;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;


/**
 * This node handles the group definition in the runtime DDs
 *
 * @author Jerome Dochez
 * @version
 */
public class GroupNode extends DeploymentDescriptorNode {

    Group group = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        return group;
    }


    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (RuntimeTagNames.GROUP.equals(element.getQName())) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (RuntimeTagNames.NAME.equals(attributes.getQName(i))) {
                    group = new Group(attributes.getValue(i));
                }
            }
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, Group descriptor) {
        Element principal = appendChild(parent, nodeName);
        setAttribute(principal, RuntimeTagNames.NAME, descriptor.getName());
        return principal;
    }
}

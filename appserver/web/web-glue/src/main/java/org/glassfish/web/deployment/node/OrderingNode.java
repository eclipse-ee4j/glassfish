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

import org.glassfish.web.deployment.descriptor.OrderingDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling the ordering xml tree.
 *
 * @author  Shing Wai Chan
 * @version
 */
public class OrderingNode extends DeploymentDescriptorNode {
    public final static XMLElement tag = new XMLElement(WebTagNames.ORDERING);

    protected OrderingDescriptor descriptor = null;

    public OrderingNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.AFTER), OrderingOrderingNode.class, "setAfter");
        registerElementHandler(new XMLElement(WebTagNames.BEFORE), OrderingOrderingNode.class, "setBefore");
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public OrderingDescriptor getDescriptor() {
        if (descriptor==null) {
            descriptor = new OrderingDescriptor();
        }
        return descriptor;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, OrderingDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        if (descriptor.getAfter() != null) {
            OrderingOrderingNode afterNode = new OrderingOrderingNode();
            afterNode.writeDescriptor(myNode, WebTagNames.AFTER, descriptor.getAfter());
        }
        if (descriptor.getBefore() != null) {
            OrderingOrderingNode beforeNode = new OrderingOrderingNode();
            beforeNode.writeDescriptor(myNode, WebTagNames.BEFORE, descriptor.getBefore());
        }
        return myNode;
    }
}

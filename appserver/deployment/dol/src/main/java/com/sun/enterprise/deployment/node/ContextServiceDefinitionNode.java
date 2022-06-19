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
package com.sun.enterprise.deployment.node;

import static com.sun.enterprise.deployment.xml.TagNames.RESOURCE_PROPERTY;

import java.util.Map;

import org.omnifaces.concurrent.deployment.ContextServiceDefinitionDescriptor;
import org.omnifaces.concurrent.node.ContextServiceDefinitionNodeDelegate;
import org.w3c.dom.Node;

public class ContextServiceDefinitionNode extends DeploymentDescriptorNode<ContextServiceDefinitionDescriptor> {
    public final static XMLElement tag = new XMLElement(ContextServiceDefinitionNodeDelegate.getQname());

    ContextServiceDefinitionNodeDelegate delegate = new ContextServiceDefinitionNodeDelegate();

    public ContextServiceDefinitionNode() {
        registerElementHandler(
            new XMLElement(RESOURCE_PROPERTY),
            ResourcePropertyNode.class,
            delegate.getHandlerAdMethodName());
    }

    @Override
    protected Map<String, String> getDispatchTable() {
        return delegate.getDispatchTable(super.getDispatchTable());
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ContextServiceDefinitionDescriptor contextServiceDefinitionDescriptor) {
        Node node = delegate.getDescriptor(parent, nodeName, contextServiceDefinitionDescriptor);
        new ResourcePropertyNode().writeDescriptor(node, contextServiceDefinitionDescriptor);

        return node;
    }

    @Override
    public ContextServiceDefinitionDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }
}
/*
 * Copyright (c) 2022, 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.util.Map;

import org.omnifaces.concurrent.deployment.ManagedThreadFactoryDefinitionDescriptor;
import org.omnifaces.concurrent.node.ManagedThreadFactoryDefinitionNodeDelegate;
import org.w3c.dom.Node;

import static com.sun.enterprise.deployment.xml.TagNames.RESOURCE_PROPERTY;

public class ManagedThreadFactoryDefinitionNode
    extends DeploymentDescriptorNode<ManagedThreadFactoryDefinitionDescriptor> {

    private final ManagedThreadFactoryDefinitionNodeDelegate delegate = new ManagedThreadFactoryDefinitionNodeDelegate();

    public ManagedThreadFactoryDefinitionNode() {
        registerElementHandler(new XMLElement(RESOURCE_PROPERTY), ResourcePropertyNode.class,
            delegate.getHandlerAdMethodName());
    }


    @Override
    public ManagedThreadFactoryDefinitionDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        return delegate.getDispatchTable(super.getDispatchTable());
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, ManagedThreadFactoryDefinitionDescriptor descriptor) {
        Node node = delegate.getDescriptor(parent, nodeName, descriptor);
        return ResourcePropertyNode.write(node, descriptor);
    }
}

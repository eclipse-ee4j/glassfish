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

package org.glassfish.ejb.deployment.node.runtime;

import java.util.Iterator;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.List;

import org.glassfish.ejb.deployment.descriptor.runtime.PrefetchDisabledDescriptor;
import org.w3c.dom.Node;

/**
 * This node handles the prefetch-disabled runtime deployment descriptors
 */
public class PrefetchDisabledNode extends DeploymentDescriptorNode<PrefetchDisabledDescriptor> {

    private PrefetchDisabledDescriptor descriptor;

    public PrefetchDisabledNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.QUERY_METHOD), MethodNode.class);
    }


    @Override
    public PrefetchDisabledDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new PrefetchDisabledDescriptor();
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof EjbDescriptor) {
                descriptor.setEjbDescriptor((EjbDescriptor) parentDesc);
            }
        }
        return descriptor;
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof MethodDescriptor) {
            descriptor.addMethodDescriptor((MethodDescriptor) newDescriptor);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, PrefetchDisabledDescriptor prefetchDisabledDescriptor) {
        Node prefetchDisabledNode = super.writeDescriptor(parent, nodeName, prefetchDisabledDescriptor);
        List<MethodDescriptor> methodDescs = prefetchDisabledDescriptor.getConvertedMethodDescs();
        if (!methodDescs.isEmpty()) {
            MethodNode methodNode = new MethodNode();
            for (Iterator methodIterator = methodDescs.iterator(); methodIterator.hasNext();) {
                MethodDescriptor methodDesc = (MethodDescriptor) methodIterator.next();
                methodNode.writeQueryMethodDescriptor(prefetchDisabledNode, RuntimeTagNames.QUERY_METHOD, methodDesc);
            }
        }

        return prefetchDisabledNode;
    }
}

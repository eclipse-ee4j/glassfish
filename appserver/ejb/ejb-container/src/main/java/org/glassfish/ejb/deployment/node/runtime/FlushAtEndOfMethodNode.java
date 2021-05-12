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

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.ejb.deployment.descriptor.runtime.FlushAtEndOfMethodDescriptor;
import org.w3c.dom.Node;

/**
 * This node handles the flush-at-end-of-method runtime deployment descriptors
 *
 */
public class FlushAtEndOfMethodNode extends DeploymentDescriptorNode<FlushAtEndOfMethodDescriptor> {

    private FlushAtEndOfMethodDescriptor descriptor;

    public FlushAtEndOfMethodNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.METHOD), MethodNode.class);
    }


    @Override
    public FlushAtEndOfMethodDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new FlushAtEndOfMethodDescriptor();
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
    public Node writeDescriptor(Node parent, String nodeName, FlushAtEndOfMethodDescriptor flushMethodDescriptor) {
        Node flushMethodNode = super.writeDescriptor(parent, nodeName, flushMethodDescriptor);
        ArrayList methodDescs = flushMethodDescriptor.getConvertedMethodDescs();
        if (!methodDescs.isEmpty()) {
            MethodNode methodNode = new MethodNode();
            for (Iterator methodIterator = methodDescs.iterator(); methodIterator.hasNext();) {
                MethodDescriptor methodDesc = (MethodDescriptor) methodIterator.next();
                // do not write out ejb-name element for the method
                methodNode.writeDescriptor(flushMethodNode, RuntimeTagNames.METHOD, methodDesc, null);
            }
        }

        return flushMethodNode;
    }
}

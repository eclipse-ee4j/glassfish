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

package org.glassfish.ejb.deployment.node;


import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.ConcurrentMethodDescriptor;
import org.glassfish.ejb.deployment.descriptor.TimeoutValueDescriptor;
import org.w3c.dom.Node;

public class ConcurrentMethodNode extends DeploymentDescriptorNode<ConcurrentMethodDescriptor> {

    private static final String WRITE_LOCK = "Write";

    private ConcurrentMethodDescriptor descriptor = null;

    public ConcurrentMethodNode() {
        super();

        registerElementHandler(new XMLElement(EjbTagNames.METHOD), MethodNode.class,
                "setConcurrentMethod");
        registerElementHandler(new XMLElement(EjbTagNames.CONCURRENT_ACCESS_TIMEOUT),
                TimeoutValueNode.class, "setAccessTimeout");

    }

    @Override
    public ConcurrentMethodDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new ConcurrentMethodDescriptor();
        return descriptor;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (EjbTagNames.CONCURRENT_LOCK.equals(element.getQName())) {
            descriptor.setWriteLock(value.equals(WRITE_LOCK));
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ConcurrentMethodDescriptor desc) {
        Node concurrentNode = super.writeDescriptor(parent, nodeName, descriptor);

        MethodNode methodNode = new MethodNode();

        methodNode.writeJavaMethodDescriptor(concurrentNode, EjbTagNames.METHOD,
                desc.getConcurrentMethod());

        if( desc.hasLockMetadata() ) {
            String lockType = desc.isWriteLocked() ? "Write" : "Read";
            appendTextChild(concurrentNode, EjbTagNames.CONCURRENT_LOCK, lockType);
        }

        if( desc.hasAccessTimeout() ) {
            TimeoutValueNode timeoutValueNode = new TimeoutValueNode();
            TimeoutValueDescriptor timeoutDesc = new TimeoutValueDescriptor();
            timeoutDesc.setValue(desc.getAccessTimeoutValue());
            timeoutDesc.setUnit(desc.getAccessTimeoutUnit());
            timeoutValueNode.writeDescriptor(concurrentNode, EjbTagNames.CONCURRENT_ACCESS_TIMEOUT,
                timeoutDesc);
        }

        return concurrentNode;
     }

}

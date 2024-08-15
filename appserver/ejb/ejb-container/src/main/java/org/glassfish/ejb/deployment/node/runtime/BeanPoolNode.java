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

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node handles the bean-pool runtime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class BeanPoolNode extends DeploymentDescriptorNode<BeanPoolDescriptor> {

    private BeanPoolDescriptor descriptor;

    @Override
    public BeanPoolDescriptor getDescriptor() {
        if (descriptor==null) descriptor = new BeanPoolDescriptor();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map dispatchTable = super.getDispatchTable();
        dispatchTable.put(RuntimeTagNames.STEADY_POOL_SIZE, "setSteadyPoolSize");
        dispatchTable.put(RuntimeTagNames.POOL_RESIZE_QUANTITY, "setPoolResizeQuantity");
        dispatchTable.put(RuntimeTagNames.MAX_POOL_SIZE, "setMaxPoolSize");
        dispatchTable.put(RuntimeTagNames.POOL_IDLE_TIMEOUT_IN_SECONDS, "setPoolIdleTimeoutInSeconds");
        dispatchTable.put(RuntimeTagNames.MAX_WAIT_TIME_IN_MILLIS, "setMaxWaitTimeInMillis");
        return dispatchTable;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, BeanPoolDescriptor descriptor) {
        Node beanpoolNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(beanpoolNode, RuntimeTagNames.STEADY_POOL_SIZE, descriptor.getSteadyPoolSize());
        appendTextChild(beanpoolNode, RuntimeTagNames.POOL_RESIZE_QUANTITY, descriptor.getPoolResizeQuantity());
        appendTextChild(beanpoolNode, RuntimeTagNames.MAX_POOL_SIZE, descriptor.getMaxPoolSize());
        appendTextChild(beanpoolNode, RuntimeTagNames.POOL_IDLE_TIMEOUT_IN_SECONDS, descriptor.getPoolIdleTimeoutInSeconds());
        appendTextChild(beanpoolNode, RuntimeTagNames.MAX_WAIT_TIME_IN_MILLIS, descriptor.getMaxWaitTimeInMillis());
        return beanpoolNode;
    }
}

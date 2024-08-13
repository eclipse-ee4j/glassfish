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
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.glassfish.ejb.deployment.descriptor.runtime.BeanCacheDescriptor;
import org.w3c.dom.Node;

/**
 * This node handles the bean-cache untime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class BeanCacheNode extends DeploymentDescriptorNode<BeanCacheDescriptor> {

    private BeanCacheDescriptor descriptor;

    @Override
    public BeanCacheDescriptor getDescriptor() {
        if (descriptor==null) descriptor = new BeanCacheDescriptor();
        return descriptor;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
    if (RuntimeTagNames.IS_CACHE_OVERFLOW_ALLOWED.equals(element.getQName())) {
        descriptor.setIsCacheOverflowAllowed(Boolean.valueOf(value));
    } else
        super.setElementValue(element, value);
    }

    @Override
    protected Map getDispatchTable() {
    Map dispatchTable = super.getDispatchTable();
    dispatchTable.put(RuntimeTagNames.MAX_CACHE_SIZE, "setMaxCacheSize");
    dispatchTable.put(RuntimeTagNames.RESIZE_QUANTITY, "setResizeQuantity");
    dispatchTable.put(RuntimeTagNames.CACHE_IDLE_TIMEOUT_IN_SECONDS, "setCacheIdleTimeoutInSeconds");
    dispatchTable.put(RuntimeTagNames.REMOVAL_TIMEOUT_IN_SECONDS, "setRemovalTimeoutInSeconds");
    dispatchTable.put(RuntimeTagNames.VICTIM_SELECTION_POLICY, "setVictimSelectionPolicy");
    return dispatchTable;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, BeanCacheDescriptor descriptor) {
    Node beanCacheNode = super.writeDescriptor(parent, nodeName, descriptor);
    appendTextChild(beanCacheNode, RuntimeTagNames.MAX_CACHE_SIZE, descriptor.getMaxCacheSize());
    appendTextChild(beanCacheNode, RuntimeTagNames.RESIZE_QUANTITY, descriptor.getResizeQuantity());
    appendTextChild(beanCacheNode, RuntimeTagNames.IS_CACHE_OVERFLOW_ALLOWED, String.valueOf(descriptor.isIsCacheOverflowAllowed()));
    appendTextChild(beanCacheNode, RuntimeTagNames.CACHE_IDLE_TIMEOUT_IN_SECONDS, descriptor.getCacheIdleTimeoutInSeconds());
    appendTextChild(beanCacheNode, RuntimeTagNames.REMOVAL_TIMEOUT_IN_SECONDS, descriptor.getRemovalTimeoutInSeconds());
    appendTextChild(beanCacheNode, RuntimeTagNames.VICTIM_SELECTION_POLICY, descriptor.getVictimSelectionPolicy());
    return beanCacheNode;
    }
}

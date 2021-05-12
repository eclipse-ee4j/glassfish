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
import java.util.Vector;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.runtime.IASPersistenceManagerDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.PersistenceManagerInUse;
import org.w3c.dom.Node;

/**
 * This node handles the pm-descriptors runtime xml element
 *
 * @author  Jerome Dochez
 * @version
 */
public class PMDescriptorsNode extends RuntimeDescriptorNode {

    public PMDescriptorsNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.PM_DESCRIPTOR), PMDescriptorNode.class, "addPersistenceManager");
        registerElementHandler(new XMLElement(RuntimeTagNames.PM_INUSE), PMInUseNode.class, "setPersistenceManagerInUse");
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, EjbBundleDescriptorImpl descriptor) {
        Node pms = null;
        Vector pmDescriptors = descriptor.getPersistenceManagers();
        if (pmDescriptors != null && !pmDescriptors.isEmpty()) {
            pms = super.writeDescriptor(parent, nodeName, descriptor);
            PMDescriptorNode pmNode = new PMDescriptorNode();

            for (Iterator pmIterator = pmDescriptors.iterator(); pmIterator.hasNext();) {
                IASPersistenceManagerDescriptor pmDescriptor = (IASPersistenceManagerDescriptor) pmIterator.next();
                pmNode.writeDescriptor(pms, RuntimeTagNames.PM_DESCRIPTOR, pmDescriptor);
            }
            PersistenceManagerInUse inUse = descriptor.getPersistenceManagerInUse();
            if (inUse != null) {
                PMInUseNode inUseNode = new PMInUseNode();
                inUseNode.writeDescriptor(pms, RuntimeTagNames.PM_INUSE, inUse);
            }
        }
        return pms;
    }
}

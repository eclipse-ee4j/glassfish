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
import java.util.Map;
import java.util.logging.Level;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbCMPFinder;
import org.glassfish.ejb.deployment.descriptor.runtime.PrefetchDisabledDescriptor;
import org.w3c.dom.Node;

/**
 * This node handles the cmp runtime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class CmpNode extends DeploymentDescriptorNode<IASEjbCMPEntityDescriptor> {

    protected IASEjbCMPEntityDescriptor descriptor;

    public CmpNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.FINDER), FinderNode.class);
        registerElementHandler(new XMLElement(RuntimeTagNames.PREFETCH_DISABLED), PrefetchDisabledNode.class);
    }

    @Override
    public IASEjbCMPEntityDescriptor getDescriptor() {
        if (descriptor == null) {
            Object desc = getParentNode().getDescriptor();
            if (desc instanceof IASEjbCMPEntityDescriptor) {
                descriptor = (IASEjbCMPEntityDescriptor) desc;
            }
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map dispatchTable = super.getDispatchTable();
        dispatchTable.put(RuntimeTagNames.MAPPING_PROPERTIES, "setMappingProperties");
        // deprecated element, will be ignored at reading
        dispatchTable.put(RuntimeTagNames.IS_ONE_ONE_CMP, null);
        return dispatchTable;
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        getDescriptor();
        if (descriptor == null) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, "enterprise.deployment.backend.addDescriptorFailure",
                new Object[] {newDescriptor, this});
            return;
        }
        if (newDescriptor instanceof IASEjbCMPFinder ) {
            descriptor.addOneOneFinder((IASEjbCMPFinder) newDescriptor);
        } else if (newDescriptor instanceof PrefetchDisabledDescriptor) {
            descriptor.setPrefetchDisabledDescriptor((PrefetchDisabledDescriptor)newDescriptor);
        } else {
            super.addDescriptor(descriptor);
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, IASEjbCMPEntityDescriptor ejbDescriptor) {
        Node cmpNode = super.writeDescriptor(parent, nodeName, ejbDescriptor);
        appendTextChild(cmpNode, RuntimeTagNames.MAPPING_PROPERTIES, ejbDescriptor.getMappingProperties());
        Map finders = ejbDescriptor.getOneOneFinders();
        if (!finders.isEmpty()) {
            Node findersNode = appendChild(cmpNode, RuntimeTagNames.ONE_ONE_FINDERS);
            FinderNode fn = new FinderNode();
            for (Iterator finderIterator = finders.values().iterator();finderIterator.hasNext();) {
                IASEjbCMPFinder aFinder = (IASEjbCMPFinder) finderIterator.next();
                fn.writeDescriptor(findersNode, RuntimeTagNames.FINDER, aFinder);
            }
        }

        // prefetch-disabled
        PrefetchDisabledDescriptor prefetchDisabledDesc =  ejbDescriptor.getPrefetchDisabledDescriptor();
        if (prefetchDisabledDesc != null) {
            PrefetchDisabledNode prefetchDisabledNode = new PrefetchDisabledNode();
            prefetchDisabledNode.writeDescriptor(cmpNode, RuntimeTagNames.PREFETCH_DISABLED, prefetchDisabledDesc);
        }

        return cmpNode;
    }
}

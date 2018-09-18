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

import java.util.HashMap;
import java.util.Map;

import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.ejb.deployment.descriptor.runtime.IASPersistenceManagerDescriptor;
import org.w3c.dom.Node;

/**
 * This node handles the pm-descriptor runtime xml element
 *
 * @author  Jerome Dochez
 * @version 
 */

public class PMDescriptorNode extends RuntimeDescriptorNode<IASPersistenceManagerDescriptor> {

    private IASPersistenceManagerDescriptor descriptor;

    @Override
    public IASPersistenceManagerDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new IASPersistenceManagerDescriptor();
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {    
        Map table = new HashMap();
        table.put(RuntimeTagNames.PM_IDENTIFIER, "setPersistenceManagerIdentifier");
        table.put(RuntimeTagNames.PM_VERSION, "setPersistenceManagerVersion");
        table.put(RuntimeTagNames.PM_CONFIG, "setPersistenceManagerConfig");
        table.put(RuntimeTagNames.PM_CLASS_GENERATOR, "setPersistenceManagerClassGenerator");
        table.put(RuntimeTagNames.PM_MAPPING_FACTORY, "setPersistenceManagerMappingFactory");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, IASPersistenceManagerDescriptor descriptor) {
	Node pd = super.writeDescriptor(parent, nodeName, descriptor);
	appendTextChild(pd, RuntimeTagNames.PM_IDENTIFIER, descriptor.getPersistenceManagerIdentifier());
	appendTextChild(pd, RuntimeTagNames.PM_VERSION, descriptor.getPersistenceManagerVersion());
	appendTextChild(pd, RuntimeTagNames.PM_CONFIG, descriptor.getPersistenceManagerConfig());
	appendTextChild(pd, RuntimeTagNames.PM_CLASS_GENERATOR, descriptor.getPersistenceManagerClassGenerator());
	appendTextChild(pd, RuntimeTagNames.PM_MAPPING_FACTORY, descriptor.getPersistenceManagerMappingFactory());
        return pd;
    }
}

/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.ejb.deployment.descriptor.runtime.PersistenceManagerInUse;
import org.w3c.dom.Node;

/**
 * This node handles the pm-inuse runtime xml element
 *
 * @author Jerome Dochez
 */
@Deprecated(forRemoval = true, since = "3.1")
public class PMInUseNode extends RuntimeDescriptorNode<PersistenceManagerInUse> {

    private PersistenceManagerInUse descriptor;

    @Override
    public PersistenceManagerInUse getDescriptor() {
        if (descriptor == null) {
            descriptor = new PersistenceManagerInUse();
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map<String, String> table = new HashMap<>();
        table.put(RuntimeTagNames.PM_IDENTIFIER, "set_pm_identifier");
        table.put(RuntimeTagNames.PM_VERSION, "set_pm_version");
        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, PersistenceManagerInUse descriptor) {
        Node pmInUse = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(pmInUse, RuntimeTagNames.PM_IDENTIFIER, descriptor.get_pm_identifier());
        appendTextChild(pmInUse, RuntimeTagNames.PM_VERSION, descriptor.get_pm_version());
        return pmInUse;
    }
}

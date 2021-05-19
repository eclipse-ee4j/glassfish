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

/*
 * RunAsNode.java
 *
 * Created on January 30, 2002, 9:23 AM
 */

package com.sun.enterprise.deployment.node;

import java.util.Map;

import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

/**
 * This class handles the run-as xml fragment
 *
 * @author  Jerome Dochez
 * @version
 */
public class RunAsNode extends DeploymentDescriptorNode <RunAsIdentityDescriptor>{

    private RunAsIdentityDescriptor descriptor;

    @Override
    public RunAsIdentityDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new RunAsIdentityDescriptor();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(TagNames.ROLE_NAME, "setRoleName");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, RunAsIdentityDescriptor descriptor) {
        Node subNode = super.writeDescriptor(parent, nodeName, descriptor);
        writeLocalizedDescriptions(subNode, descriptor);
        appendTextChild(subNode, TagNames.ROLE_NAME, descriptor.getRoleName());
        return subNode;
    }

}

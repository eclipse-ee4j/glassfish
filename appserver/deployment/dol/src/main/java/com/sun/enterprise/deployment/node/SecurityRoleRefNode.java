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
 * SecurityRoleRefNode.java
 *
 * Created on January 24, 2002, 4:57 PM
 */

package com.sun.enterprise.deployment.node;

import java.util.Map;

import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

/**
 * This class handles the DD xml security-role-ref tag
 *
 * @author  Jerome Dochez
 * @version 
 */
public class SecurityRoleRefNode extends DeploymentDescriptorNode<RoleReference> {

    private RoleReference descriptor;

    @Override
    public RoleReference getDescriptor() {
        if (descriptor == null) descriptor = new RoleReference();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(TagNames.ROLE_NAME, "setName");
        table.put(TagNames.ROLE_LINK, "setValue");            
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, RoleReference roleRef) {
        Node subNode = super.writeDescriptor(parent, nodeName, roleRef);

        writeLocalizedDescriptions(subNode, roleRef);        

        appendTextChild(subNode, TagNames.ROLE_NAME, roleRef.getName());                   
        appendTextChild(subNode, TagNames.ROLE_LINK, roleRef.getValue());                  
        return subNode;
    }
}

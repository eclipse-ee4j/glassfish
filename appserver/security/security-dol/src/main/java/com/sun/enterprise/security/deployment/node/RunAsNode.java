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

package com.sun.enterprise.security.deployment.node;

import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.xml.EjbTagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This class handles the run-as xml fragment
 *
 * @author  Jer ome Dochez
 * @version 
 */
public class RunAsNode extends DeploymentDescriptorNode {

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */    
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.ROLE_NAME, "setRoleName");    
        return table;
    }    

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree 
     * @param node name for the root element for this DOM tree fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, RunAsIdentityDescriptor descriptor) {    
        Node subNode = super.writeDescriptor(parent, nodeName, descriptor);
        writeLocalizedDescriptions(subNode, descriptor);
        appendTextChild(subNode, EjbTagNames.ROLE_NAME, descriptor.getRoleName());
        return subNode;
    }
    
}

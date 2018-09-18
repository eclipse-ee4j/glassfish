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

package com.sun.enterprise.security.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.xml.EjbTagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This class handles the DD xml security-role-ref tag
 *
 * @author  Jerome Dochez
 * @version 
 */
public class SecurityRoleRefNode extends DeploymentDescriptorNode {
    
    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value. 
     *  
     * @return the map with the element name as a key, the setter method as a value
     */    
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.ROLE_NAME, "setName");
        table.put(EjbTagNames.ROLE_LINK, "setValue");            
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
    public Node writeDescriptor(Node parent, String nodeName, Descriptor descriptor) {
        if (! (descriptor instanceof RoleReference)) {
            throw new IllegalArgumentException(getClass() + " cannot handles descriptors of type " + descriptor.getClass());
        }    
        RoleReference roleRef = (RoleReference) descriptor;        
        Node subNode = super.writeDescriptor(parent, nodeName, roleRef);

        writeLocalizedDescriptions(subNode, descriptor);        

        appendTextChild(subNode, EjbTagNames.ROLE_NAME, roleRef.getName());                   
        appendTextChild(subNode, EjbTagNames.ROLE_LINK, roleRef.getValue());                  
        return subNode;
    }
}

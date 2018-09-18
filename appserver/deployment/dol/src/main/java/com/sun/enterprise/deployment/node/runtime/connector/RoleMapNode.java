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

package com.sun.enterprise.deployment.node.runtime.connector;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.connector.MapElement;
import com.sun.enterprise.deployment.runtime.connector.RoleMap;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles the role-map runtime deployment descriptors 
 *
 * @author  Jerome Dochez
 * @version 
 */
public class RoleMapNode extends RuntimeDescriptorNode {
    
    
    public RoleMapNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.MAP_ELEMENT), 
                               MapElementNode.class, "addMapElement"); 
    }
    
    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the descriptor to write
     * @return the DOM tree top node
     */    
    public Node writeDescriptor(Node parent, String nodeName, RoleMap descriptor) {
	Element roleMapNode = (Element) super.writeDescriptor(parent, nodeName, descriptor);
	appendTextChild(roleMapNode, RuntimeTagNames.DESCRIPTION, descriptor.getDescription());
	setAttribute(roleMapNode, RuntimeTagNames.MAP_ID, (String) descriptor.getValue(RoleMap.MAP_ID));

	// map-element*
	MapElement[] maps = descriptor.getMapElement();	
	if (maps.length>0) {
	    MapElementNode men = new MapElementNode();
	    for (int i=0;i<maps.length;i++) {
		men.writeDescriptor(roleMapNode, RuntimeTagNames.MAP_ELEMENT, maps[i]);
	    }
	}
	
	return roleMapNode;
    }
}

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
 * InjectionTargetNode.java
 *
 * Created on October 18, 2005, 1:02 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node handles all information relative to the injection-target element
 *
 * @author Jerome Dochez
 */
public class InjectionTargetNode extends DeploymentDescriptorNode {

    @Override
    protected InjectionTarget createDescriptor() {
        return new InjectionTarget();
    }


    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.INJECTION_TARGET_CLASS,
            "setClassName");
        table.put(TagNames.INJECTION_TARGET_NAME,
            "setTargetName");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, InjectionTarget descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, TagNames.INJECTION_TARGET_CLASS,
            descriptor.getClassName());
        appendTextChild(myNode, TagNames.INJECTION_TARGET_NAME,
            descriptor.getTargetName());
        return myNode;
    }

}

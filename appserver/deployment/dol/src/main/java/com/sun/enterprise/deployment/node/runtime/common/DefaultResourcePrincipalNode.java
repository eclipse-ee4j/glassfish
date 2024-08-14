/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.common.DefaultResourcePrincipal;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node handles all the role mapping information
 *
 * @author Jerome Dochez
 */
public class DefaultResourcePrincipalNode extends RuntimeDescriptorNode<DefaultResourcePrincipal> {

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.NAME, "setName");
        table.put(RuntimeTagNames.PASSWORD, "setPassword");
        return table;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, DefaultResourcePrincipal descriptor) {
        Node ejbRef = appendChild(parent, nodeName);
        appendTextChild(ejbRef, RuntimeTagNames.NAME, descriptor.getName());
        appendTextChild(ejbRef, RuntimeTagNames.PASSWORD, descriptor.getPassword());
        return ejbRef;
    }
}

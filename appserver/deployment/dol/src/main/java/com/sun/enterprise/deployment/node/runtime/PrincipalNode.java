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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.glassfish.security.common.PrincipalImpl;
import org.w3c.dom.Node;

/**
 * This mode handles the principal definition in the runtine DDs
 *
 * @author Jerome Dochez
 * @version
 */
public class PrincipalNode extends DeploymentDescriptorNode {

    PrincipalImpl principal = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        return principal;
    }


    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (RuntimeTagNames.NAME.equals(element.getQName())) {
            principal = new PrincipalImpl(value);
        } else {
            super.setElementValue(element, value);
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, PrincipalImpl descriptor) {
        Node principal = appendChild(parent, nodeName);
        appendTextChild(principal, RuntimeTagNames.NAME, descriptor.getName());
        return principal;

    }
}

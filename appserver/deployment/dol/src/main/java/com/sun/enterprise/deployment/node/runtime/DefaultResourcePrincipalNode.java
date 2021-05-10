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

import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Node;

/**
 * This node handles the runtime deployment descriptor tag
 * default-resource-principal
 *
 * @author Jerome Dochez
 * @version
 */
public class DefaultResourcePrincipalNode extends DeploymentDescriptorNode {

    private String name = null;
    private String passwd = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Object getDescriptor() {
        return null;
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
            name = value;
        } else if (RuntimeTagNames.PASSWORD.equals(element.getQName())) {
            passwd = value;
        } else {
            super.setElementValue(element, value);
        }
    }


    /**
     * notification of the end of XML parsing for this node
     */
    @Override
    public void postParsing() {
        if (getParentNode().getDescriptor() instanceof ResourceReferenceDescriptor) {
            ((ResourceReferenceDescriptor) getParentNode().getDescriptor())
                .setResourcePrincipal(new ResourcePrincipal(name, passwd));
        } else {
            getParentNode().addDescriptor(new ResourcePrincipal(name, passwd));
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for the descriptor
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, ResourcePrincipal rpDescriptor) {
        Node principalNode = super.writeDescriptor(parent, nodeName, null);
        appendTextChild(principalNode, RuntimeTagNames.NAME, rpDescriptor.getName());
        appendTextChild(principalNode, RuntimeTagNames.PASSWORD, rpDescriptor.getPassword());
        return principalNode;
    }
}

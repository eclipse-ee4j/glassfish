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

package com.sun.enterprise.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.common.ProtectionDescriptor;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles request-protection and response-protection elements
 *
 */
public class ProtectionNode extends DeploymentDescriptorNode {

    ProtectionDescriptor descriptor = null;

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
       if (descriptor == null) {
            descriptor = new ProtectionDescriptor();
        }
        return descriptor;
    }


    /**
     * parsed an attribute of an element
     *
     * @param the element name
     * @param the attribute name
     * @param the attribute value
     * @return true if the attribute was processed
     */
    protected boolean setAttributeValue(XMLElement elementName,
        XMLElement attributeName, String value) {
        if (attributeName.getQName().equals(WebServicesTagNames.AUTH_SOURCE)) {
            descriptor.setAttributeValue(descriptor.AUTH_SOURCE, value);
            return true;
        } else if (attributeName.getQName().equals(
            WebServicesTagNames.AUTH_RECIPIENT)) {
            descriptor.setAttributeValue(descriptor.AUTH_RECIPIENT, value);
            return true;
        }
        return false;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param node name for
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName,
        ProtectionDescriptor protectionDesc) {
        Element protectionNode =
            (Element)super.writeDescriptor(parent, nodeName, protectionDesc);

        // auth-source
        if (protectionDesc.getAttributeValue(protectionDesc.AUTH_SOURCE)
            != null) {
            setAttribute(protectionNode, WebServicesTagNames.AUTH_SOURCE, protectionDesc.getAttributeValue(protectionDesc.AUTH_SOURCE));
        }

        // auth-recipient
        if (protectionDesc.getAttributeValue(protectionDesc.AUTH_RECIPIENT)
           != null) {
            setAttribute(protectionNode, WebServicesTagNames.AUTH_RECIPIENT, protectionDesc.getAttributeValue(protectionDesc.AUTH_RECIPIENT));
        }

        return protectionNode;
    }
}

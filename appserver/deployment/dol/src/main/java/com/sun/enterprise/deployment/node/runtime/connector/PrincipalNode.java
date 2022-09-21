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

package com.sun.enterprise.deployment.node.runtime.connector;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.runtime.connector.Principal;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles the principal runtime deployment descriptors
 *
 * @author  Jerome Dochez
 * @version
 */
public class PrincipalNode extends RuntimeDescriptorNode<RuntimeDescriptor> {

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        RuntimeDescriptor descriptor = getDescriptor();
        if (descriptor == null) {
            throw new RuntimeException("Trying to set values on a null descriptor");
        }
        if (element.getQName().equals(RuntimeTagNames.USER_NAME)) {
            descriptor.setAttributeValue(Principal.USER_NAME, value);
        } else if (element.getQName().equals(RuntimeTagNames.PASSWORD)) {
            descriptor.setAttributeValue(Principal.PASSWORD, value);
        } else if (element.getQName().equals(RuntimeTagNames.CREDENTIAL)) {
            descriptor.setAttributeValue(Principal.CREDENTIAL, value);
        } else {
            super.setElementValue(element, value);
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName name for the descriptor
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, Principal descriptor) {
        Element principalNode = (Element) super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(principalNode, TagNames.DESCRIPTION, descriptor.getDescription());
        setAttribute(principalNode, RuntimeTagNames.USER_NAME, (String) descriptor.getValue(Principal.USER_NAME));
        setAttribute(principalNode, RuntimeTagNames.PASSWORD, (String) descriptor.getValue(Principal.PASSWORD));
        setAttribute(principalNode, RuntimeTagNames.CREDENTIAL, (String) descriptor.getValue(Principal.CREDENTIAL));
        return principalNode;
    }
}

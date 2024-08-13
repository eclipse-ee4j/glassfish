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

package org.glassfish.web.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;

import org.glassfish.web.deployment.descriptor.CookieConfigDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling cookie-config xml node.
 *
 * @author Shing Wai Chan
 */
public class CookieConfigNode extends DeploymentDescriptorNode<CookieConfigDescriptor> {
    private CookieConfigDescriptor descriptor;

    public CookieConfigNode() {
        super();
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public CookieConfigDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new CookieConfigDescriptor();
        }
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(WebTagNames.COMMON_NAME, "setName");
        table.put(WebTagNames.DOMAIN, "setDomain");
        table.put(WebTagNames.PATH, "setPath");
        table.put(WebTagNames.COMMENT, "setComment");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.HTTP_ONLY.equals(element.getQName())) {
            descriptor.setHttpOnly(Boolean.parseBoolean(value));
        } else if (WebTagNames.SECURE.equals(element.getQName())) {
            descriptor.setSecure(Boolean.parseBoolean(value));
        } else if (WebTagNames.MAX_AGE.equals(element.getQName())) {
            descriptor.setMaxAge(Integer.parseInt(value));
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, CookieConfigDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        if (descriptor.getName() != null && descriptor.getName().length() > 0) {
            appendTextChild(myNode, WebTagNames.COMMON_NAME, descriptor.getName());
        }
        if (descriptor.getDomain() != null) {
            appendTextChild(myNode, WebTagNames.DOMAIN, descriptor.getDomain());
        }
        if (descriptor.getPath() != null) {
            appendTextChild(myNode, WebTagNames.PATH, descriptor.getPath());
        }
        if (descriptor.getComment() != null) {
            appendTextChild(myNode, WebTagNames.COMMENT, descriptor.getComment());
        }
        appendTextChild(myNode, WebTagNames.HTTP_ONLY, Boolean.toString(descriptor.isHttpOnly()));
        appendTextChild(myNode, WebTagNames.SECURE, Boolean.toString(descriptor.isSecure()));
        appendTextChild(myNode, WebTagNames.MAX_AGE, Integer.toString(descriptor.getMaxAge()));

        return myNode;
    }
}

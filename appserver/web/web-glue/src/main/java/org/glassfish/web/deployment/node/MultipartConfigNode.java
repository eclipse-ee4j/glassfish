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

import org.glassfish.web.deployment.descriptor.MultipartConfigDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling multipart-config xml node.
 *
 * @author Shing Wai Chan
 */
public class MultipartConfigNode extends DeploymentDescriptorNode<MultipartConfigDescriptor> {
    private MultipartConfigDescriptor descriptor;

    public MultipartConfigNode() {
        super();
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public MultipartConfigDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MultipartConfigDescriptor();
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
        table.put(WebTagNames.LOCATION, "setLocation");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.MAX_FILE_SIZE.equals(element.getQName())) {
            descriptor.setMaxFileSize(Long.valueOf(value));
        } else if (WebTagNames.MAX_REQUEST_SIZE.equals(element.getQName())) {
            descriptor.setMaxRequestSize(Long.valueOf(value));
        } else if (WebTagNames.FILE_SIZE_THRESHOLD.equals(element.getQName())) {
            descriptor.setFileSizeThreshold(Integer.valueOf(value));
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
    public Node writeDescriptor(Node parent, String nodeName, MultipartConfigDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, WebTagNames.LOCATION, descriptor.getLocation());
        if (descriptor.getMaxFileSize() != null) {
            appendTextChild(myNode, WebTagNames.MAX_FILE_SIZE, descriptor.getMaxFileSize().toString());
        }
        if (descriptor.getMaxRequestSize() != null) {
            appendTextChild(myNode, WebTagNames.MAX_REQUEST_SIZE, descriptor.getMaxRequestSize().toString());
        }
        if (descriptor.getFileSizeThreshold() != null) {
            appendTextChild(myNode, WebTagNames.FILE_SIZE_THRESHOLD, descriptor.getFileSizeThreshold().toString());
        }

        return myNode;
    }
}

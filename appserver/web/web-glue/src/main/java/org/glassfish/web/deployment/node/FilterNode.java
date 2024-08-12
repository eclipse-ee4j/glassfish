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

import com.sun.enterprise.deployment.node.DisplayableComponentNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;
import java.util.Vector;

import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling filter xml node
 *
 * @author  Jerome Dochez
 * @version
 */
public class FilterNode extends DisplayableComponentNode<ServletFilterDescriptor> {

    private ServletFilterDescriptor descriptor;

    // constructor. register sub nodes.
    public FilterNode() {
        super();
        registerElementHandler(new XMLElement(WebTagNames.INIT_PARAM),
                                                            InitParamNode.class, "addInitializationParameter");
    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public ServletFilterDescriptor getDescriptor() {

        if (descriptor==null) {
            descriptor = new ServletFilterDescriptor();
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
        table.put(WebTagNames.DISPLAY_NAME, "setDisplayName");
        table.put(WebTagNames.FILTER_NAME, "setName");
        table.put(WebTagNames.FILTER_CLASS, "setClassName");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (WebTagNames.ASYNC_SUPPORTED.equals(element.getQName())) {
            descriptor.setAsyncSupported(Boolean.valueOf(value));
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
    public Node writeDescriptor(Node parent, String nodeName, ServletFilterDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        writeDisplayableComponentInfo(myNode, descriptor);
        appendTextChild(myNode, WebTagNames.FILTER_NAME, descriptor.getName());
        appendTextChild(myNode, WebTagNames.FILTER_CLASS, descriptor.getClassName());
        if (descriptor.isAsyncSupported() != null) {
            appendTextChild(myNode, WebTagNames.ASYNC_SUPPORTED, String.valueOf(descriptor.isAsyncSupported()));
        }
        Vector initParams = descriptor.getInitializationParameters();
        if (!initParams.isEmpty()) {
            WebCommonNode.addInitParam(myNode, WebTagNames.INIT_PARAM, initParams.elements());
        }

        return myNode;
    }
}

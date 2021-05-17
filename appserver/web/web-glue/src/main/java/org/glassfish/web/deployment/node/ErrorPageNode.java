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
import org.glassfish.web.deployment.descriptor.ErrorPageDescriptor;
import org.glassfish.web.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * This node handles the error-page xml tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class ErrorPageNode extends DeploymentDescriptorNode<ErrorPageDescriptor> {

    protected ErrorPageDescriptor descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public ErrorPageDescriptor getDescriptor() {
        if (descriptor==null) {
            descriptor = new ErrorPageDescriptor();
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
        table.put(WebTagNames.ERROR_CODE, "setErrorSignifierAsString");
        table.put(WebTagNames.EXCEPTION_TYPE, "setExceptionType");
        table.put(WebTagNames.LOCATION, "setLocation");
        return table;
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
    public Node writeDescriptor(Node parent, String nodeName, ErrorPageDescriptor descriptor) {
        Node myNode = appendChild(parent, nodeName);
        String exceptionType = descriptor.getExceptionType();
        if (exceptionType!=null && exceptionType.length()!=0) {
            appendTextChild(myNode, WebTagNames.EXCEPTION_TYPE, exceptionType);
        } else {
            String errorSignifier = descriptor.getErrorSignifierAsString();
            if (errorSignifier != null) {
                appendTextChild(myNode, WebTagNames.ERROR_CODE, errorSignifier);
            }
        }
        appendTextChild(myNode, WebTagNames.LOCATION, descriptor.getLocation());
        return myNode;
    }
}

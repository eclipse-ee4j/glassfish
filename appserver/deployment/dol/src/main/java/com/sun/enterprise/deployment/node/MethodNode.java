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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This class handle the method element
 *
 * @author Jerome Dochez
 */
public class MethodNode extends DeploymentDescriptorNode<MethodDescriptor> {

    private MethodDescriptor descriptor;

    @Override
    public MethodDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new MethodDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.EJB_NAME, "setEjbName");
        table.put(TagNames.METHOD_INTF, "setEjbClassSymbol");
        table.put(TagNames.METHOD_NAME, "setName");
        table.put(TagNames.METHOD_PARAM, "addParameterClass");
        return table;
    }


    @Override
    public boolean endElement(XMLElement element) {
        String qname = element.getQName();
        if (TagNames.METHOD_PARAMS.equals(qname)) {
            MethodDescriptor desc = getDescriptor();
            // this means we have an empty method-params element
            // which means this method has no input parameter
            if (desc.getParameterClassNames() == null) {
                desc.setEmptyParameterClassNames();
            }
        }
        return super.endElement(element);
    }


    public Node writeDescriptor(Node parent, String nodeName, MethodDescriptor descriptor, String ejbName) {
        Node methodNode = super.writeDescriptor(parent, nodeName, descriptor);
        writeLocalizedDescriptions(methodNode, descriptor);
        if (ejbName != null && !ejbName.isEmpty()) {
            appendTextChild(methodNode, TagNames.EJB_NAME, ejbName);
        }
        String methodIntfSymbol = descriptor.getEjbClassSymbol();
        if (methodIntfSymbol != null && !methodIntfSymbol.equals(MethodDescriptor.EJB_BEAN)) {
            appendTextChild(methodNode, TagNames.METHOD_INTF, methodIntfSymbol);
        }
        appendTextChild(methodNode, TagNames.METHOD_NAME, descriptor.getName());
        if (descriptor.getParameterClassNames() != null) {
            Node paramsNode = appendChild(methodNode, TagNames.METHOD_PARAMS);
            writeMethodParams(paramsNode, descriptor);
        }
        return methodNode;
    }


    /**
     * Write the method descriptor class to a query-method DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeQueryMethodDescriptor(Node parent, String nodeName, MethodDescriptor descriptor) {
        Node methodNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(methodNode, TagNames.METHOD_NAME, descriptor.getName());
        Node paramsNode = appendChild(methodNode, TagNames.METHOD_PARAMS);
        writeMethodParams(paramsNode, descriptor);
        return methodNode;
    }


    /**
     * Write the method descriptor class to a java-method DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param methodDescriptor the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeJavaMethodDescriptor(Node parent, String nodeName, MethodDescriptor methodDescriptor) {
        // Write out the java method description. In the case of a void
        // method, a <method-params> element will *not* be written out.
        return writeJavaMethodDescriptor(parent, nodeName, methodDescriptor, false);
    }


    /**
     * Write the method descriptor class to a java-method DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName node name for the root element of this xml fragment
     * @param methodDescriptor the descriptor to write
     * @param writeEmptyMethodParamsElementForVoidMethods
     * @return the DOM tree top node
     */
    public Node writeJavaMethodDescriptor(Node parent, String nodeName, MethodDescriptor methodDescriptor,
        boolean writeEmptyMethodParamsElementForVoidMethods) {
        Node methodNode = super.writeDescriptor(parent, nodeName, methodDescriptor);
        appendTextChild(methodNode, RuntimeTagNames.METHOD_NAME, methodDescriptor.getName());
        if (methodDescriptor.getParameterClassNames() == null) {
            if (writeEmptyMethodParamsElementForVoidMethods) {
                appendChild(methodNode, RuntimeTagNames.METHOD_PARAMS);
            }
        } else {
            Node paramsNode = appendChild(methodNode, RuntimeTagNames.METHOD_PARAMS);
            writeMethodParams(paramsNode, methodDescriptor);
        }
        return methodNode;
    }


    /**
     * writes the method parameters to the DOM Tree
     *
     * @param the parent node for the parameters
     * @param the method descriptor
     */
    private void writeMethodParams(Node paramsNode, MethodDescriptor descriptor) {
        String[] params = descriptor.getParameterClassNames();
        if (params == null) {
            return;
        }
        for (String param : params) {
            appendTextChild(paramsNode, RuntimeTagNames.METHOD_PARAM, param);
        }
    }
}


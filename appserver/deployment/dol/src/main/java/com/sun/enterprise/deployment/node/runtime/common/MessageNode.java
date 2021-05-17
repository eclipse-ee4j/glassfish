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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.runtime.common.MessageDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import org.w3c.dom.Node;

/**
 * This node handles message element
 *
 */
public class MessageNode extends DeploymentDescriptorNode {

    MessageDescriptor descriptor = null;
    private static final String ALL_METHODS = "*";

    public MessageNode() {
        registerElementHandler(new XMLElement(
            RuntimeTagNames.JAVA_METHOD), MethodNode.class,
            "setMethodDescriptor");
    }

    /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public Object getDescriptor() {
        if (descriptor == null) {
            descriptor = new MessageDescriptor();
            setMiscDescriptors();
        }
        return descriptor;
    }


    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (WebServicesTagNames.OPERATION_NAME.equals(element.getQName())) {
            descriptor.setOperationName(value);
        } else {
            super.setElementValue(element, value);
        }
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
        MessageDescriptor messageDesc) {
        Node messageNode = super.writeDescriptor(parent, nodeName,
           messageDesc);

        // for empty message case, set the method descriptor
        // to a method descriptor with "*" as name
        if (messageDesc.getOperationName() == null &&
            messageDesc.getMethodDescriptor() == null) {
            MethodDescriptor allMethodDesc = new MethodDescriptor();
            allMethodDesc.setName(ALL_METHODS);
            messageDesc.setMethodDescriptor(allMethodDesc);
        }

        // java-method
        MethodDescriptor methodDesc = messageDesc.getMethodDescriptor();
        if (methodDesc != null) {
            MethodNode methodNode = new MethodNode();
            methodNode.writeJavaMethodDescriptor(messageNode,
                RuntimeTagNames.JAVA_METHOD, methodDesc);
        }

        // operation-name
        appendTextChild(messageNode, WebServicesTagNames.OPERATION_NAME,
            messageDesc.getOperationName());

        return messageNode;
    }

    private void setMiscDescriptors() {
        XMLNode parentNode =
            getParentNode().getParentNode().getParentNode();

        // get the endpoint or portinfo descriptor
        Object parentDesc = parentNode.getDescriptor();

        if (parentDesc instanceof ServiceRefPortInfo) {
            descriptor.setServiceRefPortInfo((ServiceRefPortInfo)parentDesc);
        } else if(parentDesc instanceof WebServiceEndpoint) {
            descriptor.setWebServiceEndpoint((WebServiceEndpoint)parentDesc);
        }

        // Get the bundle descriptor of which this belongs
        BundleDescriptor bundleDesc = null;
        parentNode = parentNode.getParentNode().getParentNode();
        if (parentNode.getDescriptor() instanceof WebBundleDescriptor) {
            // In the cases of used in
            // 1. webservice-endpoint for web component
            // 2. port-info for web component
            bundleDesc =
                (WebBundleDescriptor)parentNode.getDescriptor();
        } else if (parentNode.getDescriptor() instanceof BundleDescriptor) {
            // In the cases of used in port-info for app client
            bundleDesc = (BundleDescriptor)parentNode.getDescriptor();
        } else {
            // In the case of used in webservice-endpoint for ejb component
            if (parentNode.getDescriptor() instanceof EjbDescriptor) {
                EjbDescriptor ejbDesc =
                    (EjbDescriptor)parentNode.getDescriptor();
                bundleDesc = ejbDesc.getEjbBundleDescriptor();
            } else {
                // In the case of used in port-info for ejb component
                parentNode = parentNode.getParentNode();
                if (parentNode.getDescriptor() instanceof EjbDescriptor) {
                    EjbDescriptor ejbDesc =
                        (EjbDescriptor)parentNode.getDescriptor();
                    bundleDesc = ejbDesc.getEjbBundleDescriptor();
                }
            }
        }
        descriptor.setBundleDescriptor(bundleDesc);
    }
}

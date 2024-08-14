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

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.XMLNode;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * @author tjquinn
 */
public class JavaWebStartAccessNode extends DeploymentDescriptorNode<JavaWebStartAccessDescriptor> {

    protected JavaWebStartAccessDescriptor descriptor;

    /** Creates a new instance of JavaWebStartAccessNode */
    public JavaWebStartAccessNode() {
        handlers = null;
        registerElementHandler(new XMLElement(RuntimeTagNames.JNLP_DOC), JnlpDocNode.class);
    }


   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    @Override
    public JavaWebStartAccessDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new JavaWebStartAccessDescriptor();
            XMLNode<?> parentNode = getParentNode();
            if (parentNode != null && (parentNode instanceof AppClientRuntimeNode)) {
                Object parentDescriptor = parentNode.getDescriptor();
                if (parentDescriptor != null && (parentDescriptor instanceof ApplicationClientDescriptor)) {
                    ApplicationClientDescriptor acDescriptor = (ApplicationClientDescriptor) parentDescriptor;
                    acDescriptor.setJavaWebStartAccessDescriptor(descriptor);
                }

            }
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
        table.put(RuntimeTagNames.CONTEXT_ROOT, "setContextRoot");
        table.put(RuntimeTagNames.ELIGIBLE, "setEligible");
        table.put(RuntimeTagNames.VENDOR, "setVendor");
        table.put(RuntimeTagNames.JNLP_DOC, "setJnlpDocument");
        return table;
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name for the descriptor
     * @param descr the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, JavaWebStartAccessDescriptor descr) {
        Node accessNode = super.writeDescriptor(parent, nodeName, descr);
        appendTextChild(accessNode, RuntimeTagNames.CONTEXT_ROOT, descr.getContextRoot());
        appendTextChild(accessNode, RuntimeTagNames.ELIGIBLE, Boolean.toString(descr.isEligible()));
        appendTextChild(accessNode, RuntimeTagNames.VENDOR, descr.getVendor());
        appendTextChild(accessNode, RuntimeTagNames.JNLP_DOC, descr.getJnlpDocument());
        return accessNode;
    }


    public static void writeJavaWebStartInfo(Node parent, JavaWebStartAccessDescriptor descr) {
        if (descr != null) {
            JavaWebStartAccessNode newNode = new JavaWebStartAccessNode();
            newNode.writeDescriptor(parent, RuntimeTagNames.JAVA_WEB_START_ACCESS, descr);
        }
    }
}

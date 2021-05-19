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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.MethodPermissionDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.security.common.Role;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This class handles all the method-permission xml tag
 * information
 *
 * @author  Jerome Dochez
 * @version
 */
public class MethodPermissionNode extends DeploymentDescriptorNode<MethodPermissionDescriptor> {

    private MethodPermissionDescriptor descriptor;

    /** Creates new MethodPermissionNode */
    public MethodPermissionNode() {
        super();
        registerElementHandler(new XMLElement(EjbTagNames.METHOD),
                                                            MethodNode.class, "addMethod");
    }

    @Override
    public MethodPermissionDescriptor getDescriptor() {
       if (descriptor==null) {
            descriptor = new MethodPermissionDescriptor();
        }
        return descriptor;
    }

    /**
     * SAX Parser API implementation, we don't really care for now.
     */
    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (EjbTagNames.UNCHECKED.equals(element.getQName())) {
            descriptor.addMethodPermission(MethodPermission.getUncheckedMethodPermission());
        } else
            super.startElement(element, attributes);
    }

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.ROLE_NAME.equals(element.getQName())) {
            Role role = new Role(value);
            descriptor.addMethodPermission(new MethodPermission(role));
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, MethodPermissionDescriptor descriptor,
                EjbDescriptor ejb) {
        Node subNode = super.writeDescriptor(parent, nodeName, descriptor);
        return writeDescriptorInNode(subNode, descriptor, ejb);
    }

    /**
     * Write the descriptor in a DOM tree which root element is provided
     *
     * @param subNode the root element for the DOM fragment
     * @param descriptor the method permisison descriptor
     * @param ejb the ejb descriptor the above method permission belongs to
     */
    public Node writeDescriptorInNode(Node subNode, MethodPermissionDescriptor descriptor,
                EjbDescriptor ejb) {

        writeLocalizedDescriptions(subNode, descriptor);

        MethodPermission[] mps = descriptor.getMethodPermissions();
        if (mps.length==0)
            return null;

        if (!mps[0].isExcluded()) {
            if (mps[0].isUnchecked()) {
                appendChild(subNode, EjbTagNames.UNCHECKED);
            } else {
                for (int i=0;i<mps.length;i++) {
                    appendTextChild(subNode, TagNames.ROLE_NAME, mps[i].getRole().getName());
                }
            }
        }

        MethodDescriptor[] methods = descriptor.getMethods();
        MethodNode mn = new MethodNode();
        for (int i=0;i<methods.length;i++) {
            String ejbName = ejb.getName();
            mn.writeDescriptor(subNode, EjbTagNames.METHOD, methods[i], ejbName);
        }
        return subNode;
    }
}

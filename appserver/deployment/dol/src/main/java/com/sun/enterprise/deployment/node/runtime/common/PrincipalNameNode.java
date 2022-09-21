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

package com.sun.enterprise.deployment.node.runtime.common;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.common.PrincipalNameDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node handles principal-name information
 *
 * @author deployment dev team
 */
public class PrincipalNameNode extends RuntimeDescriptorNode<PrincipalNameDescriptor> {

    @Override
    protected PrincipalNameDescriptor createDescriptor() {
        return new PrincipalNameDescriptor(null);
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        PrincipalNameDescriptor principal = getDescriptor();
        if (RuntimeTagNames.PRINCIPAL_NAME.equals(element.getQName())) {
            principal.setName(value);
            Object rootDesc = getParentNode().getParentNode().getDescriptor();
            if (rootDesc instanceof RootDeploymentDescriptor) {
                principal.setClassLoader(((RootDeploymentDescriptor) rootDesc).getClassLoader());
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    protected boolean setAttributeValue(XMLElement element, XMLElement attribute, String value) {
        PrincipalNameDescriptor principal = getDescriptor();
        if (attribute.getQName().equals(RuntimeTagNames.CLASS_NAME)) {
            principal.setClassName(value);
            return true;
        }
        return false;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, PrincipalNameDescriptor descriptor) {
        // principal-name
        Element principal = (Element) appendTextChild(parent, RuntimeTagNames.PRINCIPAL_NAME, descriptor.getName());

        // class-name
        setAttribute(principal, RuntimeTagNames.CLASS_NAME, descriptor.getClassName());

        return principal;
    }
}

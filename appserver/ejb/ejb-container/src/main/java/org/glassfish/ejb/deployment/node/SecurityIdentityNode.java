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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.RunAsNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Collections;
import java.util.Map;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

/**
 * This node handles all information relative to security-indentity tag
 *
 * @author Jerome Dochez
 */
public class SecurityIdentityNode extends DeploymentDescriptorNode<Descriptor> {

    public static Node writeSecureIdentity(Node parent, String nodeName, EjbDescriptor descriptor) {
        Node subNode = appendChild(parent, nodeName);
        appendTextChild(subNode, TagNames.DESCRIPTION, descriptor.getSecurityIdentityDescription());
        if (descriptor.getUsesCallerIdentity()) {
            Node useCaller = subNode.getOwnerDocument().createElement(EjbTagNames.USE_CALLER_IDENTITY);
            subNode.appendChild(useCaller);
        } else {
            RunAsNode runAs = new RunAsNode();
            runAs.writeDescriptor(subNode, TagNames.RUNAS_SPECIFIED_IDENTITY, descriptor.getRunAsIdentity());
        }
        return subNode;
    }


    public SecurityIdentityNode() {
        registerElementHandler(new XMLElement(TagNames.RUNAS_SPECIFIED_IDENTITY), RunAsNode.class);
    }


    @Override
    public Descriptor getDescriptor() {
        return null;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        return Collections.emptyMap();
    }


    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (EjbTagNames.USE_CALLER_IDENTITY.equals(element.getQName())) {
            ((EjbDescriptor) getParentNode().getDescriptor()).setUsesCallerIdentity(true);
        } else {
            super.startElement(element, attributes);
        }
        return;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.DESCRIPTION.equals(element.getQName())) {
            ((EjbDescriptor) getParentNode().getDescriptor()).setSecurityIdentityDescription(value);
        } else {
            super.setElementValue(element, value);
        }
    }
}

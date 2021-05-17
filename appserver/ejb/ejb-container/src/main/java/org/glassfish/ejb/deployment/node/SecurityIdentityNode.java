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

import java.util.Map;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.RunAsNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

/**
 * This node handles all information relative to security-indentity tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class SecurityIdentityNode extends DeploymentDescriptorNode {

    public SecurityIdentityNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.RUNAS_SPECIFIED_IDENTITY), RunAsNode.class);
    }

    @Override
    public Object getDescriptor() {
        return null;
    }

    @Override
    protected Map getDispatchTable() {
        return  null;
    }

    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if( EjbTagNames.USE_CALLER_IDENTITY.equals(element.getQName()) ) {
            ((EjbDescriptor) getParentNode().getDescriptor()).
                setUsesCallerIdentity(true);
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

    public Node writeDescriptor(Node parent, String nodeName, EjbDescriptor descriptor) {
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
}

/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime.common.wls;

import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This node handles resource-description in weblogic.xml
 *
 * @author Shing Wai Chan
 */
public class ResourceDescriptionNode extends RuntimeDescriptorNode<ResourceReferenceDescriptor> {

    private ResourceReferenceDescriptor descriptor;

    @Override
    public ResourceReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourceReferenceDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.RESOURCE_REFERENCE_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof ResourceReferenceContainer) {
                try {
                    descriptor = ((ResourceReferenceContainer) parentDesc).getResourceReferenceByName(value);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof ResourceReferenceContainer) {
            ResourceReferenceContainer resourceReferenceContainer = (ResourceReferenceContainer) parentDesc;
            // resource-reference-description*
            Set<ResourceReferenceDescriptor> resourceReferenceDescriptors = resourceReferenceContainer
                .getResourceReferenceDescriptors();
            for (ResourceReferenceDescriptor resourceReferenceDescriptor : resourceReferenceDescriptors) {
                writeDescriptor(parent, nodeName, resourceReferenceDescriptor);
            }
        }
        return parent;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceReferenceDescriptor reference) {
        Node refNode = appendChild(parent, nodeName);
        appendTextChild(refNode, TagNames.RESOURCE_REFERENCE_NAME, reference.getName());
        appendTextChild(refNode, RuntimeTagNames.JNDI_NAME, reference.getJndiName());
        return parent;
    }
}

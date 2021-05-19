/*
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

import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.Set;

/**
 * This node handles resource-env-description in weblogic.xml
 *
 * @author  Shing Wai Chan
 */
public class ResourceEnvDescriptionNode extends RuntimeDescriptorNode<ResourceEnvReferenceDescriptor> {
    private ResourceEnvReferenceDescriptor descriptor = null;

    public ResourceEnvDescriptionNode() {
    }

    @Override
    public ResourceEnvReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourceEnvReferenceDescriptor();
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
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.RESOURCE_ENV_REFERENCE_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof ResourceEnvReferenceContainer) {
                try {
                    descriptor = ((ResourceEnvReferenceContainer)parentDesc).getResourceEnvReferenceByName(value);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceEnvReferenceDescriptor descriptor) {
        Node refNode = appendChild(parent, nodeName);
        appendTextChild(refNode, TagNames.RESOURCE_ENV_REFERENCE_NAME, descriptor.getName());
        appendTextChild(refNode, RuntimeTagNames.JNDI_NAME, descriptor.getJndiName());
        return refNode;
    }

    /**
     * write all occurrences of the descriptor corresponding to the current
     * node from the parent descriptor to an JAXP DOM node and return it
     *
     * This API will be invoked by the parent node when the parent node
     * writes out a mix of statically and dynamically registered sub nodes.
     *
     * This method should be overriden by the sub classes if it
     * needs to be called by the parent node.
     *
     * @param parent node in the DOM tree
     * @param nodeName the name of the node
     * @param parentDesc parent descriptor of the descriptor to be written
     * @return the JAXP DOM node
     */
    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof ResourceEnvReferenceContainer) {
            ResourceEnvReferenceContainer resourceEnvReferenceContainer =
                (ResourceEnvReferenceContainer)parentDesc;
            // resource-env-description*
            Set<ResourceEnvReferenceDescriptor> resourceEnvReferenceDescriptors =
                resourceEnvReferenceContainer.getResourceEnvReferenceDescriptors();
            for (ResourceEnvReferenceDescriptor resourceEnvReferenceDescriptor :
                    resourceEnvReferenceDescriptors) {
                writeDescriptor(parent, nodeName, resourceEnvReferenceDescriptor);
            }
        }
        return parent;
    }
}

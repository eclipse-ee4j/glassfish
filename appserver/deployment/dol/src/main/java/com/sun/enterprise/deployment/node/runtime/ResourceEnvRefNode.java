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

import java.util.Iterator;
import java.util.Map;

import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling runtime descriptor
 * resource-env-ref tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class ResourceEnvRefNode extends DeploymentDescriptorNode<ResourceEnvReferenceDescriptor> {

    private ResourceEnvReferenceDescriptor descriptor;

    @Override
    public ResourceEnvReferenceDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new ResourceEnvReferenceDescriptor();
        return descriptor;
    }

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
                    descriptor = ((ResourceEnvReferenceContainer) parentDesc).getResourceEnvReferenceByName(value);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
        } else super.setElementValue(element, value);
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceEnvReferenceDescriptor ejbRef) {
        Node resRefNode = super.writeDescriptor(parent, nodeName, ejbRef);
        appendTextChild(resRefNode, TagNames.RESOURCE_ENV_REFERENCE_NAME, ejbRef.getName());
        appendTextChild(resRefNode, RuntimeTagNames.JNDI_NAME, ejbRef.getJndiName());
        return resRefNode;
    }

    /**
     * writes all the runtime information for resource environment references
     *
     * @param parent node to add the runtime xml info
     * @param the J2EE component containing ejb references
     */
    public static void writeResoureEnvReferences(Node parent, ResourceEnvReferenceContainer descriptor) {
        // resource-env-ref*
        Iterator resRefs = descriptor.getResourceEnvReferenceDescriptors().iterator();
        if (resRefs.hasNext()) {
            ResourceEnvRefNode resourceEnvRefNode = new ResourceEnvRefNode();
            while (resRefs.hasNext()) {
                resourceEnvRefNode.writeDescriptor(parent, TagNames.RESOURCE_ENV_REFERENCE,
                    (ResourceEnvReferenceDescriptor) resRefs.next());
            }
        }
    }

}

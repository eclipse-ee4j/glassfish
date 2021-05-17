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
import java.util.logging.Level;

import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

/**
 * This node handles the runtime deployment descriptors for resource-ref tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class ResourceRefNode extends DeploymentDescriptorNode<ResourceReferenceDescriptor> {

    private ResourceReferenceDescriptor descriptor;

    public ResourceRefNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.DEFAULT_RESOURCE_PRINCIPAL),
                               DefaultResourcePrincipalNode.class, "setResourcePrincipal");
    }

    @Override
    public ResourceReferenceDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new ResourceReferenceDescriptor();
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
        if (TagNames.RESOURCE_REFERENCE_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            if (parentDesc instanceof ResourceReferenceContainer) {
                try {
                    descriptor = ((ResourceReferenceContainer) parentDesc).getResourceReferenceByName(value);
                    DOLUtils.getDefaultLogger().fine("Applying res-ref " + value + " runtime settings to " + descriptor);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
        } else super.setElementValue(element, value);
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (descriptor == null) {
            DOLUtils.getDefaultLogger().log(Level.WARNING, "enterprise.deployment.backend.addDescriptorFailure",
                new Object[] {newDescriptor, this});
            return;
        }
        if (newDescriptor instanceof ResourcePrincipal) {
            descriptor.setResourcePrincipal((ResourcePrincipal) newDescriptor);
        } else if (newDescriptor instanceof MailConfiguration) {
            // XXX - This special case doesn't seem to be needed since no one
            // ever uses the value set here.  I'm not even sure this case can
            // ever happen; see MailConfigurationNode.
            descriptor.setMailConfiguration((MailConfiguration) newDescriptor);
        } else {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.addDescriptorFailure",
                    new Object[]{"In " + this + " do not know what to do with " + newDescriptor});
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceReferenceDescriptor rrDescriptor) {
        Node rrNode = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(rrNode, TagNames.RESOURCE_REFERENCE_NAME, rrDescriptor.getName());
        appendTextChild(rrNode, RuntimeTagNames.JNDI_NAME, rrDescriptor.getJndiName());
        if (rrDescriptor.getResourcePrincipal() != null) {
            DefaultResourcePrincipalNode drpNode = new DefaultResourcePrincipalNode();
            drpNode.writeDescriptor(rrNode, RuntimeTagNames.DEFAULT_RESOURCE_PRINCIPAL,
                                    rrDescriptor.getResourcePrincipal());
        }
        return rrNode;
    }

    /**
     * writes all the runtime information for resources references
     *
     * @param parent node to add the runtime xml info
     * @param the J2EE component containing ejb references
     */
    public static void writeResourceReferences(Node parent, ResourceReferenceContainer descriptor) {
        // resource-ref*
        Iterator rrs = descriptor.getResourceReferenceDescriptors().iterator();
        if (rrs.hasNext()) {

            ResourceRefNode rrNode = new ResourceRefNode();
            while (rrs.hasNext()) {
                rrNode.writeDescriptor(parent, TagNames.RESOURCE_REFERENCE,
                    (ResourceReferenceDescriptor) rrs.next());
            }
        }
    }
}

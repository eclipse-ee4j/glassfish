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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This class handles all information related to the resource-env-ref xml tag
 *
 * @author Jerome Dochez
 */
public class ResourceEnvRefNode extends DeploymentDescriptorNode<ResourceEnvReferenceDescriptor> {

    private ResourceEnvReferenceDescriptor descriptor;

    public ResourceEnvRefNode() {
        registerElementHandler(new XMLElement(TagNames.INJECTION_TARGET), InjectionTargetNode.class, "addInjectionTarget");
    }


    @Override
    public ResourceEnvReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourceEnvReferenceDescriptor();
        }
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.RESOURCE_ENV_REFERENCE_NAME, "setName");
        table.put(TagNames.RESOURCE_ENV_REFERENCE_TYPE, "setRefType");
        table.put(TagNames.MAPPED_NAME, "setMappedName");
        table.put(TagNames.LOOKUP_NAME, "setLookupName");
        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceEnvReferenceDescriptor descriptor) {
        Node ejbResNode = appendChild(parent, nodeName);

        writeLocalizedDescriptions(ejbResNode, descriptor);

        appendTextChild(ejbResNode, TagNames.RESOURCE_ENV_REFERENCE_NAME, descriptor.getName());
        appendTextChild(ejbResNode, TagNames.RESOURCE_ENV_REFERENCE_TYPE, descriptor.getRefType());
        appendTextChild(ejbResNode, TagNames.MAPPED_NAME, descriptor.getMappedName());

        if (descriptor.isInjectable()) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : descriptor.getInjectionTargets()) {
                ijNode.writeDescriptor(ejbResNode, TagNames.INJECTION_TARGET, target);
            }
        }
        appendTextChild(ejbResNode, TagNames.LOOKUP_NAME, descriptor.getLookupName());
        return ejbResNode;
    }
}

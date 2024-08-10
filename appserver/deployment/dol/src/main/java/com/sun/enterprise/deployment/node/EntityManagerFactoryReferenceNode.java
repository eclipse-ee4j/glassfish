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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node handles all persistence-unit-ref xml tag elements
 *
 * @author  Shing Wai Chan
 * @version
 */
public class EntityManagerFactoryReferenceNode extends DeploymentDescriptorNode {

    public EntityManagerFactoryReferenceNode() {
        super();
        registerElementHandler(new XMLElement(TagNames.INJECTION_TARGET),
                                InjectionTargetNode.class, "addInjectionTarget");
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map
     * xml element to method name on the descriptor class for setting
     * the element value.
     *
     * @return the map with the element name as a key, the setter method as
     *         a value
     */
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(TagNames.PERSISTENCE_UNIT_REF_NAME, "setName");
        table.put(TagNames.PERSISTENCE_UNIT_NAME, "setUnitName");
        return table;
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param node name for the root element of this xml fragment
     * @param the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, EntityManagerFactoryReferenceDescriptor descriptor) {
        Node entityMgrFactoryRefNode = appendChild(parent, nodeName);
        writeLocalizedDescriptions(entityMgrFactoryRefNode, descriptor);

        appendTextChild(entityMgrFactoryRefNode, TagNames.PERSISTENCE_UNIT_REF_NAME, descriptor.getName());
        appendTextChild(entityMgrFactoryRefNode, TagNames.PERSISTENCE_UNIT_NAME, descriptor.getUnitName());
        if( descriptor.isInjectable() ) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : descriptor.getInjectionTargets()) {
                ijNode.writeDescriptor(entityMgrFactoryRefNode, TagNames.INJECTION_TARGET, target);
            }
        }

        return entityMgrFactoryRefNode;
    }
}

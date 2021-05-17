/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.xml.TagNames;
import org.w3c.dom.Node;

import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.SynchronizationType;
import java.util.Map;

/**
 * This node handles all persistence-context-ref xml tag elements
 *
 * @author  Shing Wai Chan
 * @version
 */
public class EntityManagerReferenceNode extends DeploymentDescriptorNode {
    private static final String TRANSACTION = "Transaction";
    private static final String EXTENDED = "Extended";
    private static final String SYNCHRONIZED = "Synchronized";
    private static final String UNSYNCHRONIZED = "Unsynchronized";

    // Holds property name during name/value processing.
    private String propertyName = null;

    public EntityManagerReferenceNode() {
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
        table.put(TagNames.PERSISTENCE_CONTEXT_REF_NAME, "setName");
        table.put(TagNames.PERSISTENCE_UNIT_NAME, "setUnitName");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.PERSISTENCE_CONTEXT_TYPE.equals(element.getQName())) {
            EntityManagerReferenceDescriptor entityMgrReferenceDescriptor =
                (EntityManagerReferenceDescriptor)getDescriptor();
            PersistenceContextType contextType = null;
            if (EXTENDED.equals(value)) {
                contextType = PersistenceContextType.EXTENDED;
            } else if (TRANSACTION.equals(value)) {
                contextType = PersistenceContextType.TRANSACTION;
            } else {
                throw new IllegalArgumentException(localStrings.getLocalString(
                "enterprise.deployment.node.invalidvalue",
                "Invalid value for a tag under {0} : {1}",
                new Object[] {TagNames.PERSISTENCE_CONTEXT_TYPE, value}));
            }

            entityMgrReferenceDescriptor.setPersistenceContextType(contextType);

        } else if (TagNames.PERSISTENCE_CONTEXT_SYNCHRONIZATION_TYPE.equals(element.getQName() ) ) {
            EntityManagerReferenceDescriptor entityMgrReferenceDescriptor =
                    (EntityManagerReferenceDescriptor)getDescriptor();
            SynchronizationType synchronizationType;
            if (SYNCHRONIZED.equals(value)) {
                synchronizationType = SynchronizationType.SYNCHRONIZED;
            } else if (UNSYNCHRONIZED.equals(value)) {
                synchronizationType = SynchronizationType.UNSYNCHRONIZED;
            } else {
                throw new IllegalArgumentException(localStrings.getLocalString(
                        "enterprise.deployment.node.invalidvalue",
                        "Invalid value for a tag under {0} : {1}",
                        new Object[] {TagNames.PERSISTENCE_CONTEXT_SYNCHRONIZATION_TYPE, value}));
            }
            entityMgrReferenceDescriptor.setSynchronizationType(synchronizationType);

        } else if (TagNames.NAME_VALUE_PAIR_NAME.equals(element.getQName())) {
            propertyName = value;
        } else if (TagNames.NAME_VALUE_PAIR_VALUE.equals(element.getQName())) {
            EntityManagerReferenceDescriptor entityMgrReferenceDescriptor =
                (EntityManagerReferenceDescriptor)getDescriptor();
            entityMgrReferenceDescriptor.addProperty(propertyName, value);
            propertyName = null;
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param descriptor descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, EntityManagerReferenceDescriptor descriptor) {
        Node entityMgrRefNode = appendChild(parent, nodeName);
        writeLocalizedDescriptions(entityMgrRefNode, descriptor);

        appendTextChild(entityMgrRefNode, TagNames.PERSISTENCE_CONTEXT_REF_NAME, descriptor.getName());
        appendTextChild(entityMgrRefNode, TagNames.PERSISTENCE_UNIT_NAME, descriptor.getUnitName());
        PersistenceContextType contextType = descriptor.getPersistenceContextType();
        String contextTypeString = (contextType != null &&
            PersistenceContextType.EXTENDED.equals(contextType)) ?
            EXTENDED : TRANSACTION;
        appendTextChild(entityMgrRefNode, TagNames.PERSISTENCE_CONTEXT_TYPE,
            contextTypeString);

        for(Map.Entry<String, String> property :
                descriptor.getProperties().entrySet()) {
            Node propertyNode = appendChild(entityMgrRefNode,
                                            TagNames.PERSISTENCE_PROPERTY);
            appendTextChild(propertyNode, TagNames.NAME_VALUE_PAIR_NAME,
                            property.getKey());
            appendTextChild(propertyNode, TagNames.NAME_VALUE_PAIR_VALUE,
                            property.getValue());
        }

        if( descriptor.isInjectable() ) {
            InjectionTargetNode ijNode = new InjectionTargetNode();
            for (InjectionTarget target : descriptor.getInjectionTargets()) {
                ijNode.writeDescriptor(entityMgrRefNode, TagNames.INJECTION_TARGET, target);
            }
        }

        return entityMgrRefNode;
    }
}

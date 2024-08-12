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

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;

import java.util.Map;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;
import org.w3c.dom.Node;

/**
 *
 * @author  dochez
 * @version
 */
public class EjbRelationNode extends DeploymentDescriptorNode<RelationshipDescriptor> {

    private RelationRoleDescriptor source;
    private RelationRoleDescriptor sink;
    private RelationshipDescriptor descriptor;

    public EjbRelationNode() {
       super();
       registerElementHandler(new XMLElement(EjbTagNames.EJB_RELATIONSHIP_ROLE),
                                                            EjbRelationshipRoleNode.class);
    }

    @Override
    public RelationshipDescriptor getDescriptor() {
        if (descriptor==null) descriptor = new RelationshipDescriptor();
        return descriptor;
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof RelationRoleDescriptor) {
            if (source==null) {
                source = (RelationRoleDescriptor) newDescriptor;
            } else {
                sink = (RelationRoleDescriptor) newDescriptor;

                descriptor.setSource(source);
                source.setPartner(sink);
                source.setRelationshipDescriptor(descriptor);
                descriptor.setSink(sink);
                sink.setPartner(source);
                sink.setRelationshipDescriptor(descriptor);

                if ( source.getCMRField() != null && sink.getCMRField() != null )
                    descriptor.setIsBidirectional(true);
                else
                    descriptor.setIsBidirectional(false);
            }
        }
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.EJB_RELATION_NAME, "setName");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, RelationshipDescriptor descriptor) {
        Node ejbRelationNode = super.writeDescriptor(parent, nodeName, descriptor);
        writeLocalizedDescriptions(ejbRelationNode, descriptor);
        appendTextChild(ejbRelationNode, EjbTagNames.EJB_RELATION_NAME, descriptor.getName());
        EjbRelationshipRoleNode roleNode = new EjbRelationshipRoleNode();
        roleNode.writeDescriptor(ejbRelationNode, EjbTagNames.EJB_RELATIONSHIP_ROLE, descriptor.getSource());
        roleNode.writeDescriptor(ejbRelationNode, EjbTagNames.EJB_RELATIONSHIP_ROLE, descriptor.getSink());
        return ejbRelationNode;
    }
}

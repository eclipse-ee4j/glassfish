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
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling the ejb-relationships xml element
 *
 * @author Jerome Dochez
 */
public class RelationshipsNode extends DeploymentDescriptorNode<RelationshipDescriptor> {

    public static Node writeRelationships(Node parent, String nodeName, EjbBundleDescriptorImpl descriptor) {
        Node relationshipsNode = appendChild(parent, nodeName);
        appendTextChild(relationshipsNode, TagNames.DESCRIPTION, descriptor.getRelationshipsDescription());
        EjbRelationNode subNode = new EjbRelationNode();
        for (RelationshipDescriptor rd : descriptor.getRelationships()) {
            subNode.writeDescriptor(relationshipsNode, EjbTagNames.EJB_RELATION, rd);
        }
        return relationshipsNode;
    }


    public RelationshipsNode() {
        registerElementHandler(new XMLElement(EjbTagNames.EJB_RELATION), EjbRelationNode.class);
    }


    @Override
    public RelationshipDescriptor getDescriptor() {
        return (RelationshipDescriptor) getParentNode().getDescriptor();
    }


    @Override
    public boolean endElement(XMLElement element) {
        return element.equals(getXMLRootTag());
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = new HashMap<>();
        table.put(TagNames.DESCRIPTION, "setRelationshipsDescription");
        return table;
    }
}

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

import java.util.Map;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling all sub-element of cmp-field tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class CmpFieldNode extends DeploymentDescriptorNode<FieldDescriptor> {

    private FieldDescriptor fieldDescriptor;

    @Override
    public FieldDescriptor getDescriptor() {
        if (fieldDescriptor == null) fieldDescriptor = new FieldDescriptor();
        return fieldDescriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.FIELD_NAME, "setName");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, FieldDescriptor descriptor) {
        Node cmpField = appendChild(parent, nodeName);
        writeLocalizedDescriptions(cmpField, descriptor);
        appendTextChild(cmpField, EjbTagNames.FIELD_NAME, descriptor.getName());
        return cmpField;
    }
}

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
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;

import org.w3c.dom.Node;

/**
 * This node handles all information relative to the injection-target element
 *
 * @author Jerome Dochez
 */
public class InjectionTargetNode extends DeploymentDescriptorNode<InjectionTarget> {

    @Override
    protected InjectionTarget createDescriptor() {
        return new InjectionTarget();
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(TagNames.INJECTION_TARGET_CLASS, "setClassName");
        table.put(TagNames.INJECTION_TARGET_NAME, "setTargetName");
        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, InjectionTarget descriptor) {
        Node myNode = appendChild(parent, nodeName);
        appendTextChild(myNode, TagNames.INJECTION_TARGET_CLASS, descriptor.getClassName());
        appendTextChild(myNode, TagNames.INJECTION_TARGET_NAME, descriptor.getTargetName());
        return myNode;
    }

}

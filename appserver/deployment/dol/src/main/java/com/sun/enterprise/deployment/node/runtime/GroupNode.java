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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.common.GroupNameDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;


/**
 * This node handles the group definition in the runtime DDs
 *
 * @author Jerome Dochez
 */
public class GroupNode extends DeploymentDescriptorNode<GroupNameDescriptor> {

    private GroupNameDescriptor group;


    @Override
    public GroupNameDescriptor getDescriptor() {
        return group;
    }


    @Override
    public void startElement(XMLElement element, Attributes attributes) {
        if (RuntimeTagNames.GROUP.equals(element.getQName())) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (RuntimeTagNames.NAME.equals(attributes.getQName(i))) {
                    group = new GroupNameDescriptor(attributes.getValue(i));
                }
            }
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, GroupNameDescriptor descriptor) {
        Element element = appendChild(parent, nodeName);
        setAttribute(element, RuntimeTagNames.NAME, descriptor.getName());
        return element;
    }
}

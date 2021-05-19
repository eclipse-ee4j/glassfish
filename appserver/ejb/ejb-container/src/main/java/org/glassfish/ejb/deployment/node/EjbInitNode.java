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

import java.util.Map;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbInitInfo;
import org.w3c.dom.Node;

public class EjbInitNode extends DeploymentDescriptorNode<EjbInitInfo> {

    private EjbInitInfo ejbInitInfo;

    public EjbInitNode() {
       super();
       registerElementHandler(new XMLElement(EjbTagNames.INIT_CREATE_METHOD), MethodNode.class, "setCreateMethod");
       registerElementHandler(new XMLElement(EjbTagNames.INIT_BEAN_METHOD), MethodNode.class, "setBeanMethod");
    }

    @Override
    public EjbInitInfo getDescriptor() {
        if (ejbInitInfo == null) ejbInitInfo = new EjbInitInfo();
        return ejbInitInfo;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbInitInfo initMethod) {
        Node initNode = appendChild(parent, nodeName);
        MethodNode methodNode = new MethodNode();

        methodNode.writeJavaMethodDescriptor
            (initNode, EjbTagNames.INIT_CREATE_METHOD,
             initMethod.getCreateMethod());

        methodNode.writeJavaMethodDescriptor
            (initNode, EjbTagNames.INIT_BEAN_METHOD,
             initMethod.getBeanMethod());

        return initNode;
    }
}

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
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbApplicationExceptionInfo;
import org.w3c.dom.Node;

public class EjbApplicationExceptionNode extends DeploymentDescriptorNode<EjbApplicationExceptionInfo> {

    private EjbApplicationExceptionInfo eaeInfo;

    public EjbApplicationExceptionNode() {
       super();
    }

    @Override
    public EjbApplicationExceptionInfo getDescriptor() {
        if (eaeInfo == null) eaeInfo = new EjbApplicationExceptionInfo();
        return eaeInfo;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.APP_EXCEPTION_CLASS, "setExceptionClassName");
        table.put(EjbTagNames.APP_EXCEPTION_ROLLBACK, "setRollback");
        table.put(EjbTagNames.APP_EXCEPTION_INHERITED, "setInherited");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName,
                                EjbApplicationExceptionInfo descriptor) {
        Node appExceptionNode = appendChild(parent, nodeName);
        appendTextChild(appExceptionNode, EjbTagNames.APP_EXCEPTION_CLASS,
                        descriptor.getExceptionClassName());
        appendTextChild(appExceptionNode, EjbTagNames.APP_EXCEPTION_ROLLBACK,
                        Boolean.toString(descriptor.getRollback()));
        appendTextChild(appExceptionNode, EjbTagNames.APP_EXCEPTION_INHERITED,
                        Boolean.toString(descriptor.getInherited()));
        return appExceptionNode;
    }
}

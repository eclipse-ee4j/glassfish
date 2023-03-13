/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.EjbApplicationExceptionInfo;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;

import java.util.Map;

import org.w3c.dom.Node;

import static org.glassfish.ejb.deployment.EjbTagNames.APP_EXCEPTION_CLASS;
import static org.glassfish.ejb.deployment.EjbTagNames.APP_EXCEPTION_INHERITED;
import static org.glassfish.ejb.deployment.EjbTagNames.APP_EXCEPTION_ROLLBACK;

public class EjbApplicationExceptionNode extends DeploymentDescriptorNode<EjbApplicationExceptionInfo> {

    private EjbApplicationExceptionInfo eaeInfo;

    @Override
    public EjbApplicationExceptionInfo getDescriptor() {
        if (eaeInfo == null) {
            eaeInfo = new EjbApplicationExceptionInfo();
        }
        return eaeInfo;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(APP_EXCEPTION_CLASS, "setExceptionClassName");
        table.put(APP_EXCEPTION_ROLLBACK, "setRollback");
        table.put(APP_EXCEPTION_INHERITED, "setInherited");
        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbApplicationExceptionInfo descriptor) {
        Node appExceptionNode = appendChild(parent, nodeName);
        appendTextChild(appExceptionNode, APP_EXCEPTION_CLASS, descriptor.getExceptionClassName());
        appendTextChild(appExceptionNode, APP_EXCEPTION_ROLLBACK, Boolean.toString(descriptor.getRollback()));
        appendTextChild(appExceptionNode, APP_EXCEPTION_INHERITED, Boolean.toString(descriptor.getInherited()));
        return appExceptionNode;
    }
}

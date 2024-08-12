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

package org.glassfish.ejb.deployment.node.runtime;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;

import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbCMPFinder;
import org.w3c.dom.Node;

public class FinderNode extends DeploymentDescriptorNode<IASEjbCMPFinder> {

    private IASEjbCMPFinder descriptor;

    @Override
    public IASEjbCMPFinder getDescriptor() {
        if (descriptor==null) {
            descriptor = new IASEjbCMPFinder();
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map dispatchTable = super.getDispatchTable();
        dispatchTable.put(RuntimeTagNames.METHOD_NAME, "setMethodName");
        dispatchTable.put(RuntimeTagNames.QUERY_PARAMS, "setQueryParameterDeclaration");
        dispatchTable.put(RuntimeTagNames.QUERY_FILTER, "setQueryFilter");
        dispatchTable.put(RuntimeTagNames.QUERY_VARIABLES, "setQueryVariables");
        dispatchTable.put(RuntimeTagNames.QUERY_ORDERING, "setQueryOrdering");
        return dispatchTable;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, IASEjbCMPFinder finder) {
        Node finderNode = super.writeDescriptor(parent, nodeName, finder);
        appendTextChild(finderNode, RuntimeTagNames.METHOD_NAME, finder.getMethodName());
        appendTextChild(finderNode, RuntimeTagNames.QUERY_PARAMS, finder.getQueryParameterDeclaration());
        appendTextChild(finderNode, RuntimeTagNames.QUERY_FILTER, finder.getQueryFilter());
        appendTextChild(finderNode, RuntimeTagNames.QUERY_VARIABLES, finder.getQueryVariables());
        appendTextChild(finderNode, RuntimeTagNames.QUERY_ORDERING, finder.getQueryOrdering());
        return finderNode;
    }
}

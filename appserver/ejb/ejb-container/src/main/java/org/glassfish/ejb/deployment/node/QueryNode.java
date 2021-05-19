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
import java.util.logging.Level;

import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.QueryDescriptor;
import org.w3c.dom.Node;

/**
 * This class is responsible for handling the query element
 *
 * @author  Jerome Dochez
 * @version
 */
public class QueryNode extends DeploymentDescriptorNode<QueryDescriptor> {

    private QueryDescriptor descriptor;

    public QueryNode() {
        super();
        registerElementHandler(new XMLElement(EjbTagNames.QUERY_METHOD),
                                                                MethodNode.class, "setQueryMethodDescriptor");
    }

    @Override
    public QueryDescriptor getDescriptor() {
        if (descriptor == null) descriptor = new QueryDescriptor();
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        // no need to be synchronized for now
        Map table = super.getDispatchTable();
        table.put(EjbTagNames.EJB_QL, "setQuery");
        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (EjbTagNames.QUERY_RESULT_TYPE_MAPPING.equals(element.getQName())) {
            if (EjbTagNames.QUERY_REMOTE_TYPE_MAPPING.equals(value)) {
                descriptor.setHasRemoteReturnTypeMapping();
            } else if (EjbTagNames.QUERY_LOCAL_TYPE_MAPPING.equals(value)) {
                descriptor.setHasLocalReturnTypeMapping();
            } else {
                DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.addDescriptorFailure",
                                new Object[] {((Descriptor) getParentNode().getDescriptor()).getName() , value});
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, QueryDescriptor descriptor) {
        Node queryNode = super.writeDescriptor(parent, nodeName, descriptor);

        writeLocalizedDescriptions(queryNode, descriptor);

        // query-method
        MethodNode methodNode = new MethodNode();
        methodNode.writeQueryMethodDescriptor(queryNode, EjbTagNames.QUERY_METHOD,
                                                                         descriptor.getQueryMethodDescriptor());

        if (descriptor.getHasRemoteReturnTypeMapping()) {
            appendTextChild(queryNode, EjbTagNames.QUERY_RESULT_TYPE_MAPPING,
                                                    EjbTagNames.QUERY_REMOTE_TYPE_MAPPING);
        } else {
        if (descriptor.getHasLocalReturnTypeMapping()) {
                appendTextChild(queryNode, EjbTagNames.QUERY_RESULT_TYPE_MAPPING,
                                                    EjbTagNames.QUERY_LOCAL_TYPE_MAPPING);
            }
    }
        // ejbql element is mandatory.  If no EJB QL query has been
        // specified for the method, the xml element will be empty
        String ejbqlText = descriptor.getIsEjbQl() ? descriptor.getQuery() : "";
        Node child = appendChild(queryNode, EjbTagNames.EJB_QL);
        child.appendChild(getOwnerDocument(child).createTextNode(ejbqlText));

        return queryNode;
    }
}

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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling the container-transaction XML node
 *
 * @author Jerome Dochez
 */
public class ContainerTransactionNode extends DeploymentDescriptorNode<ContainerTransaction> {

    private String trans_attribute;
    private String description;
    private final Vector<MethodDescriptor> methods = new Vector<>();

    public static Node writeContainerTransactions(Node parent, String nodeName, String methodName,
        Map<MethodDescriptor, ContainerTransaction> methodToTransactions) {
        MethodNode mn = new MethodNode();
        for (Entry<MethodDescriptor, ContainerTransaction> entry : methodToTransactions.entrySet()) {
            MethodDescriptor md = entry.getKey();
            Node ctNode = appendChild(parent, nodeName);
            ContainerTransaction ct = entry.getValue();
            appendTextChild(ctNode, TagNames.DESCRIPTION, ct.getDescription());
            mn.writeDescriptor(ctNode, EjbTagNames.METHOD, md, methodName);
            appendTextChild(ctNode, EjbTagNames.TRANSACTION_ATTRIBUTE, ct.getTransactionAttribute());
        }
        return null;
    }


    public ContainerTransactionNode() {
        registerElementHandler(new XMLElement(EjbTagNames.METHOD), MethodNode.class);
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof MethodDescriptor) {
            methods.add((MethodDescriptor) newDescriptor);
        }
    }


    @Override
    public ContainerTransaction getDescriptor() {
        return new ContainerTransaction(trans_attribute, description);
    }


    @Override
    public boolean endElement(XMLElement element) {
        boolean doneWithNode = super.endElement(element);

        if (doneWithNode) {
            ContainerTransaction ct = new ContainerTransaction(trans_attribute, description);
            for (MethodDescriptor method : methods) {
                EjbBundleDescriptorImpl bundle = (EjbBundleDescriptorImpl) getParentNode().getDescriptor();
                EjbDescriptor ejb = bundle.getEjbByName(method.getEjbName(), true);
                ejb.getMethodContainerTransactions().put(method, ct);
            }
        }
        return doneWithNode;
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.DESCRIPTION.equals(element.getQName())) {
            description = value;
        }
        if (EjbTagNames.TRANSACTION_ATTRIBUTE.equals(element.getQName())) {
            trans_attribute = value;
        }
    }
}

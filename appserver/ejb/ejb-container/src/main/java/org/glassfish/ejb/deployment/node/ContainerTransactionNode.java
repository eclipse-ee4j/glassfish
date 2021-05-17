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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MethodNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.w3c.dom.Node;
/**
 * This node is responsible for handling the container-transaction XML node
 *
 * @author  Jerome Dochez
 * @version
 */
public class ContainerTransactionNode extends DeploymentDescriptorNode {

    private String trans_attribute;
    private String description;
    private Vector methods = new Vector();

    public ContainerTransactionNode() {
       registerElementHandler(new XMLElement(EjbTagNames.METHOD), MethodNode.class);
    }

    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof MethodDescriptor) {
            methods.add(newDescriptor);
        }
    }

    @Override
    public Object getDescriptor() {
        return null;
    }

    @Override
    public boolean endElement(XMLElement element) {
        boolean doneWithNode = super.endElement(element);

        if (doneWithNode) {
            ContainerTransaction ct =  new ContainerTransaction(trans_attribute, description);
            for (Iterator methodsIterator = methods.iterator();methodsIterator.hasNext();) {
                MethodDescriptor md = (MethodDescriptor) methodsIterator.next();
                EjbBundleDescriptorImpl bundle = (EjbBundleDescriptorImpl) getParentNode().getDescriptor();
                EjbDescriptor ejb = bundle.getEjbByName(md.getEjbName(), true);
                ejb.getMethodContainerTransactions().put(md, ct);
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

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node in the DOM tree
     * @param nodeName name for the root element of this xml fragment
     * @param ejb the descriptor to write
     * @return the DOM tree top node
     */
    public Node writeDescriptor(Node parent, String nodeName, EjbDescriptor ejb) {

        Map methodToTransactions = ejb.getMethodContainerTransactions();
        MethodNode mn = new MethodNode();
        for (Object o : methodToTransactions.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            MethodDescriptor md = (MethodDescriptor) entry.getKey();
            Node ctNode = super.writeDescriptor(parent, nodeName, ejb);
            ContainerTransaction ct = (ContainerTransaction) entry.getValue();
            appendTextChild(ctNode, EjbTagNames.DESCRIPTION, ct.getDescription());
            mn.writeDescriptor(ctNode, EjbTagNames.METHOD, md, ejb.getName());
            appendTextChild(ctNode, EjbTagNames.TRANSACTION_ATTRIBUTE, ct.getTransactionAttribute());
        }
        return null;
    }
}

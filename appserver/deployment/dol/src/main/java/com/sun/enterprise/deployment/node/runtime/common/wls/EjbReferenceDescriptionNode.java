/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime.common.wls;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;
import org.w3c.dom.Node;

/**
 * This node handles ejb-reference-description in weblogic DD.
 *
 * @author  Shing Wai Chan
 */
public class EjbReferenceDescriptionNode extends RuntimeDescriptorNode<EjbReference> {
    private EjbReference descriptor;

    @Override
    public EjbReference getDescriptor() {
        return descriptor;
    }

    /**
     * all sub-implementation of this class can use a dispatch table to map xml element to
     * method name on the descriptor class for setting the element value.
     *
     * @return the map with the element name as a key, the setter method as a value
     */
    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }

    /**
     * receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.EJB_REFERENCE_NAME.equals(element.getQName())) {
            Object parentDesc = getParentNode().getDescriptor();
            Logger logger = DOLUtils.getDefaultLogger();
            if (parentDesc instanceof EjbReferenceContainer) {
                try {
                    descriptor = ((EjbReferenceContainer) parentDesc).getEjbReference(value);
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Applying ref runtime to " + descriptor);
                    }
                } catch (IllegalArgumentException iae) {
                    logger.warning(iae.getMessage());
                }
            }
            if (descriptor == null) {
                logger.log(Level.SEVERE, "enterprise.deployment.backend.addDescriptorFailure",
                        new Object[]{"ejb-ref" , value });
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbReference descriptor) {
        Node ejbRef = appendChild(parent, nodeName);
        appendTextChild(ejbRef, TagNames.EJB_REFERENCE_NAME, descriptor.getName());
        appendTextChild(ejbRef, RuntimeTagNames.JNDI_NAME, descriptor.getJndiName());
        return ejbRef;
    }

    /**
     * write all occurrences of the descriptor corresponding to the current
     * node from the parent descriptor to an JAXP DOM node and return it
     *
     * This API will be invoked by the parent node when the parent node
     * writes out a mix of statically and dynamically registered sub nodes.
     *
     * This method should be overriden by the sub classes if it
     * needs to be called by the parent node.
     *
     * @param parent node in the DOM tree
     * @param nodeName the name of the node
     * @param parentDesc parent descriptor of the descriptor to be written
     * @return the JAXP DOM node
     */
    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof EjbReferenceContainer) {
            EjbReferenceContainer ejbReferenceContainer = (EjbReferenceContainer)parentDesc;
            // ejb-reference-description*
            Set<EjbReferenceDescriptor> ejbReferences = ejbReferenceContainer.getEjbReferenceDescriptors();
            for (EjbReferenceDescriptor ejbReference : ejbReferences) {
                writeDescriptor(parent, nodeName, ejbReference);
            }
        }
        return parent;
    }
}

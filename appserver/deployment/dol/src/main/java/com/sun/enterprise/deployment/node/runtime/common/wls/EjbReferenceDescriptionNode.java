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
public class EjbReferenceDescriptionNode extends RuntimeDescriptorNode<EjbReferenceDescriptor> {
    private EjbReferenceDescriptor descriptor;

    @Override
    public EjbReferenceDescriptor getDescriptor() {
        return descriptor;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        return table;
    }


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
                logger.log(Level.SEVERE, DOLUtils.ADD_DESCRIPTOR_FAILURE, new Object[] {"ejb-ref", value});
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbReferenceDescriptor descriptor) {
        Node ejbRef = appendChild(parent, nodeName);
        appendTextChild(ejbRef, TagNames.EJB_REFERENCE_NAME, descriptor.getName());
        appendTextChild(ejbRef, RuntimeTagNames.JNDI_NAME, descriptor.getJndiName());
        return ejbRef;
    }


    @Override
    public Node writeDescriptors(Node parent, String nodeName, Descriptor parentDesc) {
        if (parentDesc instanceof EjbReferenceContainer) {
            EjbReferenceContainer ejbReferenceContainer = (EjbReferenceContainer) parentDesc;
            // ejb-reference-description*
            Set<EjbReferenceDescriptor> ejbReferences = ejbReferenceContainer.getEjbReferenceDescriptors();
            for (EjbReferenceDescriptor ejbReference : ejbReferences) {
                writeDescriptor(parent, nodeName, ejbReference);
            }
        }
        return parent;
    }
}

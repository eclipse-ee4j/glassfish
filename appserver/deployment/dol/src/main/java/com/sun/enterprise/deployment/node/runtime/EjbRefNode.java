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

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.api.naming.SimpleJndiName;
import org.w3c.dom.Node;

/**
 * This node class is responsible for handling runtime deployment descriptors
 * for ejb-ref
 *
 * @author Jerome Dochez
 * @version
 */
public class EjbRefNode extends DeploymentDescriptorNode<EjbReferenceDescriptor> {

    private EjbReferenceDescriptor descriptor;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public EjbReferenceDescriptor getDescriptor() {
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
            if (parentDesc instanceof EjbReferenceContainer) {
                try {
                    descriptor = ((EjbReferenceContainer) parentDesc).getEjbReference(value);
                    DOLUtils.getDefaultLogger().finer("Applying ref runtime to " + descriptor);
                } catch (IllegalArgumentException iae) {
                    DOLUtils.getDefaultLogger().warning(iae.getMessage());
                }
            }
            if (descriptor == null) {
                DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.ADD_DESCRIPTOR_FAILURE,
                    new Object[] {"ejb-ref", value});
            }
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName name for the descriptor
     * @param ejbRef the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbReferenceDescriptor ejbRef) {
        Node ejbRefNode = appendChild(parent, nodeName);
        appendTextChild(ejbRefNode, TagNames.EJB_REFERENCE_NAME, ejbRef.getName());

        SimpleJndiName jndiName = ejbRef.getJndiName();
        EjbDescriptor ejbReferee = ejbRef.getEjbDescriptor();

        // If this is an intra-app remote ejb dependency, write out the portable jndi name
        // of the target ejb.
        if (ejbReferee != null) {
            if (!ejbRef.isLocal() && ejbRef.getType().equals(EjbSessionDescriptor.TYPE)) {
                EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbReferee;
                String intf = ejbRef.isEJB30ClientView() ? ejbRef.getEjbInterface() : ejbRef.getEjbHomeInterface();
                jndiName = sessionDesc.getPortableJndiName(intf);
            }
        }
        appendTextChild(ejbRefNode, RuntimeTagNames.JNDI_NAME, jndiName);
        return ejbRefNode;
    }


    /**
     * writes all the runtime information for ejb references
     *
     * @param parent node to add the runtime xml info
     * @param descriptor the J2EE component containing ejb references
     */
    public static void writeEjbReferences(Node parent, EjbReferenceContainer descriptor) {
        // ejb-ref*
        Iterator<EjbReferenceDescriptor> ejbRefs = descriptor.getEjbReferenceDescriptors().iterator();
        if (ejbRefs.hasNext()) {
            EjbRefNode refNode = new EjbRefNode();
            while (ejbRefs.hasNext()) {
                EjbReferenceDescriptor ejbRef = ejbRefs.next();
                if (!ejbRef.isLocal()) {
                    refNode.writeDescriptor(parent, TagNames.EJB_REFERENCE, ejbRef);
                }
            }
        }
    }
}

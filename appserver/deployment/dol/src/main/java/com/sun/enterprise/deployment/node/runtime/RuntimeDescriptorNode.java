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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * Superclass for all the runtime descriptor nodes
 *
 * @author  Jerome Dochez
 * @version
 */
public class RuntimeDescriptorNode<T> extends DeploymentDescriptorNode<T> {

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public T getDescriptor() {
        if (abstractDescriptor == null) {
            abstractDescriptor = createDescriptor();
            if (abstractDescriptor == null) {
                return (T) getParentNode().getDescriptor();
            }
        }
        return (T) abstractDescriptor;
    }


    protected Object createDescriptor() {
        return RuntimeDescriptorFactory.getDescriptor(getXMLPath());
    }


    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (getDispatchTable().containsKey(element.getQName())) {
            super.setElementValue(element, value);
        } else {
            Object o = getDescriptor();
            if (o instanceof RuntimeDescriptor) {
                RuntimeDescriptor rd = (RuntimeDescriptor) o;
                rd.setValue(element.getQName(), value);
            } else {
                DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.addDescriptorFailure",
                    new Object[] {element.getQName(), value});
            }
        }
    }


    /**
     * writes all information common to all J2EE components
     *
     * @param parent xml node parent to add the info to
     * @param descriptor the descriptor
     */
    public static void writeCommonComponentInfo(Node parent, Descriptor descriptor) {
        if (descriptor instanceof EjbReferenceContainer) {
            EjbRefNode.writeEjbReferences(parent, (EjbReferenceContainer) descriptor);
        }
        if (descriptor instanceof ResourceReferenceContainer) {
            ResourceRefNode.writeResourceReferences(parent, (ResourceReferenceContainer) descriptor);
        }
        if (descriptor instanceof ResourceEnvReferenceContainer) {
            ResourceEnvRefNode.writeResoureEnvReferences(parent, (ResourceEnvReferenceContainer) descriptor);
        }
        if (descriptor instanceof JndiNameEnvironment) {
            ServiceRefNode.writeServiceReferences(parent, (JndiNameEnvironment) descriptor);
        }
        if (descriptor instanceof MessageDestinationReferenceContainer) {
            MessageDestinationRefNode.writeMessageDestinationReferences(parent,
                (MessageDestinationReferenceContainer) descriptor);
        }
    }


    public static void writeMessageDestinationInfo(Node parent, BundleDescriptor descriptor) {
        for (Iterator iter = descriptor.getMessageDestinations().iterator(); iter.hasNext();) {
            MessageDestinationRuntimeNode node = new MessageDestinationRuntimeNode();
            node.writeDescriptor(parent, RuntimeTagNames.MESSAGE_DESTINATION,
                (MessageDestinationDescriptor) iter.next());
        }
    }

}

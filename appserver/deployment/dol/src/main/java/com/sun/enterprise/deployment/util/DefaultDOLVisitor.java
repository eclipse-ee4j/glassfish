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

package com.sun.enterprise.deployment.util;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.types.*;

import java.util.Iterator;

/**
 * Default implementation of DescriptorVisitor interface for convenience
 *
 * @author  Jerome Dochez
 * @version
 */

public class DefaultDOLVisitor implements DescriptorVisitor {

    /**
     * visits a J2EE descriptor
     * @param the descriptor
     */
    public void accept(Descriptor descriptor) {
    }

    /**
     * get the visitor for its sub descriptor
     * @param sub descriptor to return visitor for
     */
    public DescriptorVisitor getSubDescriptorVisitor(Descriptor subDescriptor) {
        return this;
    }

    protected void accept(BundleDescriptor bundleDescriptor) {
        if (bundleDescriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment nameEnvironment = (JndiNameEnvironment)bundleDescriptor;
            for (Iterator<EjbReference> itr = nameEnvironment.getEjbReferenceDescriptors().iterator();itr.hasNext();) {
                accept(itr.next());
            }

            for (Iterator<ResourceReferenceDescriptor> itr = nameEnvironment.getResourceReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next());
            }

            for (Iterator<ResourceEnvReferenceDescriptor> itr= nameEnvironment.getResourceEnvReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next());
            }

            for (Iterator<MessageDestinationReferencer> itr = nameEnvironment.getMessageDestinationReferenceDescriptors().iterator();itr.hasNext();) {
                accept(itr.next());
            }

            for (Iterator<MessageDestinationDescriptor> itr = bundleDescriptor.getMessageDestinations().iterator(); itr.hasNext();) {
                accept(itr.next());
            }

            for (Iterator<ServiceReferenceDescriptor> itr = nameEnvironment.getServiceReferenceDescriptors().iterator();itr.hasNext();) {
                accept(itr.next());
            }
        }
    }

    /**
     * visits an ejb reference for the last J2EE component visited
     * @param the ejb reference
     */
    protected void accept(EjbReference ejbRef) {
    }

    /**
     * visits a web service reference descriptor
     * @param serviceRef
     */
    protected void accept(ServiceReferenceDescriptor serviceRef) {
    }


    /**
     * visits an resource reference for the last J2EE component visited
     * @param the resource reference
     */
    protected void accept(ResourceReferenceDescriptor resRef) {
    }

    /**
     * visits an resource environment reference for the last J2EE component visited
     * @param the resource environment reference
     */
    protected void accept(ResourceEnvReferenceDescriptor resourceEnvRef) {
    }

    protected void accept(MessageDestinationReferencer msgDestReferencer) {
    }

    /**
     * visits an message destination for the last J2EE component visited
     * @param the message destination
     */
    protected void accept(MessageDestinationDescriptor msgDest) {
    }
}

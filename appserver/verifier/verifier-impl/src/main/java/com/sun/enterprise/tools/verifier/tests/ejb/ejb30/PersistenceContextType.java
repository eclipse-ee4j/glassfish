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

package com.sun.enterprise.tools.verifier.tests.ejb.ejb30;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.tools.verifier.tests.AbstractPersistenceContextType;

import java.util.Collection;

/**
 * @author bshankar@sun.com
 * @see AbstractPersistenceContextType
 */
public class PersistenceContextType extends AbstractPersistenceContextType {
    
    protected Collection<EntityManagerReferenceDescriptor>
            getEntityManagerReferenceDescriptors(Descriptor descriptor) {
        return EjbDescriptor.class.cast(descriptor)
        .getEntityManagerReferenceDescriptors();
    }
    
    protected boolean isStatefulSessionBean(Descriptor d) {
        if (d instanceof EjbSessionDescriptor) {
            String stateType = ((EjbSessionDescriptor)d).getSessionType();
            if (EjbSessionDescriptor.STATEFUL.equals(stateType)) {
                return true;
            }
        }
        return false;
    }
}


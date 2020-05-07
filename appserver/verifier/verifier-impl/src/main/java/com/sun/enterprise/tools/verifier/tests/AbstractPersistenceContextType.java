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

package com.sun.enterprise.tools.verifier.tests;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;

import java.util.Collection;
import jakarta.persistence.PersistenceContextType;

/**
 * Assertion :
 *
 *  Only Stateful Session Bean can use EXTENDED persistence context type.
 *
 * @author bshankar@sun.com
 */
public abstract class AbstractPersistenceContextType extends VerifierTest
        implements VerifierCheck {
    final static String className = AbstractPersistenceContextType.class.getName();
    
    public Result check(Descriptor descriptor) {
        
        // initialize the result object
        Result result = getInitializedResult();
        addErrorDetails(result,
                getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); //default status is PASSED
        
        if(!isStatefulSessionBean(descriptor)) {
            for (EntityManagerReferenceDescriptor emRefDesc : getEntityManagerReferenceDescriptors(descriptor)) {
                if(emRefDesc.getPersistenceContextType().equals(PersistenceContextType.EXTENDED)) {
                    String unitName = emRefDesc.getUnitName() == null ? "" : emRefDesc.getUnitName();
                    result.failed(
                            smh.getLocalString(className + ".failed", 
                            "Found a persistence unit by name [ {0} ] of EXTENDED context type.", new Object[]{unitName}));
                }
            }
        }
        return result;
    }
    
    protected abstract Collection<EntityManagerReferenceDescriptor>
            getEntityManagerReferenceDescriptors(Descriptor descriptor);
    
    protected abstract boolean isStatefulSessionBean(Descriptor d);
    
}

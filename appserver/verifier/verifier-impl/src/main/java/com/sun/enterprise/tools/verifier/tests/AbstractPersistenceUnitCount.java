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
import org.glassfish.deployment.common.RootDeploymentDescriptor;

/**
 * Assertion :
 *
 *  persistemce.xml should have atleast one persistence unit.
 *
 * @author bshankar@sun.com
 */
public abstract class AbstractPersistenceUnitCount extends VerifierTest
        implements VerifierCheck {
    final static String className = AbstractPersistenceUnitCount.class.getName();
    
    public Result check(Descriptor descriptor) {
        
        RootDeploymentDescriptor rootDescriptor = getRootDescriptor(descriptor);
        
        Result result = getInitializedResult();
        addErrorDetails(result,
                getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); //default status is PASSED
        
        if(rootDescriptor.getExtensionsDescriptors(PersistenceUnitsDescriptor.class).size() == 0)
            result.setStatus(Result.NOT_APPLICABLE);
        
        for(PersistenceUnitsDescriptor pus : rootDescriptor.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            if (pus.getPersistenceUnitDescriptors().size() == 0) {
                result.failed(
                        smh.getLocalString(
                        className + ".failed",
                        "persistence.xml in persistence unit root [ {0} ] has no persistence units.",
                        new Object[]{pus.getPuRoot()}));
            }
        }
        return result;
    }
    
    protected abstract RootDeploymentDescriptor getRootDescriptor(Descriptor descriptor);
    
}

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

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;

/**
 * Base class for EJB 3.0 session bean tests.
 * @author Vikas Awasthi
 */
public abstract class SessionBeanTest extends EjbTest {
    
    public abstract Result check(EjbSessionDescriptor descriptor) ;
    protected ComponentNameConstructor compName = null;
    protected Result result = null;
    
    public Result check(EjbDescriptor descriptor) {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor(); 
        if (descriptor instanceof EjbSessionDescriptor) 
            return check((EjbSessionDescriptor) descriptor);
        
        addNaDetails(result, compName);
        result.notApplicable(smh.getLocalString
                ("com.sun.enterprise.tools.verifier.tests.ejb.ejb30.SessionBeanTest.notApplicable",
                "Test apply only to Session Bean components"));
        return result;
    }
}

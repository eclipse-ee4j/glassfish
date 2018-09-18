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

package com.sun.enterprise.tools.verifier.tests.ejb.intf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

/** 
 *  Interface test.  
 * Verify that the bean local or remote interface class exist and is loadable.
 */
abstract public class InterfaceClassExist extends EjbTest implements EjbCheck { 
    
    
    /**
     * Following 2 methods are used to determine whether this method is being called by 
     * local/remote interface.
     */
    abstract protected String getInterfaceName(EjbDescriptor descriptor);
    abstract protected String getInterfaceType();
    
    /**  
     * Local Interface test.
     * Verify that the bean remote or local interface class exist and is loadable.
     *  
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        if (!(descriptor instanceof EjbSessionDescriptor) &&
                !(descriptor instanceof EjbEntityDescriptor)) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfaceClassExist.notApplicable1",
                    "Test apply only to session or entity beans."));
            return result;
        }
        
        if(getInterfaceName(descriptor) == null || "".equals(getInterfaceName(descriptor))){
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfaceClassExist.notApplicable2",
                    "Not Applicable because, EJB [ {0} ] does not have {1} Interface.",
                    new Object[] {descriptor.getEjbClassName(), getInterfaceType()}));
            return result;
        }
        
        // verify that the local or remote interface class exist and is loadable
        try {
            ClassLoader jcl = getVerifierContext().getClassLoader();
            Class c = Class.forName(getClassName(descriptor), false, jcl);
            if(!c.isInterface()) {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        ("com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfaceClassExist.failed",
                        "[ {0} ] is defined as a class. It should be an interface.",
                        new Object[] {getClassName(descriptor)}));
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "Error: "+ getInterfaceType() +" interface [ {0} ] does not exist or is not loadable.",
                    new Object[] {getClassName(descriptor)}));
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);	
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    getInterfaceType() + " interface [ {0} ] exist and is loadable.",
                    new Object[] {getClassName(descriptor)}));
        }
        return result;
    }
    
    private String getClassName(EjbDescriptor descriptor) {
        return getInterfaceName(descriptor);
    }
}

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
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.logging.Level;

/** 
 * Declare local and remote interfaces as public interfaces test.  
 * All enterprise bean local and/or remote interfaces must be declared as public.
 */
abstract public class InterfacePublic extends EjbTest implements EjbCheck { 
    
    /**
     * Methods to get the type of interface: local/remote and the name of the class
     */
    
    abstract protected Set<String> getInterfaceNames(EjbDescriptor descriptor);
    
    /** 
     * Declare local and remote interfaces as public interfaces test.  
     * All enterprise bean local and/or interfaces must be declared as public.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        if (!(descriptor instanceof EjbSessionDescriptor) &&
                !(descriptor instanceof EjbEntityDescriptor)) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.homeintf.HomeMethodTest.notApplicable1",
                    "Test apply only to session or entity beans."));
            return result;                
        }
        String assertionClass = "com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfacePublic";

        for (String intfName : getInterfaceNames(descriptor)) {
            try {
                ClassLoader jcl = getVerifierContext().getClassLoader();
                Class c = Class.forName(intfName, false, jcl);
                
                // local and remote interface must be defined as public
                if (!Modifier.isPublic(c.getModifiers())) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (assertionClass + ".failed",
                            "Error: [ {0} ] is not defined as a public interface.",
                            new Object[] {intfName}));
                }
            } catch (ClassNotFoundException e) {
                // ignore as it will be caught in EjbArchiveClassesLoadable
                logger.log(Level.FINER,e.getMessage(),e);
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (assertionClass + ".passed",
                            "Valid public interface(s)."));
        }
        return result;
    }
}

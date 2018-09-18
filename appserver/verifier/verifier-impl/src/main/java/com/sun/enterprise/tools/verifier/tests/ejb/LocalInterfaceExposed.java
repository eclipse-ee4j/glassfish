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

package com.sun.enterprise.tools.verifier.tests.ejb;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Local interface should not be exposed through remote interface
 *
 * @author  Sheetal Vartak
 * @version 
 */
public class LocalInterfaceExposed extends EjbTest implements EjbCheck { 
    
    /**  
     * Bean interface type test.  
     * The bean provider must provide either Local or Remote or Both interfaces
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
                            (getClass().getName()+".notApplicable1",
                            "Test apply only to session or entity beans."));
            return result;
        }
        
        EjbBundleDescriptorImpl bundle = descriptor.getEjbBundleDescriptor();
        Iterator<EjbDescriptor> iterator = (bundle.getEjbs()).iterator();
        Set<String> localInterfaces = new HashSet<String>();
        while(iterator.hasNext()) {
            EjbDescriptor entity = iterator.next();
            if (entity.getLocalClassName() != null) 
                localInterfaces.add(entity.getLocalClassName());
            localInterfaces.addAll(entity.getLocalBusinessClassNames());
        }
        ClassLoader jcl = getVerifierContext().getClassLoader();
        try { 
            Set<String> remoteInterfaces = new HashSet<String>();
            if(descriptor.getRemoteClassName()!=null)
                remoteInterfaces.add(descriptor.getRemoteClassName());
            remoteInterfaces.addAll(descriptor.getRemoteBusinessClassNames());
            
            for (String intf : remoteInterfaces) {
                Class c = Class.forName(intf, false, getVerifierContext().getClassLoader());
                Method[] methods = c.getDeclaredMethods();
                for(int i=0; i<methods.length; i++) {
                    //check all the local interfaces in the ejb bundle
                    for(Iterator itr = localInterfaces.iterator();itr.hasNext();) {
                        String localIntf = (String) itr.next();
                        Class returnType = methods[i].getReturnType();
                        if((getBaseComponentType(returnType).getName()).equals(localIntf) ||
                                (contains(methods[i].getParameterTypes(), localIntf))) {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "Error : Local Interface [ {0} ] has been " +
                                    "exposed in remote interface [ {1} ]",
                                    new Object[] {localIntf, c.getName()}));
                            return result;
                        }
                    }
                } 
            }
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid Remote interface."));
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                            (getClass().getName() + ".failedException",
                            "Error: [ {0} ] class not found.",
                            new Object[] {descriptor.getRemoteClassName()}));
        }
        return result;
    }
    /** returns true if intf is contained in this args array */
    private boolean contains(Class[] args, String intf) {
        for (int i = 0; i < args.length; i++)
            if(getBaseComponentType(args[i]).getName().equals(intf))
                return true;
        
        return false;
    }

    /** This api recursively looks for class.getComponentType. This handles 
     *  cases where array of arrays are used. */
    private Class getBaseComponentType(Class cls) {
        if(!cls.isArray())
            return cls;
        return getBaseComponentType(cls.getComponentType());
    }
}


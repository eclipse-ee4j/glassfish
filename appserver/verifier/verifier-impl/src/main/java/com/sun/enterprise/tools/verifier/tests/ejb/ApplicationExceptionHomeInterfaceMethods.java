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

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;

/** 
 * The Bean Provider defines the application exceptions in the throws clauses 
 * of the methods of the home interface.  An application exception
 * is  an exception defined in the throws clause of a method in the Bean's home
 * interface, other than java.rmi.RemoteException.  An application 
 * exception must not be defined as a subclass of the 
 * java.lang.RuntimeException, or of the java.rmi.RemoteException. These are 
 * reserved for system exceptions. 
 * The jakarta.ejb.CreateException, jakarta.ejb.RemoveException, 
 * jakarta.ejb.FinderException, and subclasses thereof, are considered to be 
 * application exceptions.
 */
public class ApplicationExceptionHomeInterfaceMethods extends EjbTest implements EjbCheck { 

    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * The Bean Provider defines the application exceptions in the throws clauses 
     * of the methods of the home interface.  An application exception
     * is  an exception defined in the throws clause of a method in the Bean's home
     * interface, other than java.rmi.RemoteException.  An application 
     * exception must not be defined as a subclass of the 
     * java.lang.RuntimeException, or of the java.rmi.RemoteException. These are 
     * reserved for system exceptions. 
     * The jakarta.ejb.CreateException, jakarta.ejb.RemoveException, 
     * jakarta.ejb.FinderException, and subclasses thereof, are considered to be 
     * application exceptions.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        if ((descriptor instanceof EjbSessionDescriptor) ||
                (descriptor instanceof EjbEntityDescriptor)) {
            if(descriptor.getHomeClassName() != null)
                commonToBothInterfaces(descriptor.getHomeClassName(),descriptor);
            if(descriptor.getLocalHomeClassName() != null)
                commonToBothInterfaces(descriptor.getLocalHomeClassName(), descriptor);
        }

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "All the methods of Home interface are defined properly"));
        }
        return result;
    }


    /**
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home Interface of the Ejb
     * This parameter may be optional depending on the test
     */

    private void commonToBothInterfaces (String home, EjbDescriptor descriptor) {

        try {
            Class c = Class.forName(home, false, getVerifierContext().getClassLoader());
            Class [] methodExceptionTypes;

            for(Method methods : c.getDeclaredMethods()) {
                methodExceptionTypes = methods.getExceptionTypes();
                // methods must also throw java.rmi.RemoteException
                if (!(EjbUtils.isValidApplicationException(methodExceptionTypes))) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "For the Interface [ {0} ] Method [ {1} ] does" +
                            " not throw valid application exceptions",
                            new Object[] {home, methods.getName()}));
                }
            } // for all the methods within the home interface class, loop

        } catch (Exception e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                    "Error: Home interface [ {0} ] does not exist or is" +
                    " not loadable within bean [ {1} ]",
                    new Object[] {home, descriptor.getName()}));
        }
    }
}

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

package com.sun.enterprise.tools.verifier.tests.ejb.session;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/**
 * Session beans home interface no finder methods name test.
 * 
 * The following are the requirements for the enterprise Bean's home interface 
 * signature: 
 * 
 * Since all session objects hide their identity, there is no need to provide 
 * a finder for them. The home interface for a session object must not define 
 * any finder methods.
 */
public class HomeInterfaceNoFinderMethodNames extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * Session beans home interface no finder methods name test.
     * 
     * The following are the requirements for the enterprise Bean's home interface 
     * signature: 
     * 
     * Since all session objects hide their identity, there is no need to provide 
     * a finder for them. The home interface for a session object must not define 
     * any finder methods.
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor instanceof EjbSessionDescriptor) {
            // RULE: Session home interface are not allowed to have any
            //       finder methods
            if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
                commonToBothInterfaces(descriptor.getHomeClassName(),(EjbSessionDescriptor)descriptor);
            if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
                commonToBothInterfaces(descriptor.getLocalHomeClassName(),(EjbSessionDescriptor)descriptor);
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "Valid: no finder methods were found.  Session bean's home interface" +
                    " is not allowed to have any finder methods."));
        }
        return result;
    }

    /**
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb.
     */

    private void commonToBothInterfaces(String home, EjbSessionDescriptor descriptor) {
        try {
            Class c = Class.forName(home,
                                false,
                                getVerifierContext().getClassLoader());
            for(Method methods : c.getDeclaredMethods()) {
                if(methods.getName().startsWith("find")) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".debug1",
                                    "For Home Interface [ {0} ] Method [ {1} ]",
                                    new Object[] {c.getName(),methods.getName()}));
                    result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".failed",
                                    "Improperly named method [ {0} ] was found. Session bean's home interface is not allowed to have any finder methods.",
                                    new Object[] {methods.getName()}));
                }
            }

        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                            "Error: Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
                            new Object[] {home, descriptor.getName()}));
        }
    }
}

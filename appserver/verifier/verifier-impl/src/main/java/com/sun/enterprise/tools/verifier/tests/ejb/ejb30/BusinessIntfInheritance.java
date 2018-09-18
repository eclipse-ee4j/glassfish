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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Set;

/**
 * A business interface must not extend javax.ejb.EJBObject or 
 * javax.ejb.EJBLocalObject.
 * 
 * @author Vikas Awasthi
 */
public class BusinessIntfInheritance extends EjbTest {
    
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = 
                getVerifierContext().getComponentNameConstructor();

        Set<String> remoteAndLocalIntfs = descriptor.getRemoteBusinessClassNames();
        remoteAndLocalIntfs.addAll(descriptor.getLocalBusinessClassNames());
        
        for (String remoteOrLocalIntf : remoteAndLocalIntfs) {
            try {
                Class c = Class.forName(remoteOrLocalIntf, 
                                        false, 
                                        getVerifierContext().getClassLoader());
                if(javax.ejb.EJBObject.class.isAssignableFrom(c) ||
                        javax.ejb.EJBLocalObject.class.isAssignableFrom(c)) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "[ {0} ] extends either javax.ejb.EJBObject " +
                                    "or javax.ejb.EJBLocalObject.",
                                    new Object[] {remoteOrLocalIntf}));
                }
            } catch (ClassNotFoundException e) {
                Verifier.debug(e);
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                                (getClass().getName() + ".failed1",
                                "Business Interface class [ {0} ] not found.",
                                new Object[] {remoteOrLocalIntf}));
            }
        }
        if(result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Business Interface(s) are valid."));
        }
        
        return result;
    }
}

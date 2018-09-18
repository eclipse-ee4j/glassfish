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
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Set;

/**
 * Business interface of an enterprise bean must be an interface and must not be
 * defined as a class.
 * 
 * @author Vikas Awasthi
 */
public class ValidBusinessInterface extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName =
                            getVerifierContext().getComponentNameConstructor();
        Set<String> remoteAndLocalIntfs = descriptor.getRemoteBusinessClassNames();
        remoteAndLocalIntfs.addAll(descriptor.getLocalBusinessClassNames());

        for (String remoteOrLocalIntf : remoteAndLocalIntfs) {
            try {
                ClassLoader classLoader = getVerifierContext().getClassLoader();
                Class c = Class.forName(remoteOrLocalIntf, false, classLoader);
                if(!c.isInterface()) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "[ {0} ] is defined as a class. It should be an interface.",
                            new Object[] {c}));
                }
            } catch (ClassNotFoundException e) {
                // ignore as it will be caught in other tests
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Business Interface(s) are valid."));
        }

        return result;
    }
}

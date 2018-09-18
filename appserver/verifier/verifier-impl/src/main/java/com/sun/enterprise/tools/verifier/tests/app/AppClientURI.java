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

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.tools.verifier.tests.app.ApplicationTest;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;

/**
 * The java element specifies the URI of a java application
 * client module, relative to the top level of the application package.
 *
 */

public class AppClientURI extends ApplicationTest implements AppCheck {
    
    
    /**
     * The java element specifies the URI of a java application
     * client module, relative to the top level of the application package.
     *
     * @param descriptor the Application deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {
        
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Result result = getInitializedResult();
        
        // java element specifies the URI of a java application
        // client module, relative to the top level of the application package
        for (Iterator itr = descriptor.getBundleDescriptors(ApplicationClientDescriptor.class).iterator(); itr.hasNext();) {
            ApplicationClientDescriptor acd = (ApplicationClientDescriptor) itr.next();
            
            // not sure what we can do to test this string?
            // as long as it's not blank, pass...
            if (!acd.getModuleDescriptor().getArchiveUri().endsWith(".jar")) {
                addErrorDetails(result, compName);
                result.failed
                        (smh.getLocalString
                        (getClass().getName() + ".failed",
                        "Error: [ {0} ] does not specify the URI [ {1} ] of an ejb-jar, relative to the top level of the application package [ {2} ], or does not end with \".jar\"",
                        new Object[] {acd.getName(), acd.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
                        
            }
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed
                    (smh.getLocalString
                    (getClass().getName() + ".passed",
                    "All the Application URIs are valid."));
        }
        
        return result;
    }
}

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

import java.util.Iterator;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.app.ApplicationTest;

/**
 * The connector element specifies the URI of a connector
 * module, relative to the top level of the application package.
 *
 */

public class ConnectorURI extends ApplicationTest implements AppCheck {
    
    /**
     * The connector element specifies the URI of a connector
     * module, relative to the top level of the application package.
     *
     * @param descriptor the Application deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Result result = getInitializedResult();
        
        for (Iterator itr = descriptor.getBundleDescriptors(ConnectorDescriptor.class).iterator(); itr.hasNext();) {
            ConnectorDescriptor cond = (ConnectorDescriptor) itr.next();
            
            if (!cond.getModuleDescriptor().getArchiveUri().endsWith(".rar")) {
                addErrorDetails(result, compName);
                result.failed
                        (smh.getLocalString
                        (getClass().getName() + ".failed",
                        "Error: [ {0} ] does not specify the URI [ {1} ] of a Connector module, relative to the top level of the application package [ {2} ], or does not end with \".rar\"",
                        new Object[] {cond.getName(), cond.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
            }
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed
                    (smh.getLocalString
                    (getClass().getName() + ".passed",
                    "All the Connector URIs are valid."));
        }
        
        return result;
    }
}

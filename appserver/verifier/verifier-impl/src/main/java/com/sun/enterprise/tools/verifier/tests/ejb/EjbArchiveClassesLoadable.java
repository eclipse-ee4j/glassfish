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
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.util.ArchiveClassesLoadableHelper;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/**
 * A j2ee archive should be self sufficient and should not depend on any classes to be 
 * available at runtime.
 * The test checks whether all the classes found in the ejb archive are loadable and the
 * classes that are referenced inside their code are also loadable within the jar. 
 *  
 * @author Vikas Awasthi
 */
public class EjbArchiveClassesLoadable extends EjbTest implements EjbCheck {
    
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
//        String archiveUri = getAbstractArchiveUri(descriptor);
        
        ClosureCompiler closureCompiler=getVerifierContext().getClosureCompiler();
        
        boolean allPassed = closureCompiler.buildClosure(descriptor.getEjbClassName());
        if (allPassed) {
            result.setStatus(Result.PASSED);
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                          (getClass().getName() + ".passed",
                           "All the classes are loadable."));
        } else {
            result.setStatus(Result.FAILED);
            addErrorDetails(result, compName);
            result.addErrorDetails(ArchiveClassesLoadableHelper.
                    getFailedResult(closureCompiler));
            result.addErrorDetails(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.loadableError",
                            "Please either bundle the above mentioned classes in the application " +
                            "or use optional packaging support for them."));
        } 
        return result;    
    }
}

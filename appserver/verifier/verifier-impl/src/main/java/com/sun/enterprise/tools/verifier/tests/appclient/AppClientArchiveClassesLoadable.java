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

package com.sun.enterprise.tools.verifier.tests.appclient;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.util.ArchiveClassesLoadableHelper;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.util.Enumeration;

/**
 * A j2ee archive should be self sufficient and should not depend on any classes to be 
 * available at runtime.
 * The test checks whether all the classes found in the appclient archive are loadable and the
 * classes that are referenced inside their code are also loadable within the jar. 
 *  
 * @author Vikas Awasthi
 */
public class AppClientArchiveClassesLoadable extends AppClientTest implements AppClientCheck {

    public Result check(ApplicationClientDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String archiveUri = getAbstractArchiveUri(descriptor);
        
        boolean allPassed = true;
        FileArchive arch = null;
        Enumeration entries= null;
        ClosureCompiler closureCompiler=getVerifierContext().getClosureCompiler();

        try {
                String uri = getAbstractArchiveUri(descriptor);
                arch = new FileArchive();
                arch.open(uri);
                entries = arch.entries();
                arch.close();
        } catch(Exception e) {
            e.printStackTrace();
            result.failed(smh.getLocalString(getClass().getName() + ".exception",
                                             "Error: [ {0} ] exception while loading the archive [ {1} ].",
                                              new Object[] {e, descriptor.getName()}));
            return result;
        }
        Object entry;
        while (entries.hasMoreElements()) {
            String name=null;
            entry  = entries.nextElement();
               name = (String)entry;
            // look for entries with .class extension
            if (name.endsWith(".class")) {
                String className = name.substring(0, name.length()-".class".length()).replace('/','.');
                boolean status=closureCompiler.buildClosure(className);
                allPassed=status && allPassed;
            }
        }
        if (allPassed) {
            result.setStatus(Result.PASSED);
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                (getClass().getName() + ".passed",
                "All the classes are loadable within [ {0} ] without any linkage error.",
                new Object[] {archiveUri}));
//            result.addGoodDetails(closureCompiler.toString());
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

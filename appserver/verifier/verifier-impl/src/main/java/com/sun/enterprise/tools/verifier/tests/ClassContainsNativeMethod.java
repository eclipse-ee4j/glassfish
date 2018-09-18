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

package com.sun.enterprise.tools.verifier.tests;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;
import org.glassfish.deployment.common.Descriptor;

import java.util.Collection;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ClassContainsNativeMethod
        extends VerifierTest implements VerifierCheck {
    public Result check(Descriptor descriptor) {
        ComponentNameConstructor compName =
                getVerifierContext().getComponentNameConstructor();
        Result result = getInitializedResult();
        result.setStatus(Result.PASSED);
        ClosureCompilerImpl cc = ClosureCompilerImpl.class.cast(
                getVerifierContext().getClosureCompiler());
        Collection<String> nativeMethods = cc.getNativeMethods();
        if(!nativeMethods.isEmpty()) {
            addWarningDetails(result, compName);
            result.warning(smh.getLocalString(getClass().getName() + ".warning",
                    "Supplied below is the list of method names " +
                    "(in the format <package.classname>.<methodName>) " +
                    "that are defined as native methods and used by the application:\n"));
            for(String m : nativeMethods) {
                result.warning("\n\t" + m);
            }
            result.warning(smh.getLocalString(getClass().getName() + ".suggestion",
                    "Please make sure that they are implemented on all operating systems."));
        }
        return result;
    }
}

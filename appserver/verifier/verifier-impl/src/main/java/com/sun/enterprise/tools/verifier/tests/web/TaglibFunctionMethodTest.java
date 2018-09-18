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

package com.sun.enterprise.tools.verifier.tests.web;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.TagLibTest;
import com.sun.enterprise.tools.verifier.web.FunctionDescriptor;

/**
 * The specified method, in function-signature element, must be a public static
 * method in the specified class, and must be specified using a fully-qualified
 * return type followed by the method name, followed by the fully-qualified
 * argument types in parenthesis, separated by commas.
 * 
 * @author Sudipto Ghosh
 */
public class TaglibFunctionMethodTest extends TagLibTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {
        ComponentNameConstructor compName =
                getVerifierContext().getComponentNameConstructor();
        Result result = getInitializedResult();
        VerifierTestContext context = getVerifierContext();
        TagLibDescriptor tlds[] = context.getTagLibDescriptors();
        FunctionDescriptor[] fnDesc = null;

        if (tlds == null) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "No tag lib files are specified"));
            return result;
        }

        for (TagLibDescriptor tld : tlds) {
            if (tld.getSpecVersion().compareTo("2.0") >= 0) {
                fnDesc = tld.getFunctionDescriptors();
                if (fnDesc != null)
                    for (FunctionDescriptor fd : fnDesc)
                        checkMethodExistence(result, fd, tld, compName);
            }
        }
        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass()
                    .getName() +
                    ".passed", "All methods defined in the function-signature element" +
                    "of the tag lib descriptor are properly defined."));
        }
        return result;
    }

    private void checkMethodExistence(Result result, FunctionDescriptor fnDesc,
                                      TagLibDescriptor tld,
                                      ComponentNameConstructor compName) {
        ClassLoader cl = getVerifierContext().getClassLoader();
        String signature = fnDesc.getFunctionSignature();
        String className = fnDesc.getFunctionClass();
        String methodName = getName(signature);
        String retType = getRetType(signature);
        String[] par = getParameters(signature);
        Class [] param = getParamTypeClass(par, cl);
        try {
            Class c = Class.forName(className, false, cl);
            boolean passed = false;
            Method[] methods= c.getMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName) &&
                        parametersMatch(m, param) &&
                        Modifier.toString(m.getModifiers()).contains("static") &&
                        returnTypeMatch(m, retType)) {
                    passed = true;
                    break;
                }
            }
            if(!passed) {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(getClass().getName() +
                        ".failed",
                        "Error: The method [ {0} ] as defined in function-signature" +
                        "element of [ {1} ] does not exists or is not a" +
                        "public static method in class [ {2} ]",
                        new Object[]{methodName, tld.getUri(), className }));
            }
        } catch (ClassNotFoundException e) {
            //do nothing. If class is not found, JSP compiler will anyway report
        }
    }
}


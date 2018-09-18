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


import com.sun.enterprise.tools.verifier.tests.TagLibTest;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.tools.verifier.web.FunctionDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 * The function-signature must be specified using a fully-qualified return type
 * followed by the method name, followed by the fully-qualified argument types
 * in parenthesis, separated by commas.
 * 
 * @author Sudipto Ghosh
 */
public class TaglibFunctionSignatureIsValid extends TagLibTest implements WebCheck {
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
                        checkSignature(result, fd, tld, compName);
            }
        }
        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass()
                    .getName() +
                    ".passed", "function-signature element of the tag lib " +
                    "descriptor are properly defined."));
        }
        return result;
    }

    /**
     * Checks the validity of the signature string contained in function-signature
     * object
     * @param result
     * @param fnDesc
     * @param tld
     * @param compName
     */
    private void checkSignature(Result result, FunctionDescriptor fnDesc,
                                      TagLibDescriptor tld,
                                      ComponentNameConstructor compName) {
        String signature = fnDesc.getFunctionSignature();
        ClassLoader cl = getVerifierContext().getClassLoader();
        String retType = getRetType(signature);
        String[] parameter = getParameters(signature);
        if (checkIfPrimitive(retType) == null && !checkValidRType(retType)) {
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString(getClass().getName() +
                    ".failed",
                    "ERROR: In the tld [ {0} ] return type is not specified correctly in " +
                    "this signature [ {1} ]",
                     new Object[]{tld.getUri(), signature}));
        }
        //parameter is a basic type or fully qualified Type
        if(!checkParamTypeClass(parameter, cl)) {
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString(getClass().getName() +
                    ".failed1",
                    "ERROR: In the tld [ {0} ] parameters are not specified correctly in " +
                    "this signature [ {1} ]",
                     new Object[]{tld.getUri(), signature}));
        }
    }

    /**
     *
     * @param retType
     * @return true if the return type is specified correctly, false otherwise
     */
    private boolean checkValidRType(String retType) {
        boolean valid = true;
        try {
            Class.forName(retType);
        } catch (ClassNotFoundException e) {
             valid=false;
        }
        return valid;
    }

    /**
     * return true, if all the parameters specified by par String[] are correctly
     * specified, false otherwise.
     * @param par
     * @param cl
     * @return
     */
    private boolean checkParamTypeClass(String[] par, ClassLoader cl) {
        for(String s : par) {
            Class c = checkIfPrimitive(s);
            if (c == null)
                try {
                    c = Class.forName(s, false, cl);
                } catch (ClassNotFoundException e) {
                    return false;
                }
        }
        return true;
    }
}

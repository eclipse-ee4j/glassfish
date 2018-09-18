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

import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;


/**
 * Listener class exists tests.
 * Verify that the Listener class exists inside the .tld file and is loadable.
 * @author Sudipto Ghosh
 */

public class TaglibListenerClassExists extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        VerifierTestContext context = getVerifierContext();
        Result result = getInitializedResult();
        TagLibDescriptor tlds[] = context.getTagLibDescriptors();
        ClassLoader cl = context.getClassLoader();

        if (tlds == null) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "No tag lib files are specified"));
            return result;
        }

        for (TagLibDescriptor tld:tlds) {
            String[] classes = tld.getListenerClasses();
            if(classes != null) {
                for ( String s : classes ) {
                    Class c = null;
                    try {
                        c = Class.forName(s, false, cl);
                    } catch (ClassNotFoundException e) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed",
                                        "Taglib listener class [ {0} ] found in " +
                                "[ {1} ] is not loadable ",
                                        new Object[] {s, tld.getUri()}));
                    }
                }
            }
        }
        if (result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed1",
                            "taglib listener classes, if any, specified in tlds are loadable"));
        }
        return result;
    }
}

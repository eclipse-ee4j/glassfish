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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.tools.verifier.web.TagDescriptor;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.deployment.WebBundleDescriptor;

/**
 * dynamic-attributes element in tag element describes whether this tag supports
 * additional attributes with dynamic names. If true, the tag-class must
 * implement the jakarta.servlet.jsp.tagext.DynamicAttributes interface.
 * Defaults to false.
 * @author Sudipto Ghosh
 *
 */
public class TagClassImplementsValidInterface extends WebTest implements WebCheck {
    public Result check(WebBundleDescriptor descriptor) {

        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        VerifierTestContext context = getVerifierContext();
        Result result = getInitializedResult();
        ClassLoader cl = context.getClassLoader();
        TagLibDescriptor tlds[] = context.getTagLibDescriptors();
        if (tlds == null) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "No tag lib files are specified"));
            return result;
        }

        for (TagLibDescriptor tld : tlds) {
            if (tld.getSpecVersion().compareTo("2.0")>=0) {
                for (TagDescriptor tagdesc : tld.getTagDescriptors()) {
                    Class c = null;
                    try {
                        c = Class.forName(tagdesc.getTagClass(), false, cl);
                    } catch (ClassNotFoundException e) {
                       //do nothing
                    }
                    if (tagdesc.getDynamicAttributes().equalsIgnoreCase("true") &&
                            !jakarta.servlet.jsp.tagext.DynamicAttributes.class.
                            isAssignableFrom(c) ) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass()
                                .getName() +
                                ".failed",
                                "Error: tag class [ {0} ] in [ {1} ] does not " +
                                "implements interface " +
                                "jakarta.servlet.jsp.tagext.DynamicAttributes",
                                new Object[]{c.getName(), tld.getUri()}));

                    }
                }
            }
        }

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass()
                                    .getName() +
                    ".passed1", "All tag-class defined in the tag lib descriptor" +
                    " files implement valid interface"));
        }
        return result;
    }
}

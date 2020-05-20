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
 * Tag class implements jakarta.servlet.jsp.tagext.JspTag for JSP version 2.0,
 * jakarta.servlet.jsp.tagext.Tag for earlier versions of JSP specification.
 *
 * @author sg133765
 */

public class TagClassExtendsValidInterface extends WebTest implements WebCheck {
    public Result check(WebBundleDescriptor descriptor) {

        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        VerifierTestContext context = getVerifierContext();
        Result result = loadWarFile(descriptor);
        TagLibDescriptor tlds[] = context.getTagLibDescriptors();
        boolean failed=false;
        boolean oneFailed = false;

        if (tlds == null) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "No tag lib files are specified"));
            return result;

        }
        for(TagLibDescriptor tld : tlds) {
            TagDescriptor[] tagDesc = tld.getTagDescriptors();
            for(TagDescriptor td : tagDesc) {
                String tagclass = td.getTagClass();
                Class c = loadClass(result, tagclass);
                if (c!=null) {
                    if (tld.getSpecVersion().trim().equalsIgnoreCase("2.0")) {
                        failed = !jakarta.servlet.jsp.tagext.JspTag.class.isAssignableFrom(c);
                    } else {
                        failed = !jakarta.servlet.jsp.tagext.Tag.class.isAssignableFrom(c);
                    }
                    if(failed) {
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.addErrorDetails(smh.getLocalString(getClass().getName() + ".failed",
                                "Error: tag class [ {0} ] in [ {1} ] does not implements valid interface",
                                new Object[] {c.getName(), tld.getUri()}));
                    } else {
                        addGoodDetails(result, compName);
                        result.addGoodDetails(smh.getLocalString
                                (getClass().getName() + ".passed1",
                                        "tag class [ {0} ] in [ {1} ] implements valid interface",
                                        new Object[] {c.getName(), tld.getUri()}));
                    }
                }
            }//for
        }
        if(oneFailed)
            result.setStatus(Result.FAILED);
        else
            result.setStatus(Result.PASSED);

        return result;
    }
}

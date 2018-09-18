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

import java.util.ArrayList;
import java.util.List;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.TagLibDescriptor;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.web.TagDescriptor;


/**
 * The name of tag must be unique.
 * @author Sudipto Ghosh
 */

public class TagNameIsUnique extends WebTest implements WebCheck {
    boolean oneFailed=false;

    public Result check(WebBundleDescriptor descriptor) {
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        VerifierTestContext context = getVerifierContext();
        Result result = loadWarFile(descriptor);

        TagLibDescriptor tlds[] = context.getTagLibDescriptors();
        if (tlds == null) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "No tag lib files are specified"));
            return result;
        }
        for(TagLibDescriptor tld : tlds) {
            TagDescriptor[] tagDesc = tld.getTagDescriptors();
            List<String> name = new ArrayList<String>();
            for(TagDescriptor td : tagDesc) {
                name.add(td.getTagName());
            }
            if (name != null) {
                String[] names = (String[])name.toArray(new String[0]);
                if (!checkForDuplicateNames(result, compName, names, tld)) {
                    addGoodDetails(result, compName);
                    result.addGoodDetails(smh.getLocalString
                            (getClass().getName() + ".passed1",
                                    "All 'name' elements are defined properly under tag element of [ {0} ]",
                                    new Object[]{tld.getUri()}));
                }
            }
        }
        if(oneFailed){
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
        return result;
    }

    public boolean checkForDuplicateNames(Result result, ComponentNameConstructor compName, String[] names, TagLibDescriptor tld) {
        boolean duplicate = false;
        for(int i=0; i<names.length-1;i++){
            for (int j=i+1; j<names.length; j++) {
                duplicate = names[i].trim().equals(names[j]);
                if(duplicate) {
                    oneFailed=true;
                    addErrorDetails(result, compName);
                    result.addErrorDetails(smh.getLocalString
                            (getClass().getName() + ".failed",
                                    "The name element value [ {0} ] under tag " +
                            "element in [ {1} ] is not unique",
                                    new Object[] {names[i], tld.getUri()}));
                }
            }
        }
        return oneFailed;
    }
}

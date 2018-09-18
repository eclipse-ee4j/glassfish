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

import com.sun.enterprise.deployment.*;
import java.io.*;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.*;

/**
 *  @author Arun Jain
 *  
 */
public abstract class Taglib extends WebTest {
    
    
    /** 
     * @param descriptor the Web deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */    
    public boolean check(WebBundleDescriptor descriptor, String taglibEntry, Result result) {
        
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String uri = getAbstractArchiveUri(descriptor);
        
        if (taglibEntry.startsWith("/"))
            taglibEntry = taglibEntry.substring(1);
        else taglibEntry = "WEB-INF/" + taglibEntry;
        File tlf = new File(uri + File.separator + taglibEntry);
        if (tlf.exists()) {
            result.addGoodDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "Tag library/.tld file exist in web application."));
            return true;
        }
        result.addErrorDetails(smh.getLocalString
                ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
        result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".failed",
                        "[ {0} ] is not a valid tld location.", new Object[] {taglibEntry}));
        return false;
    }    
}

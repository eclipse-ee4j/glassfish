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

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.deployment.common.Descriptor;

/** 
 * url-pattern element must not contain New Line (NL) or Carriage Return (CR)
 * In the schema j2ee_1_4.xsd it states that
       The url-patternType contains the url pattern of the mapping.
        It must follow the rules specified in Section 11.2 of the
        Servlet API Specification. This pattern is assumed to be in
        URL-decoded form and must not contain CR(#xD) or LF(#xA).
        If it contains those characters, the container must inform
        the developer with a descriptive error message.
 */
//This test won't be exercised now as DOL already has this test (see bug#4903530)
//But we should have it so that if any day DOL removes that test, we would catch it.
public class URLPatternContainsCRLF extends URLPattern { 

    protected void checkUrlPatternAndSetResult(String urlPattern, Descriptor descriptor, Result result, ComponentNameConstructor compName){
        if (urlPattern == null) return; //some other test takes care of this.
        // In Ascii table, Line Feed (LF) decimal value is 10 and Carriage Return (CR) decimal value is 13
        final int LF = 10, CR = 13;
        if (urlPattern.indexOf(CR)!=-1 || urlPattern.indexOf(LF)!=-1) { 
            oneFailed=true;
            result.failed(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails (smh.getLocalString
                                         (getClass().getName() + ".failed",
                                          "url-pattern [ {0} ] within [ {1} ] contains a carriage return or line feed char",
                                          new Object[] {urlPattern, descriptor.getName()}));
        } else {
            result.passed(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
            result.addGoodDetails (smh.getLocalString
                                    (getClass().getName() + ".passed",
                                     "url-pattern [ {0} ] within [ {1} ] does not contain carriage return or line feed char",
                                     new Object[] {urlPattern, descriptor.getName()}));
        }
    }
}

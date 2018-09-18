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
import java.util.jar.*;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/**
 * Welcome file element contains the file name to use as a default welcome file
 * within web application test.
 */
public class WelcomeFile extends WebTest implements WebCheck {
    
    /**
     * Welcome file element contains the file name to use as a default welcome file
     * within web application test.
     *
     * @param descriptor the Web deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        if(!isApplicable(descriptor, result)) {
            return result;
        }
        
        // Check whether the syntax of welcome-file is correct or not.
        boolean syntaxOK = checkSyntax(descriptor, result);
        
        // check whether each welcome-file exists or not
        //boolean exists = checkExists(descriptor, result);
        boolean exists = true;
        
        // report WARNING if the syntax is wrong or none of welcome-files exist.
        if (!syntaxOK) {
            result.setStatus(Result.FAILED);
        } else if (!exists) {
            result.setStatus(Result.WARNING);
        } else {
            result.setStatus(Result.PASSED);
        }
        
        return result;
    }
    
    private boolean isApplicable(WebBundleDescriptor descriptor, Result result) {
        boolean applicable = true;
        if (!descriptor.getWelcomeFiles().hasMoreElements()) {
            ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                    "There are no welcome files within the web archive [ {0} ]",
                    new Object[] {descriptor.getName()}));
            applicable = false;
        }
        return applicable;
    }
    
    private boolean checkSyntax(WebBundleDescriptor descriptor, Result result) {
        boolean syntaxOK = true;
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        for (Enumeration e = descriptor.getWelcomeFiles() ; e.hasMoreElements() ;) {
            String welcomefile = (String) e.nextElement();
            if (welcomefile.startsWith("/") || welcomefile.endsWith("/")) {
                addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString(
                        getClass().getName() + ".failed1",
                        "Error : Servlet 2.3 Spec 9.9 Welcome file URL [ {0} ] must be partial URLs with no trailing or leading /",
                        new Object[] {welcomefile, descriptor.getName()}));
                syntaxOK = false;
            }
        }
        return syntaxOK;
    }
    
    private boolean checkExists(WebBundleDescriptor descriptor, Result result) {
        findDynamicResourceURIs(descriptor);
        boolean exists = false;
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        for (Enumeration e = descriptor.getWelcomeFiles() ; e.hasMoreElements() ;) {
            String welcomeFile = (String) e.nextElement();
            if(fileExists(descriptor, welcomeFile) || urlMatches(welcomeFile)) {
                exists = true;
                addGoodDetails(result, compName);
                result.addGoodDetails(smh.getLocalString
                        (getClass().getName() + ".passed",
                        "Welcome file [ {0} ] contains the file name to use as a default welcome file within web application [ {1} ]",
                        new Object[] {welcomeFile, descriptor.getName()}));
            } else {
                addWarningDetails(result, compName);
                result.addWarningDetails(smh.getLocalString
                        (getClass().getName() + ".failed",
                        "Error: Welcome file [ {0} ] is not found within [ {1} ] or does not contain the file name to use as a default welcome file within web application [ {2} ]",
                        new Object[] {welcomeFile, descriptor.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
            }
        }
        return exists;
    }
    
    private boolean fileExists(WebBundleDescriptor descriptor, String fileName) {
        File webCompRoot = new File(getAbstractArchiveUri(descriptor));
        File welcomeFile = new File(webCompRoot, fileName);
        return welcomeFile.exists();
    }
    
    private Set dynamicResourceUrlPatterns = new HashSet();
    
    private void findDynamicResourceURIs(WebBundleDescriptor descriptor) {
        Set webComponentDescriptors = descriptor.getWebComponentDescriptors();
        for(Iterator iter = webComponentDescriptors.iterator(); iter.hasNext(); ) {
            WebComponentDescriptor webComponentDescriptor = (WebComponentDescriptor) iter.next();
            dynamicResourceUrlPatterns.addAll(webComponentDescriptor.getUrlPatternsSet());
        }
        // Remove the leading and trailing '/' character from each dynamicResourceUrlPatters
        Set newUrlPatterns = new HashSet();
        for(Iterator iter = dynamicResourceUrlPatterns.iterator(); iter.hasNext() ;) {
            String urlPattern = (String) iter.next();
            if (urlPattern.startsWith("/")) {
                urlPattern = urlPattern.substring(1);
            }
            if (urlPattern.endsWith("/")) {
                urlPattern = urlPattern.substring(0, urlPattern.length() - 1);
            }
            newUrlPatterns.add(urlPattern);
        }
        dynamicResourceUrlPatterns = newUrlPatterns;
    }
    
    private boolean urlMatches(String url) {
        for(Iterator iter = dynamicResourceUrlPatterns.iterator(); iter.hasNext() ;) {
            boolean matches = Pattern.matches((String)iter.next(), url);
            if (matches) {
                return true;
            }
        }
        return false;
    }
}

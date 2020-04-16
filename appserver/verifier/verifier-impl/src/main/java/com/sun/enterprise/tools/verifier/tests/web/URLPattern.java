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

import java.util.Enumeration;
import java.util.Iterator;

import jakarta.servlet.descriptor.*;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.ServletFilterMapping;
import com.sun.enterprise.deployment.web.WebResourceCollection;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.web.deployment.descriptor.JspConfigDescriptorImpl;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;

/**
 * The content of the url-pattern element follows the rules specified in 
 * section 10 of the servlet spec.
 * This abstract class serves as the base of some concrete classes like 
 * URLPatternErrorCheck, URLPatternWarningCheck & URLPatternContainsCRLF.
 * This class implements the check method, but inside the check method it calls a pure virtual function
 * called checkUrlPatternAndSetResult. This pure virtual function is implemented in the two derived classes.
 */
public abstract class URLPattern extends WebTest implements WebCheck {
    //These variables are needed because Result object does not maintain state.
    protected boolean oneFailed=false, oneWarning=false;

    /**
     * The content of the url-pattern element follows the rules specified in 
     * section 10 of the servlet spec.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        result.setStatus(Result.NOT_APPLICABLE);
        result.addNaDetails(smh.getLocalString
                ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
        result.addNaDetails(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                        "There is no url-pattern element within the web archive [ {0} ]",
                        new Object[] {descriptor.getName()}));
        checkWebResourceCollections(descriptor, result, compName);
        checkServletMappings(descriptor, result, compName);
        checkServletFilterMappings(descriptor, result, compName);
        checkJspGroupProperties(descriptor, result, compName);

        if(oneFailed) result.setStatus(Result.FAILED);
        else if(oneWarning) result.setStatus(Result.WARNING);
        return result;
    }

    //Each derived test should implement this method
    protected abstract void checkUrlPatternAndSetResult(String urlPattern, Descriptor descriptor, Result result, ComponentNameConstructor compName);

    private void checkWebResourceCollections(WebBundleDescriptor descriptor, Result result, ComponentNameConstructor compName){
        Enumeration e=descriptor.getSecurityConstraints();
        while (e.hasMoreElements()) {
            SecurityConstraint securityConstraint = (SecurityConstraint) e.nextElement();
            for (WebResourceCollection webResourceCollection : securityConstraint.getWebResourceCollections()) {
                for (String s : webResourceCollection.getUrlPatterns()) {
                    checkUrlPatternAndSetResult(s, descriptor, result, compName);
                }
            }
        }
    }

    private void checkServletMappings(WebBundleDescriptor descriptor, Result result, ComponentNameConstructor compName){
        for(Iterator iter=descriptor.getWebComponentDescriptors().iterator();iter.hasNext();)
            for(Iterator iter2=((WebComponentDescriptor)iter.next()).getUrlPatternsSet().iterator(); iter2.hasNext();
                checkUrlPatternAndSetResult((String)iter2.next(), descriptor, result, compName));
    }

    private void checkServletFilterMappings(WebBundleDescriptor descriptor, Result result, ComponentNameConstructor compName){
        for(Iterator iter=descriptor.getServletFilterMappings().iterator();iter.hasNext();){
            ServletFilterMapping filterMapping=(ServletFilterMapping)iter.next();
            if(filterMapping.getUrlPatterns().size() > 0) {
                for(String url : filterMapping.getUrlPatterns())
                    checkUrlPatternAndSetResult(url, descriptor, result, compName);
            }
        }
    }

    //This method checks for url-patterns appearing in jsp-config element in an web-app.
    private void checkJspGroupProperties(WebBundleDescriptor descriptor, Result result, ComponentNameConstructor compName){
        JspConfigDescriptorImpl jspC=((WebBundleDescriptorImpl)descriptor).getJspConfigDescriptor();
        if (jspC==null) return;
        for (JspPropertyGroupDescriptor desc : jspC.getJspPropertyGroups()) {
            for (String urlPattern : desc.getUrlPatterns()) {
                checkUrlPatternAndSetResult(urlPattern, descriptor, result,
                    compName);
            }
        }
    }
}

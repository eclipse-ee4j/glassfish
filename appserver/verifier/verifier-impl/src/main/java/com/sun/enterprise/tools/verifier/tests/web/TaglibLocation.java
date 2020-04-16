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

import jakarta.servlet.descriptor.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;


/**
 *  Must check for the existence of the tld file.
 *  @author     Arun Jain
 */
public class TaglibLocation extends Taglib implements WebCheck {
    

     /** 
     * .tld file exists test.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean passed = true;
        String taglibLocation;  
        Iterable<TaglibDescriptor> taglibConfig = null;
        if (((WebBundleDescriptorImpl)descriptor).getJspConfigDescriptor() != null) {
            taglibConfig = ((WebBundleDescriptorImpl)descriptor).getJspConfigDescriptor().getTaglibs();
        }

        if (taglibConfig != null) {
            for (TaglibDescriptor taglibDescriptor : taglibConfig) {
                // test all the Tag lib descriptors.
                taglibLocation = taglibDescriptor.getTaglibLocation();
                if(passed == false)
                    check(descriptor, taglibLocation, result);
                else
                    passed = check(descriptor, taglibLocation, result);         
            }
        } else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no TagLibConfigurationDescriptors within the web archive [ {0} ]",
                                  new Object[] {descriptor.getName()}));
            result.setStatus(Result.NOT_APPLICABLE);       
            return result;
            
        } 
        
        if ( passed) {
            result.setStatus(Result.PASSED);       
        }  else {
            result.setStatus(Result.FAILED);
         }
        
        return result;
    }
}

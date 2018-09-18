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

import java.io.*;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deploy.shared.FileArchive;

/**
 * Web PUBLIC identifier test
 * The Web deployment descriptor has PUBLIC identifier with a PubidLiteral 
 * of "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN" 
 */
public class WebPublicID extends WebTest implements WebCheck { 


    /** 
     * Web PUBLIC identifier test
     * The Web deployment descriptor has PUBLIC identifier with a PubidLiteral 
     * of "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN" 
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
      	String acceptablePubidLiterals[] = {
            "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN" ,
            "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" };

	String acceptableURLs[] = {"http://java.sun.com/j2ee/dtds/web-app_2_2.dtd",
				   "http://java.sun.com/dtd/web-app_2_3.dtd"};

				                           
	// open the jar and read the XML deployment descriptor
    if (descriptor.getSpecVersion().compareTo("2.4") < 0){
            
            InputStream deploymentEntry=null;
            try {
                     String uri = getAbstractArchiveUri(descriptor);
                         FileArchive arch = new FileArchive();
                         arch.open(uri);
                         deploymentEntry = arch.getEntry(
                         "WEB-INF/web.xml");
                 if (deploymentEntry != null) {
                     BufferedReader in = new BufferedReader(new InputStreamReader(deploymentEntry));
                     String s = in.readLine();
                     boolean foundDOCTYPE = false, foundPubid = false, foundURL = false;
                     while (s != null) {
                         // did we find the DOCTYPE entry? 
                         if (s.indexOf("DOCTYPE") > -1)
                             foundDOCTYPE = true;
                         if (foundDOCTYPE) {
                             for (int i=0;i<acceptablePubidLiterals.length;i++) {
                                 if (s.indexOf(acceptablePubidLiterals[i]) > -1) {
                                     foundPubid = true;
                                        result.addGoodDetails(smh.getLocalString
                                            ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                                        result.addGoodDetails
                                             (smh.getLocalString
                                             (getClass().getName() + ".passed", 
                                             "The deployment descriptor has the proper PubidLiteral: {0}", 
                                             new Object[] {acceptablePubidLiterals[i]})); 
                                 }
                                 if (s.indexOf(acceptableURLs[i]) > -1) {
                                     foundURL = true;
                                     result.addGoodDetails(smh.getLocalString
                                          ("tests.componentNameConstructor",
                                           "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                                      result.addGoodDetails
                                            (smh.getLocalString
                                            (getClass().getName() + ".passed1", 
                                            "The deployment descriptor has the proper URL corresponding the the PubIdLiteral: {0}", 
                                            new Object[] {acceptableURLs[i]})); 
                                 }
			}
		    }
		    if (foundPubid && foundURL) {
			result.setStatus(Result.PASSED);  
			break;
		    } else if(foundDOCTYPE && s.endsWith(">")) break; // DOCTYPE doesn't have any more lines to check
		    s = in.readLine();
		}
		
		if (!foundDOCTYPE){
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed
			(smh.getLocalString
			 (getClass().getName() + ".failed1", 
			  "No document type declaration found in the deployment descriptor for {0}",
			  new Object[] {descriptor.getName()}));
		} else if (!foundPubid) {
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.failed
			(smh.getLocalString
			 (getClass().getName() + ".failed2", 
			  "The deployment descriptor for {0} does not have an expected PubidLiteral ",
			  new Object[] {descriptor.getName()}));
		}else if (!foundURL) {
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
					   (getClass().getName() + ".failed", 
					    "The deployment descriptor {0} doesnot have the right URL corresponding to the PubIdLiteral", 
					    new Object[] {descriptor.getName()})); 
		}
	    }
	    
	} catch (IOException e) {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".IOException", 
			   "I/O error trying to open {0}", new Object[] {getAbstractArchiveUri(descriptor)}));
	} finally {
	    try {
              if (deploymentEntry != null)
                 deploymentEntry.close();
	    } catch (Exception x) {}
	}
        }else{
                //NOT APPLICABLE               
                result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
		    "NOT-APPLICABLE: No DOCTYPE found for [ {0} ]",
		     new Object[] {descriptor.getName()}));
            }
	return result;
    }
}

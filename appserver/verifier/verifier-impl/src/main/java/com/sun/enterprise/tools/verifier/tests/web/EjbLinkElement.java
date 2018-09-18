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

import java.util.*;
import java.util.logging.Level;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.*;


/** The value of the ejb-link element is the ejb-name of an enterprise
 * bean in the same J2EE Application archive.
 */
public class EjbLinkElement extends WebTest implements WebCheck { 


    /** The value of the ejb-link element is the ejb-name of an enterprise
     * bean in the same J2EE Application archive.
     *
     * @param descriptor the Web Application deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	boolean resolved = false;
	boolean oneFailed = false;
	int na = 0;

	// The value of the ejb-link element must be the ejb-name of an enterprise
	// bean in the same J2EE Application archive.
	String applicationName = null;
	if (!descriptor.getEjbReferenceDescriptors().isEmpty()) {
	    for (Iterator itr = descriptor.getEjbReferenceDescriptors().iterator(); 
		 itr.hasNext();) {                                                     
		EjbReferenceDescriptor nextEjbReference = (EjbReferenceDescriptor) itr.next();
		if (nextEjbReference.isLinked()) {
		    String ejb_link = nextEjbReference.getLinkName();
		    ejb_link = ejb_link.substring(ejb_link.indexOf("#") + 1);
		    // get the application descriptor and check all ejb-jars in the application
		    try {
                        Application application = descriptor.getApplication();
                        applicationName = application.getName();
//                        File tmpFile = new File(System.getProperty("java.io.tmpdir"));
//                        tmpFile = new File(tmpFile, Verifier.TMPFILENAME + ".tmp");
                        // iterate through the ejb jars in this J2EE Application
                        Set ejbBundles = application.getBundleDescriptors(EjbBundleDescriptor.class);
                        Iterator ejbBundlesIterator = ejbBundles.iterator();
                        EjbBundleDescriptor ejbBundle = null;
                        while (ejbBundlesIterator.hasNext()) {
                            ejbBundle = (EjbBundleDescriptor)ejbBundlesIterator.next();
//                            if (Verifier.getEarFile() != null){
//                                archivist.extractEntry(ejbBundle.getModuleDescriptor().getArchiveUri(), tmpFile);
//                            }
                            for (Iterator itr2 = ejbBundle.getEjbs().iterator(); itr2.hasNext();) {
                                EjbDescriptor ejbDescriptor = (EjbDescriptor) itr2.next();
                                if (ejbDescriptor.getName().equals(ejb_link)) {
                                    resolved = true;
                                    logger.log(Level.FINE, getClass().getName() + ".passed",
                                            new Object[] {ejb_link,ejbDescriptor.getName()});
                                    result.addGoodDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
				    result.addGoodDetails
					(smh.getLocalString
					 (getClass().getName() + ".passed",
					  "ejb-link [ {0} ] found same value as EJB [ {1} ]",
					  new Object[] {ejb_link,ejbDescriptor.getName()}));
				    break;
                                }
                            }
                        }
                    } catch (Exception e) {


			logger.log(Level.FINE, "com.sun.enterprise.tools.verifier.testsprint {0}", new Object[] {"[" + getClass() + "] Error: " + e.getMessage()});
			if (!oneFailed) {
                            oneFailed = true;
                        }
		    } 

		    // before you go onto the next ejb-link, tell me whether you
		    // resolved the last ejb-link okay
		    if (!resolved) {
			if (!oneFailed) {
                            oneFailed = true;
                        }
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Error: No EJB matching [ {0} ] found within [ {1} ] ear file.",
						new Object[] {ejb_link, applicationName}));
		    } else {
			// clear the resolved flag for the next ejb-link 
			resolved =false;
		    }

		} else {
		    // Cannot get the link name of an ejb reference referring 
		    // to an external bean
		    result.addNaDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.addNaDetails
			(smh.getLocalString
			 (getClass().getName() + ".notApplicable1",
			  "Not Applicable:  Cannot verify the existance of an ejb reference [ {0} ] to external bean within different .ear file.",
			  new Object[] {nextEjbReference.getName()}));
		    na++;
		}
	    }

	    if (oneFailed) {
		result.setStatus(result.FAILED);
	    } else if (na == descriptor.getEjbReferenceDescriptors().size()) {
		result.setStatus(result.NOT_APPLICABLE);
	    } else {
		result.setStatus(result.PASSED);
	    }
//            File tmpFile = new File(System.getProperty("java.io.tmpdir"));
//            tmpFile = new File(tmpFile, Verifier.TMPFILENAME + ".tmp");
//	    tmpFile.delete();
	    return result;

	} else {
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no ejb references to other beans within this web archive [ {0} ]",  
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }
}

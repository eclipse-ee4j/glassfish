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
import java.io.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * Servlet lib directory resides in WEB-INF/lib directory test.
 */
public class LibDirExists extends WebTest implements WebCheck { 

    final String servletLibDirPath = "WEB-INF/lib";
      
    /** 
     * Servlet lib directory resides in WEB-INF/lib directory test.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (!descriptor.getServletDescriptors().isEmpty()) {
	    boolean oneFailed = false;
	    int na = 0;
	    boolean foundIt = false;
	    // get the servlets in this .war
	    Set servlets = descriptor.getServletDescriptors();
	    Iterator itr = servlets.iterator();
	    // test the servlets in this .war
	    while (itr.hasNext()) {
		foundIt = false;
		WebComponentDescriptor servlet = (WebComponentDescriptor)itr.next();
//		try {
                    File warfile = new File(System.getProperty("java.io.tmpdir"));
		    warfile = new File(warfile, "wartmp");
//                    File f = Verifier.getArchiveFile(
//                             descriptor.getModuleDescriptor().getArchiveUri());
		    File warLibDir = null;

//                    if (f != null) {
//                        VerifierUtils.copyArchiveToDir(f, warfile);
//		        warLibDir = new File(warfile, servletLibDirPath);
//                    }
//                    else {
                      String uri = getAbstractArchiveUri(descriptor);
		      warLibDir = new File(uri, servletLibDirPath);
//                    }

                    if (warLibDir.isDirectory()) {
		        foundIt = true;
                    } 
/*		} catch (IOException e) {
		    if (!oneFailed ) {
			oneFailed = true;
		    }
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));

		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".IOException",
					    "Error: IOError trying to open [ {0} ], {1}",
					    new Object[] {Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri()), e.getMessage()}));
		}*/
   
		if (foundIt) {
		    result.addGoodDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "Servlet lib dir [ {0} ] resides in WEB-INF/lib directory of [ {1} ].",
					   new Object[] {servletLibDirPath,uri}));
		} else {
		    na++;
		    result.addNaDetails(smh.getLocalString
					("tests.componentNameConstructor",
					 "For [ {0} ]",
					 new Object[] {compName.toString()}));
		    result.addNaDetails(smh.getLocalString
					   (getClass().getName() + ".notApplicable2",
					    "Servlet lib dir [ {0} ] does not reside in [ {1} ].",
					    new Object[] {servletLibDirPath,uri}));
		}
	    }
            File wartmp = new File(System.getProperty("java.io.tmpdir"));
	    wartmp = new File(wartmp, "wartmp");
	    deleteDirectory(wartmp.getAbsolutePath());
	    if (na == descriptor.getServletDescriptors().size()) {
		result.setStatus(Result.NOT_APPLICABLE);
	    } else if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no servlet components within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }   
}

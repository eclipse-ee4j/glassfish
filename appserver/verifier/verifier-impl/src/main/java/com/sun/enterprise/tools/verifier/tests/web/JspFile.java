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
import com.sun.enterprise.deploy.shared.FileArchive;

/**
 * Jsp file element contains the full path to Jsp file within web application
 * test.
 */
public class JspFile extends WebTest implements WebCheck { 

    
    /**
     * Jsp file element contains the full path to Jsp file within web application
     * test.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (!descriptor.getJspDescriptors().isEmpty()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
//            ZipEntry ze=null;
//            JarFile jar=null;
            FileArchive arch=null;
	    // get the jsps in this .war
	    Set jsps = descriptor.getJspDescriptors();
	    Iterator itr = jsps.iterator();
	    // test the jsps in this .war
	    while (itr.hasNext()) {
		foundIt = false;
		WebComponentDescriptor jsp = (WebComponentDescriptor)itr.next();
		String jspFilename = jsp.getWebComponentImplementation();
            String uri = null;
                try{
//                    File f = Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
//                    if(f==null){
                        uri=getAbstractArchiveUri(descriptor);
                        try{
                            arch = new FileArchive();
                            arch.open(uri);
                        }catch(IOException e){throw e;}
//                    }
//                    else{
//                        jar = new JarFile(f);
//                    }
                    
                    if (jspFilename.startsWith("/")) 
                        jspFilename = jspFilename.substring(1);
//                    if(f!=null){
//                        ze = jar.getEntry(jspFilename);
//                        foundIt=(ze !=null);
//                    }else{ 
                        File jspf = new File(new File(arch.getURI()), jspFilename);
                        if(jspf.exists())
                            foundIt=true;
                        jspf = null;                 
//                    }
//                    if (jar!=null)
//                        jar.close();
                }catch (Exception e){
                    if (!oneFailed ) {
                        oneFailed = true;
                    }
                }
                
                if (foundIt) {
		    result.addGoodDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "Jsp file [ {0} ] contains the full path to Jsp file within web application [ {1} ]",
					   new Object[] {jspFilename, descriptor.getName()}));
		} else {
		    if (!oneFailed ) {
			oneFailed = true;
		    }
		    result.addErrorDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));

		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: Jsp file [ {0} ] is not found within [ {1} ] or does not contain the full path to Jsp file within web application [ {2} ]",
					    new Object[] {jspFilename, uri, descriptor.getName()}));
		}
	    }
	    if (oneFailed) {
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
				  "There are no Jsp components within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }
}

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
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.io.*;


/** 
 * The Web form-error-page value defines the location in the web application 
 * where the page can be used for error page can be found within web 
 * application test
 */
public class FormErrorPage extends WebTest implements WebCheck { 

    
    /** 
     * The Web form-error-page value defines the location in the web application 
     * where the page can be used for error page can be found within web 
     * application test
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor.getLoginConfiguration() != null) {
	    boolean foundIt = false;
//            ZipEntry ze=null;
//            JarFile jar=null;
            FileArchive arch=null;
            
	    String formErrorPage = descriptor.getLoginConfiguration().getFormErrorPage();
            if (formErrorPage.length() > 0) {
	       
                try{
                    
//                    File f = Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
//                    if(f==null){
                        
                        String uri=getAbstractArchiveUri(descriptor);
                        
                        try{
                            arch = new FileArchive();
                            arch.open(uri);
                        }catch(IOException e){
                            throw e;
                        }
//                    }else{
//                        jar = new JarFile(f);
//                    }
                    if (formErrorPage.startsWith("/")) 
                        formErrorPage=formErrorPage.substring(1);
//                    if (f!=null){
//                        ze = jar.getEntry(formErrorPage);
//                        foundIt = (ze != null);
//                    }
//                    else{
                        File fep = new File(new File(arch.getURI()), formErrorPage);
                        if(fep.exists())
                            foundIt=true;
                        fep = null;
//                    }
//                    if (jar!=null)
//                        jar.close();
                }catch (Exception ex) {
		    //should be aldready set?
		    foundIt = false;
	        }               
	        if (foundIt) {
		    result.addGoodDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
		    result.passed(smh.getLocalString
			          (getClass().getName() + ".passed",
			           "The form-error-page [ {0} ] value defines the location in the web application where the error page that is displayed when login is not successful can be found within web application [ {1} ]",
			           new Object[] {formErrorPage, descriptor.getName()}));
	        } else {
		    result.addErrorDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString
			      (getClass().getName() + ".failed",
			       "Error: The form-error-page [ {0} ] value does not define the location in the web application where the error page that is displayed when login is not successful can be found within web application [ {1} ]",
			       new Object[] {formErrorPage, descriptor.getName()}));
	        }
	    } else {
		result.addNaDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
	        result.notApplicable(smh.getLocalString
	    			 (getClass().getName() + ".notApplicable",
	    			  "There are no form-error-page elements within this web archive [ {0} ]",
	    			  new Object[] {descriptor.getName()}));
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no form-error-page elements within this web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}

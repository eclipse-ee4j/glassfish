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
import java.io.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deploy.shared.FileArchive;
import org.glassfish.web.deployment.descriptor.ErrorPageDescriptor;
import org.glassfish.web.deployment.descriptor.WebBundleDescriptorImpl;


/**
 * Location element contains the location of the resource in the web 
 * application
 */
public class Location extends WebTest implements WebCheck { 

    
    /**
     * Location element contains the location of the resource in the web 
     * application
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (((WebBundleDescriptorImpl)descriptor).getErrorPageDescriptors().hasMoreElements()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
//            ZipEntry ze = null;
//            JarFile jar =null;
            FileArchive arch=null;
	    // get the errorpage's in this .war
	    for (Enumeration e = ((WebBundleDescriptorImpl)descriptor).getErrorPageDescriptors() ; e.hasMoreElements() ;) {
		foundIt = false;
		ErrorPageDescriptor errorpage = (ErrorPageDescriptor) e.nextElement();
		String location = errorpage.getLocation();
            String uri = null;
                try{
//                    File f = Verifier.getArchiveFile(descriptor.getModuleDescriptor().getArchiveUri());
//                    if(f==null){
                        uri=getAbstractArchiveUri(descriptor);
                        try{
                            arch = new FileArchive();
                            arch.open(uri);
                        }catch(IOException ioe){throw ioe;}
//                    }else{
//                        jar = new JarFile(f);
//                    }
                    if (location.startsWith("/"))
                        location = location.substring(1);
//                    if (f!=null){
//                        ze = jar.getEntry(location);
//                        foundIt = (ze != null);
//                    }
//                    else{
                        File loc = new File(new File(arch.getURI()), location);
                        if(loc.exists())
                            foundIt=true;
                        loc = null;
//                    }
//                    if (jar!=null)
//                        jar.close();
                }catch (Exception ex) {
		    if (!oneFailed){
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
					   "Location [ {0} ] contains the location of the resource within web application [ {1} ]",
					   new Object[] {location, descriptor.getName()}));
		} else {
		    if (!oneFailed) {
			oneFailed = true;
		    }
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: Location [ {0} ] is not found within [ {1} ] or does not contain the location of the resource within web application [ {2} ]",
					    new Object[] {location, uri, descriptor.getName()}));
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
				  "There are no location elements within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}

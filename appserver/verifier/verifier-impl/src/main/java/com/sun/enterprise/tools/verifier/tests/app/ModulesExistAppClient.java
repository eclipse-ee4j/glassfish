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

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.tools.verifier.tests.app.ApplicationTest;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*; 
import java.util.jar.*;
import java.util.*;
import java.net.URI;

/**     
 * Application's listed J2EE modules exist in the Enterprise archive
 * The J2EE module element contains an ejb, java, or web element, which indicates 
 */
public class ModulesExistAppClient extends ApplicationTest implements AppCheck { 


    /**     
     * Application's listed J2EE modules exist in the Enterprise archive
     * The J2EE module element contains an ejb, java, or web element, which indicates 
     * the module type and contains a path to the module file
     *
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();

  
         
	if (descriptor.getBundleDescriptors(ApplicationClientDescriptor.class).size() > 0) {
	    boolean oneFailed = false;
	    for (Iterator itr = descriptor.getBundleDescriptors(ApplicationClientDescriptor.class).iterator(); itr.hasNext();) {
		ApplicationClientDescriptor acd = (ApplicationClientDescriptor) itr.next();

		if (!(acd.getModuleDescriptor().getArchiveUri().equals(""))) {
		    JarFile jarFile = null;
                    InputStream deploymentEntry=null;
                    boolean moduleDirExists = false;

//		    try {
//                        File applicationJarFile = null;
//                        if (Verifier.getEarFile() != null) {
//                           applicationJarFile = new File(Verifier.getEarFile());
//                        }

//                        if (applicationJarFile == null) {
//                            try {
                              String archBase = 
                                 getAbstractArchiveUri(descriptor);
                              String moduleName =
			         acd.getModuleDescriptor().getArchiveUri();
                              String moduleDir = FileUtils.makeFriendlyFilename(moduleName);
                              File f = new File(new File(URI.create(archBase)),
                                         moduleDir);
                              moduleDirExists = f.isDirectory();
//                            }catch (Exception e) { throw new IOException(e.getMessage());}
//                        }
//                        else {
//			  jarFile = new JarFile(applicationJarFile);
//			  ZipEntry deploymentEntry1 = jarFile.getEntry(
//                             acd.getModuleDescriptor().getArchiveUri());
//                          deploymentEntry = jarFile.getInputStream(
//                                             deploymentEntry1);
//                        }
        
			if ((deploymentEntry != null) || (moduleDirExists)) {
			    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "J2EE Application Client module [ {0} ] exists within [ {1} ].",
					   new Object[] {acd.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
			} else {
                            if (!oneFailed) {
                                oneFailed = true;
                            }
			    result.addErrorDetails(smh.getLocalString
					  (getClass().getName() + ".failed",
					   "Error: J2EE Application Client module [ {0} ] does not exist within [ {1} ].",
					   new Object[] {acd.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
			}
        
//		    } catch (FileNotFoundException ex) {
//            finally {
                        try {
                          if (jarFile != null)
                              jarFile.close();
                          if (deploymentEntry != null)
                              deploymentEntry.close();
                        } catch (Exception x) {}
//                    }

		}

	    }
            if (oneFailed) {
                result.setStatus(Result.FAILED);
            } else {
                result.setStatus(Result.PASSED);
            }
	} else {
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no java application clients in application [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }
}

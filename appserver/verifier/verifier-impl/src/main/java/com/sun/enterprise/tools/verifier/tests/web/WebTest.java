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

import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.util.io.FileUtils;

import java.io.*;


/**
 * Common code and helper methods and properties for all tests
 * in the web-app space (jsp and servlets).
 *
 * @author Jerome Dochez
 * @version 1.0
 */
abstract public class WebTest extends VerifierTest implements VerifierCheck, WebCheck {

    // variables ensuring that result details are added only once
    private boolean addedError   = false;
    private boolean addedGood    = false;
    private boolean addedNa      = false;
    private boolean addedWarning = false;

    final String separator= System.getProperty("file.separator");
    VerifierTestContext context = null;

    /**
     * <p>
     * run an individual test against the deployment descriptor for the 
     * archive the verifier is performing compliance tests against.
     * </p>
     *
     * @param descriptor deployment descriptor for the archive
     * @return result object containing the result of the individual test
     * performed
     */    
    public Result check(Descriptor descriptor) {
        return check((WebBundleDescriptor) descriptor);
    }
   
    /**
     * <p>
     * all connector tests should implement this method. it run an individual
     * test against the resource adapter deployment descriptor. 
     * </p>
     *
     * @param descriptor deployment descriptor for the archive file
     * @return result object containing the result of the individual test
     * performed
     */    
    public abstract Result check(WebBundleDescriptor descriptor);      
    
    /** 
     * load the war file 
     * 
     * @param descriptor the Enumeration
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result loadWarFile(WebBundleDescriptor descriptor) {
        
        Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	context = getVerifierContext();
        try {
            // TODO : check whether this method is required?
            WebTestsUtil webTestsUtil = WebTestsUtil.getUtil(context.getClassLoader());
//            File f = Verifier.getArchiveFile(descriptor.getModuleDescriptor().
//                     getArchiveUri());
//            if (f != null) {
//	         webTestsUtil.extractJarFile(f);
//             }
//             else {
//               // dont bother about extracting JarFile
//             }
	    //Sheetal: 09/30/02
	    //dont need to call Verifier's appendCLWithWebInfContents() since J2EEClassLoader takes care of it
	    //webTestsUtil.appendCLWithWebInfContents();
        } catch (Throwable e) {
//	    e.printStackTrace();
            Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.web.WebTest" + ".Exception",
		 "Error: Unexpected exception occurred [ {0} ]",
		 new Object[] {e.toString()}));
        }                
        return result;
    }
    
    /**
     * <p>
     * load a class from the web bundle archive file
     * </p>
     *
     * @param result to put error if necessary
     * @param className the class to load
     * @return the loaded class or null is cannot be loaded from the archive
     */
    public Class loadClass(Result result, String className) {
	
        try {
            WebTestsUtil webTestsUtil = WebTestsUtil.getUtil(context.getClassLoader());
	    //webTestsUtil.appendCLWithWebInfContents();
	    return webTestsUtil.loadClass(className);
        } catch (Throwable e) {

            // @see preVerify Method of Verifier.java
            try {
                ClassLoader cl = getVerifierContext().getAlternateClassLoader();
              if (cl == null) {
                  throw e;
               }
                Class c = cl.loadClass(className);
                return c;
             }catch(Throwable ex) {
               /*
               result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.web.WebTest.Exception",
                "Error: Unexpected exception occurred [ {0} ]",
                new Object[] {ex.toString()}));
               */
            }
        } 
        return null;
    }  

    /**
     * Method for recursively deleting all temporary directories
     */

    protected void deleteDirectory(String oneDir) {
        
        File[] listOfFiles;
        File cleanDir;
	
        cleanDir = new File(oneDir);
        if (!cleanDir.exists())  {// Nothing to do.  Return; 
	    return;
	}
        listOfFiles = cleanDir.listFiles();
        if(listOfFiles != null) {       
            for(int countFiles = 0; countFiles < listOfFiles.length; countFiles++) {                    
                if (listOfFiles[countFiles].isFile()) {
                    listOfFiles[countFiles].delete();
                } else { // It is a directory
                    String nextCleanDir =  cleanDir + separator + listOfFiles[countFiles].getName();
		    File newCleanDir = new File(nextCleanDir);
                    deleteDirectory(newCleanDir.getAbsolutePath());
                }                    
            }// End for loop
        } // End if statement            
        cleanDir.delete();
    }  

    protected String getAbstractArchiveUri(WebBundleDescriptor desc) {
        String archBase = getVerifierContext().getAbstractArchive().
                getURI().toString();
        final ModuleDescriptor moduleDescriptor = desc.getModuleDescriptor();
        if (moduleDescriptor.isStandalone()) {
            return archBase; // it must be a stand-alone module; no such physical dir exists
        } else {
            return archBase + "/" +
                    FileUtils.makeFriendlyFilename(moduleDescriptor.getArchiveUri());
        }
    }

}

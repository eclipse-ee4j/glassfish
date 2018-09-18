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

import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.io.*;
import java.util.jar.*;

/** An enterprise archive (.ear) file must contain the XML-based deployment descriptor.
 * The deployment descriptor must be named META-INF/application.xml in the JAR file.
 */
public class JarContainsXMLFile extends ApplicationTest implements AppCheck { 


    /** An enterprise archive (.ear) file must contain the XML-based deployment descriptor.
     * The deployment descriptor must be named META-INF/application.xml in the JAR file.
     * 
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();

    // This test can not have a max-version set in xml file,
    // hence we must exclude this test based on platform version.
    if(getVerifierContext().getJavaEEVersion().
            compareTo(SpecVersionMapper.JavaEEVersion_5) >= 0) {
        result.setStatus(Result.NOT_APPLICABLE);
        return result;
    }


	JarFile jarFile = null;
        InputStream deploymentEntry=null;
	try {
//	    File applicationJarFile = null;
//            if (Verifier.getEarFile() != null)
//                applicationJarFile = new File(Verifier.getEarFile());
             
	    // should try to validate against SAX XML parser before
	    // continuing, report syntax errors and drop out, otherwise
	    // continue...

//            if (applicationJarFile == null) {
//               try {
                 FileArchive arch = (FileArchive)getVerifierContext().
                                    getAbstractArchive();
                 deploymentEntry = arch.getEntry(
		               ApplicationDeploymentDescriptorFile.DESC_PATH);
//               }catch (IOException e) { throw e;}
//            }
//            else {
//
//	      jarFile = new JarFile(applicationJarFile);
//	      ZipEntry deploymentEntry1 =
//		jarFile.getEntry(ApplicationDeploymentDescriptorFile.DESC_PATH);
//              deploymentEntry = jarFile.getInputStream(deploymentEntry1);
//            }

	    if (deploymentEntry != null) {
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "Found deployment descriptor xml file [ {0} ]",
			       new Object[] {ApplicationDeploymentDescriptorFile.DESC_PATH}));
	    } else { 
		
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failed",
			       "Error: No deployment descriptor xml file found, looking for [ {0} ]",
			       new Object[] {ApplicationDeploymentDescriptorFile.DESC_PATH}));
	    }

	} catch (FileNotFoundException ex) {
	    Verifier.debug(ex);
	    
		result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException",
			   "Error: File not found trying to read deployment descriptor file [ {0} ]",
			   new Object[] {ApplicationDeploymentDescriptorFile.DESC_PATH}));
	} catch (IOException ex) {
	    Verifier.debug(ex);
	    
		result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException1",
			   "Error: IO Error trying to read deployment descriptor file [ {0} ]",
			   new Object[] {ApplicationDeploymentDescriptorFile.DESC_PATH}));
	} finally {
            try {
              if (jarFile != null)
                    jarFile.close();
              if (deploymentEntry != null)
                   deploymentEntry.close();
            } catch (Exception x) {}
        }

	return result;
    }
}

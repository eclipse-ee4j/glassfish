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

package com.sun.enterprise.tools.verifier.tests.appclient;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deploy.shared.FileArchive;

import java.io.*;
import java.util.jar.*;

/**
 * An AppClient jar file must contain the XML-based deployment descriptor.  The
 * deployment descriptor must be name META-INF/application-client-jar.xml in the
 * JAR file.
 */
public class JarContainsXML extends AppClientTest implements AppClientCheck { 

      

    /** 
     * An AppClient jar file must contain the XML-based deployment descriptor.  The
     * deployment descriptor must be name META-INF/application-client-jar.xml in the
     * JAR file.
     * 
     * @param descriptor the app-client deployment descriptor 
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(ApplicationClientDescriptor descriptor) {
	Result result = getInitializedResult();

    // This test can not have a max-version set in xml file,
    // hence we must exclude this test based on platform version.
    if(getVerifierContext().getJavaEEVersion().
            compareTo(SpecVersionMapper.JavaEEVersion_5) >= 0) {
        result.setStatus(Result.NOT_APPLICABLE);
        return result;
    }
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();


	JarFile jarFile = null;
        InputStream deploymentEntry=null;
	try {
//	    File applicationJarFile = Verifier.getAppClientJarFile(descriptor.getModuleDescriptor().getArchiveUri());

	    // should try to validate against SAX XML parser before
	    // continuing, report syntax errors and drop out, otherwise
	    // continue...
 
//           if (applicationJarFile == null) {
               String uri = getAbstractArchiveUri(descriptor);
               try {
                 FileArchive arch = new FileArchive();
                 arch.open(uri);
                 deploymentEntry = arch.getEntry(
		                   DescriptorConstants.APP_CLIENT_DD_ENTRY);
               }catch (IOException e) { throw e;}
//            }
//            else {
//
//	       jarFile = new JarFile(applicationJarFile);
//	       ZipEntry deploymentEntry1 =
//	      	   jarFile.getEntry((DescriptorConstants.APP_CLIENT_DD_ENTRY).replace('\\','/'));
//                deploymentEntry = jarFile.getInputStream(deploymentEntry1);
//            }

	    if (deploymentEntry != null) {
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "Found deployment descriptor xml file [ {0} ]",
			       new Object[] {DescriptorConstants.APP_CLIENT_DD_ENTRY}));
	    } else { 
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failed",
			       "Error: No deployment descriptor xml file found, looking for [ {0} ]",
			       new Object[] {DescriptorConstants.APP_CLIENT_DD_ENTRY}));
	    }

	} catch (FileNotFoundException ex) {
	    Verifier.debug(ex);
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException",
			   "Error: File not found trying to read deployment descriptor file [ {0} ]",
			   new Object[] {DescriptorConstants.APP_CLIENT_DD_ENTRY}));
	} catch (IOException ex) {
	    Verifier.debug(ex);
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException1",
			   "Error: IO Error trying to read deployment descriptor file [ {0} ]",
			   new Object[] {DescriptorConstants.APP_CLIENT_DD_ENTRY}));
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

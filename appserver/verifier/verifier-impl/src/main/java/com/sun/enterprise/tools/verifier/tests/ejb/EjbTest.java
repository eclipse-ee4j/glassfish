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

package com.sun.enterprise.tools.verifier.tests.ejb;


import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.WebServicesDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/**
 * Superclass for all EJB tests, contains common services.
 *
 * @author  Jerome Dochez
 * @version 
 */
public abstract class EjbTest extends VerifierTest implements VerifierCheck, EjbCheck
{
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
        
        try {
          return check((EjbDescriptor) descriptor);
        }
        catch(Throwable t) {
          // Note : We assume that each test which loads a class
          // would have its own ClassNotFoundException block
          ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
          Result r = getInitializedResult();

          if (t instanceof java.lang.NoClassDefFoundError) {
  
            String s = t.toString();
            String className = s.substring(s.indexOf(":"));
              
            addWarningDetails(r, compName);
            r.warning(smh.getLocalString
                     ("com.sun.enterprise.tools.verifier.checkinclasspath",
                      "The class [ {0} ] was not found, check manifest classpath, or make sure it is available in classpath at runtime.",
                       new Object[] {className}));
              return r;
          }
          else 
            throw new RuntimeException(t);
        }
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
    public abstract Result check(EjbDescriptor descriptor);     
    
    /**
     * <p>
     * load the declared EJB class from the archive
     * </p>
     * 
     * @param descriptor the deployment descriptors for the EJB
     * @param result result to use if the load fails
     * @return the class object for the EJB component
     */
    protected Class loadEjbClass(EjbDescriptor descriptor, Result result) {
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
       try { 
	   return Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());                            
        } catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    addErrorDetails(result, compName);
	    result.failed(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.EjbTest.failedException",
		 "Error: [ {0} ] class not found.",
		 new Object[] {descriptor.getEjbClassName()}));
            return null;
	}                                                            
    }    

    protected String getAbstractArchiveUri(EjbDescriptor desc) {
        String archBase = getVerifierContext().getAbstractArchive().
                getURI().toString();
        final ModuleDescriptor moduleDescriptor = desc.getEjbBundleDescriptor().
                getModuleDescriptor();
        if (moduleDescriptor.isStandalone()) {
            return archBase; // it must be a stand-alone module; no such physical dir exists
        } else {
            return archBase + "/" +
                    FileUtils.makeFriendlyFilename(moduleDescriptor.getArchiveUri());
        }
    }

    public boolean implementsEndpoints(EjbDescriptor descriptor) {

      BundleDescriptor bdesc = descriptor.getEjbBundleDescriptor();
      if (bdesc.hasWebServices()) {
         WebServicesDescriptor wdesc = bdesc.getWebServices();
         if (wdesc.hasEndpointsImplementedBy(descriptor))
            return true;
      }
     return false;
    }
    public String getArchiveURI(EjbDescriptor desc){
//        if (Verifier.getEarFile()==null){
            return getAbstractArchiveUri(desc);
/*
        }else{
            String uri = getVerifierContext().getStdAloneArchiveURI();
            String moduleName = desc.getEjbBundleDescriptor().
                            getModuleDescriptor().getArchiveUri();
            String moduleDir = moduleName.replace('.', '_');
            uri=uri+File.separator+moduleDir;
            return uri;

        }
*/


    }
}

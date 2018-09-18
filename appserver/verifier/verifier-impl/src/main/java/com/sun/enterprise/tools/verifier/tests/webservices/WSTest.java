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

package com.sun.enterprise.tools.verifier.tests.webservices;


import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.*;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.ModuleDescriptor;

import java.lang.ClassLoader;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.util.io.FileUtils;

import java.lang.reflect.*;

import java.io.File;

/**
 * Superclass for all EJB tests, contains common services.
 *
 * @version 
 */
/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  ; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 */

public abstract class WSTest extends VerifierTest implements VerifierCheck, WSCheck {
        
    /**
     * <p>
     * run an individual test against the deployment descriptor for the 
     * archive the verifier is performing compliance tests against.
     * </p>
     *
     * @paramm descriptor deployment descriptor for the archive
     * @return result object containing the result of the individual test
     * performed
     */    
    public Result check(Descriptor descriptor) {
        return check((WebServiceEndpoint) descriptor);
    }
   
    // tests which require the WebService descriptor can obtain it from the WebServiceEndpoint
    // similarly tests which require WebServiceDescriptor can query one more level up to get the
    // descriptor.
    /**
     * @param descriptor deployment descriptor for the archive file
     * @return result object containing the result of the individual test performed
     */    
    public abstract Result check(WebServiceEndpoint descriptor);     
    
    /**
     * <p>
     * load the declared SEI class from the archive
     * </p>
     * 
     * @param descriptor the deployment descriptors for the WebService
     * @param result result to use if the load fails
     * @return the class object for the Service Endpoint Interface 
     */
    protected Class loadSEIClass(WebServiceEndpoint descriptor, Result result) {
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
       try { 
	   VerifierTestContext context = getVerifierContext();
	   ClassLoader jcl = context.getClassLoader();
	   Class cl = Class.forName(descriptor.getServiceEndpointInterface(), false, getVerifierContext().getClassLoader());
           result.passed(smh.getLocalString (
           "com.sun.enterprise.tools.verifier.tests.webservices.clpassed", 
           "The [{0}] Class [{1}] exists and was loaded successfully.",
           new Object[] {"SEI",descriptor.getServiceEndpointInterface()}));
           return cl;
        } catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    result.failed(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.webservices.WSTest.SEIClassExists",
		 "Error: Service Endpoint Interface class [ {0} ]  not found.",
		 new Object[] {descriptor.getServiceEndpointInterface()}));
            return null;
	}                                                            
    }    

    /**
     * <p>
     * load the declared Service Impl Bean class from the archive
     * </p>
     * 
     * @param descriptor the deployment descriptors for the WebService
     * @param result result to use if the load fails
     * @return the class object for the Service Endpoint Interface 
     */
    protected Class loadImplBeanClass(WebServiceEndpoint descriptor, Result result) {
         // here we could be an EJB Endpoint or a Servlet Endpoint take care of that
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String beanClassName = null;

        if (descriptor.implementedByEjbComponent()) {
            beanClassName = descriptor.getEjbComponentImpl().getEjbClassName();
        }
        else if (descriptor.implementedByWebComponent()) {
            WebComponentDescriptor wcd = descriptor.getWebComponentImpl();
            if(wcd!=null)
                beanClassName = wcd.getWebComponentImplementation();
        }
        else {
           //result.fail, neither implemented by web nor EJB
            result.addErrorDetails(smh.getLocalString
            ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
             "Error: Unexpected error occurred [ {0} ]",
            new Object[] {"The WebService is neither implemented by an EJB nor a Servlet"}));
        }
       if (beanClassName != null) {
         try { 
	     VerifierTestContext context = getVerifierContext();
	     ClassLoader jcl = context.getClassLoader();
             return Class.forName(beanClassName, false, getVerifierContext().getClassLoader());                            
             } 
             catch (ClassNotFoundException e) {
	        Verifier.debug(e);
	        result.failed(smh.getLocalString
	    	("com.sun.enterprise.tools.verifier.tests.webservices.WSTest.BeanClassExists",
	  	 "Error: Service Endpoint Implementation Bean class [ {0} ]  not found.",
		 new Object[] {beanClassName}));
              return null;
	     }                                                            
        }
       //result.fail , beanclass name is NULL
       result.addErrorDetails(smh.getLocalString
         ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
          "Error: Unexpected error occurred [ {0} ]",
          new Object[] {"The Servlet Impl Bean class name could not be resolved"}));

       return null;
    }    

    public boolean isSEIMethod (MethodDescriptor mdesc, EjbDescriptor desc, Class sei, ClassLoader cl) {

          Method[] seiMeths = sei.getMethods();
          Method methToBeTested = null;

          try {
            methToBeTested = mdesc.getMethod(desc);
          } catch(Exception e) {
            // internal error cannot get Method for MethodDescriptor
            /*
            result.addErrorDetails(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
               "Error: Unexpected error occurred [ {0} ]",
               new Object[] {e.getMessage()}));
             */
            return false;
          }

          for (int i=0; i < seiMeths.length; i++) {
            if (WSTest.matchesSignatureAndReturn(seiMeths[i], methToBeTested))
               return true;
          }
        return false;
     }

     public static  boolean matchesSignatureAndReturn (Method meth1, Method meth2) {

        if (!(meth1.getName()).equals(meth2.getName()))
           return false;

        /*
        int mod1 = meth1.getModifiers();
        int mod2 = meth2.getModifiers();

        if (mod1 != mod2) 
           return false;
        */

        Class ret1 = meth1.getReturnType();
        Class ret2 = meth2.getReturnType();
        if (ret1 != ret2)
           return false;

        Class[] param1 = meth1.getParameterTypes();
        Class[] param2 = meth2.getParameterTypes();

        if(param1.length != param2.length)
          return false;

        for(int i = 0; i < param1.length; i++)
            if(param1[i] != param2[i])
               return false;


        // for exceptions, every exception in meth2 should be defined
        // in meth1
        Class[] excep1 = meth1.getExceptionTypes();
        Class[] excep2 = meth2.getExceptionTypes();

        for(int i = 0; i < excep2.length; i++) {
            if(!isMatching(excep2[i], excep1))
               return false;
        }

      return true;
     }

     private static boolean isMatching(Class cl, Class[] classes) {

      for (int i= 0; i < classes.length; i++) {
           /*
          if (classes[i].isAssignableFrom(cl))
             return true;
          if (cl.isAssignableFrom(classes[i]))
             return true;
          */
          if (classes[i].equals(cl))
              return true;
      }
      return false;
     }

    protected String getAbstractArchiveUri(WebServiceEndpoint desc) {
        String archBase = getVerifierContext().getAbstractArchive().
                getURI().toString();
        final ModuleDescriptor moduleDescriptor = desc.getBundleDescriptor().
                getModuleDescriptor();
        if (moduleDescriptor.isStandalone()) {
            return archBase; // it must be a stand-alone module; no such physical dir exists
        } else {
            return archBase + "/" +
                    FileUtils.makeFriendlyFilename(moduleDescriptor.getArchiveUri());
        }
    }

}

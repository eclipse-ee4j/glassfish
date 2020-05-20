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

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import java.util.*;
import java.lang.*;
import com.sun.enterprise.tools.verifier.tests.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_4; JSR109_WS_5; JSR109_WS_6; JSR109_WS_7; 
 *                   JSR109_WS_8; JSR109_WS_9; JSR109_WS_47;
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: 
 *   The Service Implementation Bean (SLSB) must have a default public constructor.
 *
 *   The Service Implementation Bean may implement the Service Endpoint 
 *   Interface, but it is not required to do so. The bean must implement all the method 
 *   signatures of the SEI. The Service Implementation Bean methods are not required to throw 
 *   javax.rmi.RemoteException. The business methods of the bean must be public and must not 
 *   be final or static. It may implement other methods in addition to those defined by the SEI.
 *
 *   The Service Implementation Bean (SLSB) class must be public, must not be final and must 
 *   not be abstract.
 *
 *   The Service Implementation Bean (SLSB)class must not define the finalize() method.
 *
 *   Currently, Service Implementation Bean (SLSB) must implement the ejbCreate() and 
 *   ejbRemove() methods which take no arguments. This is a requirement of the EJB container, 
 *   but generally can be stubbed out with an empty implementations.
 *
 *   The Stateless Session Bean must implement the jakarta.ejb.SessionBean interface either 
 *   directly or indirectly.
 *
 *   All the exceptions defined in the throws clause of the matching method of the session bean 
 *   class must be defined in the throws clause of the method of the web service endpoint 
 *   interface. 
 */
public class EJBServiceImplBeanChk extends WSTest implements WSCheck {

    /**
     * @param wsdescriptor the  WebService deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {
   
      Result result = getInitializedResult();
      ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

     EjbDescriptor descriptor = wsdescriptor.getEjbComponentImpl();

      if (descriptor != null) {

          // get hold of the ServiceImplBean Class
          String beanClass = descriptor.getEjbClassName();
          // since non-empty ness is enforced by schema, this is an internal error
          if ((beanClass == null) || ((beanClass != null) && (beanClass.length() == 0))) {
            // internal error 
             result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {"Service Implementation Bean Class Name Null"}));
          }
          Class<?> bean = null;
        
          try {
            bean = Class.forName(beanClass, false, getVerifierContext().getClassLoader());
          } catch (ClassNotFoundException e) {
            result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                "The [{0}] Class [{1}] could not be Loaded",new Object[] {"Service Impl Bean", beanClass}));
          }
        
          // get hold of the SEI Class
          String s = descriptor.getWebServiceEndpointInterfaceName();

          if ((s == null)  || (s.length() == 0)){
               // internal error, should never happen
             result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {"Service Endpoint Interface Class Name Null"}));
          }

          Class<?> sei = null;

          try {
               sei = Class.forName(s, false, getVerifierContext().getClassLoader());
          }catch(ClassNotFoundException e) {
            result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                "The [{0}] Class [{1}] could not be Loaded",new Object[] {"SEI", s}));

          }

          // it should be a stateless session bean
          boolean isSLSB = (jakarta.ejb.SessionBean.class).isAssignableFrom(bean);
          boolean implementsSEI = sei.isAssignableFrom(bean);

          if (!isSLSB) {
            //result.fail does not implement jakarta.ejb.SessionBean interface
            result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
            result.failed(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.webservices.failed", "[{0}]",
              new Object[] {"The Service Implementation Bean Does not Implement SessionBean Interface"}));
          }
          else {
            // result.passed 
             result.addGoodDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
             result.passed(smh.getLocalString (
                          "com.sun.enterprise.tools.verifier.tests.webservices.passed", "[{0}]",
                           new Object[] {"The Service Impl Bean implements SessionBean Interface"}));
          }

          EndPointImplBeanClassChecker checker = new EndPointImplBeanClassChecker(sei,bean,result,getVerifierContext().getSchemaVersion()); 

          if (implementsSEI) {
            // result.passed 
             result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
             result.passed(smh.getLocalString (
                          "com.sun.enterprise.tools.verifier.tests.webservices.passed", "[{0}]",
                           new Object[] {"The Service Impl Bean implements SEI"}));
          }
          else {
         
             // business methods of the bean should be public, not final and not static
             // This check will happen as part of this call
             Vector notImpl = checker.getSEIMethodsNotImplemented();
             if (notImpl.size() > 0) {
               // result.fail, Set the not implemented methods into the result info??
               result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
               result.failed(smh.getLocalString
                 ("com.sun.enterprise.tools.verifier.tests.webservices.failed", "[{0}]",
                 new Object[] {"The Service Implementation Bean Does not Implement ALL SEI Methods"}));
             }
             else {
               // result.pass :All SEI methods implemented
             result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
             result.passed(smh.getLocalString (
                          "com.sun.enterprise.tools.verifier.tests.webservices.passed", "[{0}]",
                           new Object[] {"The Service Impl Bean implements  all Methods of the SEI"}));
             }
          }

          // class should be public, not final and not abstract
          // should not define finalize()	
         if (checker.check(compName)) {
              // result.passed  stuff done inside the check() method nothing todo here
              result.setStatus(Result.PASSED);
          }
          else {
              // result.fail :  stuff done inside the check() method nothing todo here
              result.setStatus(Result.FAILED);
          }

      }
      else {
         // result.notapplicable
         result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
         result.notApplicable(smh.getLocalString
                 ("com.sun.enterprise.tools.verifier.tests.webservices.notapp",
                 "[{0}]", new Object[] {"Not Applicable since this is a JAX-RPC Service Endpoint"}));

      }
       return result;
    }
 }


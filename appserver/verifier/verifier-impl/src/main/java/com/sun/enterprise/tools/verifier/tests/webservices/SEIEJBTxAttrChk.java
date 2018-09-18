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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Collection;
import java.util.Iterator;

/* 
 *   @class.setup_props: ; 
 */ 

/* 
 *   @testName: check  
 *   @assertion_ids: JSR109_WS_1
 *   @test_Strategy:  
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription :If the Service Implementation Bean is an EJB, 
 *   the transaction attributes for the methods defined by the SEI do not include Mandatory.
 */ 
public class SEIEJBTxAttrChk extends WSTest implements WSCheck {

    /**
     * @param descriptor the  WebService deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint wsdescriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean pass = true;

        if (wsdescriptor.implementedByEjbComponent()) {

          EjbDescriptor descriptor = (EjbDescriptor) wsdescriptor.getEjbComponentImpl();

	  try  {
             ContainerTransaction ctx = descriptor.getContainerTransaction();

             if ((ctx != null) && 
                 (ContainerTransaction.MANDATORY.equals(ctx.getTransactionAttribute()))) {
                 // Call result.failed here : All methods are having Mandatory TX
                  result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                  result.failed(smh.getLocalString (getClass().getName() + ".failed",
                  "[{0}] of this WebService [{1}] have Mandatory Transaction Attribute.",
                  new Object[] {"All the methods", compName.toString()}));

                 return result;
             }

             Collection txMethDescs = descriptor.getTransactionMethodDescriptors();

             // get hold of the SEI Class
             String s = descriptor.getWebServiceEndpointInterfaceName();

             if (s == null) {
               // internal error, should never happen
                result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {"Service Endpoint Interface Class Name Null"}));
                pass = false;
             }	
             ClassLoader cl = getVerifierContext().getClassLoader();
             Class sei = null;

             try {
                sei = Class.forName(s, false, cl);
             }catch(ClassNotFoundException e) {
               result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {"Could not Load Service Endpoint Interface Class"}));
                pass = false;
             }

             Iterator it = txMethDescs.iterator(); 
             while (it.hasNext()) {
               // need to check if this method is part of SEI
               MethodDescriptor methdesc =(MethodDescriptor)it.next();
              if (isSEIMethod(methdesc, descriptor, sei, cl)) {
                  ctx = descriptor.getContainerTransactionFor(methdesc);
                  if ((ctx != null) && 
                     (ContainerTransaction.MANDATORY.equals(ctx.getTransactionAttribute()))) {
                     // Call result.failed here with Method details here
                     result.addErrorDetails(smh.getLocalString ("tests.componentNameConstructor",
                                   "For [ {0} ]", new Object[] {compName.toString()}));
                     result.failed(smh.getLocalString (getClass().getName() + ".failed",
                     "[{0}] of this WebService [{1}] have Mandatory Transaction Attribute.",
                     new Object[] {methdesc.getName(), compName.toString()}));
                     pass = false;
                   }
               }
             }
           } catch (Exception e) {
             // Call result.addErrorDetails here with exception details
               result.addErrorDetails(smh.getLocalString
               ("com.sun.enterprise.tools.verifier.tests.webservices.Error",
                "Error: Unexpected error occurred [ {0} ]",
                new Object[] {e.getMessage()}));
                pass = false;
           }

          if (pass) {

           result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
           result.passed(smh.getLocalString (getClass().getName() + ".passed",
                    "None of the methods of this WebService [{0}] have Mandatory Transaction Attribute.",
                           new Object[] {compName.toString()}));

          }
          
          return result;
         }
         else {
          // call result.notapplicable
          result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString (getClass().getName() + ".notapp",
                 "Not applicable since this is not an EJB Service Endpoint.")); 

          return result;
         }
       }
 }


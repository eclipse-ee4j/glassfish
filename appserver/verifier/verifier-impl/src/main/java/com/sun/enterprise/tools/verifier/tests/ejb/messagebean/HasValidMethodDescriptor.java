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

package com.sun.enterprise.tools.verifier.tests.ejb.messagebean;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;

import java.util.Collection;
import java.util.Iterator;

/**
 * Message-driven beans with container-managed transaction demarcation must use
 * Required or NotSupported transaction attribute.
 *
 * @author  Jerome Dochez
 * @version 
 */
public class HasValidMethodDescriptor extends MessageBeanTest {

  /** 
   * Run a verifier test against an individual declared message
   * drive bean component
   * 
   * @param descriptor the Enterprise Java Bean deployment descriptor
   * @return <code>Result</code> the results for this assertion
   */
  public Result check(EjbMessageBeanDescriptor descriptor) {

    Result result = getInitializedResult();
    ComponentNameConstructor compName = 
      getVerifierContext().getComponentNameConstructor();

    if (descriptor.getTransactionType().equals
        (EjbDescriptor.CONTAINER_TRANSACTION_TYPE)) {

      // returns the Message Listener methods and "ejbTimeout" if the bean is a
      // TimedObject. 
      Collection methods = descriptor.getTransactionMethodDescriptors();

      if (methods.size()==0) {
        addNaDetails(result, compName);
        result.notApplicable(smh.getLocalString
            (getClass().getName()+".notApplicable1",
             "Message-driven bean [ {0} ] does not define any method",
             new Object[] {descriptor.getName()}));                            
        return result;
      }

      Iterator iterator = methods.iterator();
      while(iterator.hasNext())
      {
        MethodDescriptor method = (MethodDescriptor) iterator.next();
        // if the MDB is also a TimedObject then don't check the
        // transaction attribute of ejbTimeout. The
        // timer/HasValidEjbTimeout test will check the transaction
        // attribute for ejbTimeout

        if( descriptor.isTimedObject() &&
            (method.getName()).equals("ejbTimeout") )
          continue;
        ContainerTransaction txAttr = descriptor.
          getContainerTransactionFor(method);
        if(txAttr == null)
        {
            if(getVerifierContext().getJavaEEVersion().compareTo(SpecVersionMapper.JavaEEVersion_5)<0) {
          // transaction attribute is not specified for method.
          addErrorDetails(result, compName);
          result.failed(smh.getLocalString
              (getClass().getName()+".failed4",
               "Error : Message-driven bean [ {0} ] method definition [ {1} ] does not have a valid container transaction descriptor.",
               new Object[] {descriptor.getName(), method.getName()}));                                 
            } // default transaction attr in EJB 3.0 is REQUIRED
          continue;
        }
        String ta = txAttr.getTransactionAttribute();
        if (ContainerTransaction.REQUIRED.equals(ta) || 
            ContainerTransaction.NOT_SUPPORTED.equals(ta)) {
          addGoodDetails(result, compName);
          result.passed(smh.getLocalString
              (getClass().getName()+".passed",
               "Message-driven bean [ {0} ] method definition [ {1} ] in assembly-descriptor is correct",
               new Object[] {descriptor.getName(), method.getName()}));                                            
        } else {
          addErrorDetails(result, compName);
          result.failed(smh.getLocalString
              (getClass().getName()+".failed3",
               "Error : Message-driven bean [ {0} ] method definition [ {1} ] transaction attribute must be Required or NotSupported",
               new Object[] {descriptor.getName(), method.getName()}));                                 
        }
      }
      return result;                    
    } else {
      addNaDetails(result, compName);
      result.notApplicable(smh.getLocalString
          (getClass().getName()+".notApplicable2",
           "Message-driven bean [ {0} ] does not use container-managed transaction",
           new Object[] {descriptor.getName()})); 
    }
    return result;
  }
}

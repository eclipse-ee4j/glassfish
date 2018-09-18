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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime.beanpool;

import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;




/** ejb [0,n]
 *    bean-pool ?
 *        steady-pool-size ? [String]
 *        pool-resize-quantity ? [String]
 *        max-pool-size ? [String]
 *        pool-idle-timeout-in-seconds ? [String]
 *        max-wait-time-in-millis ? [String]
 *
 * The bean-pool element specifies the bean pool properties for the beans
 *
 * The bean-pool is valid only for Stateless Session Beans (SSB) and
 * Message-Driven Beans (MDB).
 * @author Irfan Ahmed
 */

public class ASEjbBeanPool extends EjbTest implements EjbCheck {

    
    public BeanPoolDescriptor beanPool;
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
    	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        IASEjbExtraDescriptors ejbJar = descriptor.getIASEjbExtraDescriptors();
        if(ejbJar!=null)
        {
            try
            {
                beanPool = ejbJar.getBeanPool();
                if(beanPool!=null)
                {
                    if(descriptor instanceof EjbSessionDescriptor && ((EjbSessionDescriptor)descriptor).getSessionType().equals(EjbSessionDescriptor.STATEFUL))
                    {
                        result.addWarningDetails(smh.getLocalString
                                      ("tests.componentNameConstructor",
                                       "For [ {0} ]",
                                       new Object[] {compName.toString()}));
                        result.warning(smh.getLocalString(getClass().getName()+".warning",
                            "WARNING [AS-EJB ejb] : bean-pool should be defined for Stateless Session Beans, Entity Beans or Message Driven Beans"));
                    }else if(descriptor instanceof EjbMessageBeanDescriptor || (descriptor instanceof EjbSessionDescriptor && ((EjbSessionDescriptor)descriptor).getSessionType().equals(EjbSessionDescriptor.STATELESS)) || descriptor instanceof EjbEntityDescriptor){
                        result.addGoodDetails(smh.getLocalString
                                      ("tests.componentNameConstructor",
                                       "For [ {0} ]",
                                       new Object[] {compName.toString()}));
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB ejb] : bean-pool is correctly defined"));                
                    
                    }
                }
                else
                {
                    result.addNaDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : bean-pool element not defined"));
                }
                    return result;
            }catch(Exception ex)
            {
                result.addErrorDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                result.addErrorDetails(smh.getLocalString
                        (getClass().getName() + ".notRun",
                            "NOT RUN [AS-EJB] : Could not create a beanPool object"));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an IASEjbExtraDescriptor object"));
        }
        return result;
    }    
}
        

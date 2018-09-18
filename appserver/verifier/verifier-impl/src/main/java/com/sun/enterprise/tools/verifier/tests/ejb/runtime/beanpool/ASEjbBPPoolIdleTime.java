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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.Verifier;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *    bean-pool ?
 *        steady-pool-size ? [String]
 *        pool-resize-quantity ? [String]
 *        max-pool-size ? [String]
 *        pool-idle-timeout-in-seconds ? [String]
 *        max-wait-time-in-millis ? [String]
 *
 * The pool-idle-timeout-in-seconds specifies the maximum time for which a SSB or
 * an MDB is allowed to be idle in the bean pool.
 *
 * Valid value are 0 to MAX_LONG
 *
 *
 * @author Irfan Ahmed
 */

public class ASEjbBPPoolIdleTime extends ASEjbBeanPool
{

    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String pool = null;
        String poolIdleTimeout = null;
        try
        {
            pool = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-pool");
            if (pool!=null)
            {
                poolIdleTimeout = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-pool/pool-idle-timeout-in-seconds");
                try{
                    if (poolIdleTimeout!=null)
                    {
                        poolIdleTimeout =  poolIdleTimeout.trim();
                        if (poolIdleTimeout.length()==0)
                        {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed",
                                "FAILED [AS-EJB bean-pool] : pool-idle-timeout-in-seconds cannot be empty"));

                        }else
                        {
                            long value = new Integer(poolIdleTimeout).longValue();
                            if(value < 0  || value > Long.MAX_VALUE)
                            {
                                addErrorDetails(result, compName);
                                result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                    "FAILED [AS-EJB bean-pool] : pool-idle-timeout-in-seconds cannot be {0}. It should be between 0 and {1}",
                                    new Object[]{new Long(value),new Long(Long.MAX_VALUE)}));
                            }
                            else
                            {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed",
                                    "PASSED [AS-EJB bean-pool] : pool-idle-timeout-in-seconds is {0}",
                                    new Object[]{new Long(value)}));
                            }
                        }
                    }else // if pool-idle-timeout is not specified
                    {
                        addNaDetails(result, compName);
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                            "NOT APPLICABLE [AS-EJB bean-pool] : pool-idle-timeout-in-seconds element not defined"));
                    }
                }catch(NumberFormatException nfex){
                    Verifier.debug(nfex);
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed2",
                        "FAILED [AS-EJB bean-pool] : The value [ {0} ] for pool-idle-timeout-in-seconds is not a valid Integer number",new Object[]{poolIdleTimeout}));


                }

            }else  //if bean-pool elememnt is not present
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB] : bean-pool element not defined"));
            }

        }catch (Exception ex){
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create the descriptor object"));
        }
        return result;
    }
}

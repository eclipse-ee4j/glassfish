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

import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *    bean-pool ?
 *        steady-pool-size ? [String]
 *        resize-quantity ? [String]
 *        max-pool-size ? [String]
 *        pool-idle-timeout-in-seconds ? [String]
 *        max-wait-time-in-millis ? [String]
 *
 * The resize-quantity specifies the number of beans to be created if the
 * pool is empty
 *
 * valid values are o tp MAX_INT
 *
 *
 * @author Irfan Ahmed
 */
public class ASEjbBPPoolResizeQty extends ASEjbBeanPool
{

    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String pool = null;
        String poolResizeQty = null;
        String maxPoolSize = null;
        boolean oneFailed = false;

        try{
            pool = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-pool");
            if (pool!=null)
            {
                poolResizeQty = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-pool/resize-quantity");
                try{
                    if (poolResizeQty!=null)
                    {
                        poolResizeQty = poolResizeQty.trim();
                        if (poolResizeQty.length()==0)
                        {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed",
                                "FAILED [AS-EJB bean-pool] : resize-quantity cannot be empty"));
                        }else
                        {
                            int resizeQtyVal = Integer.parseInt(poolResizeQty);
                            if (resizeQtyVal < 0  || resizeQtyVal > Integer.MAX_VALUE)
                            {
                                addErrorDetails(result, compName);
                                result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                        "FAILED [AS-EJB bean-pool] : resize-quantity cannot be [ {0} ]. It should be between 0 and {1}",
                                        new Object[]{new Integer(poolResizeQty),new Integer(Integer.MAX_VALUE)}));
                            }else
                            {
                                int poolSizeVal=0;
                                maxPoolSize = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-pool/max-pool-size");
                                if (maxPoolSize!=null)
                                {
                                    try{
                                        poolSizeVal = Integer.parseInt(maxPoolSize);
                                    }catch(NumberFormatException nfe){
                                        oneFailed = true;
                                        addErrorDetails(result, compName);
                                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                            "FAILED [AS-EJB bean-pool] : The value [ {0} ] for max-pool-size is not a valid Integer number",new Object[]{maxPoolSize}));

                                    }
                                    if (!oneFailed){
                                        if (resizeQtyVal <= poolSizeVal)
                                        {
                                            addGoodDetails(result, compName);
                                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                                                "PASSED [AS-EJB bean-pool] : resize-quantity is [ {0} ] and is less-than/equal to max-pool-size[{1}]",
                                                new Object[]{new Integer(poolResizeQty), new Integer(maxPoolSize)}));
                                        }
                                        else
                                        {
                                            addWarningDetails(result, compName);
                                            result.warning(smh.getLocalString(getClass().getName()+".warning",
                                                "WARNING [AS-EJB bean-pool] : resize-quantity [ {0} ] is greater than max-pool-size [{1}]",new Object[]{new Integer(poolResizeQty), new Integer(maxPoolSize)}));
                                        }
                                    }
                                }else
                                {
                                    addGoodDetails(result, compName);
                                    result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                            "PASSED [AS-EJB bean-pool] : resize-quantity is [ {0} ]", new Object[]{new Integer(poolResizeQty)}));
                                }
                            }
                        }
                    }else // if resize-quantity not defined
                    {
                        addNaDetails(result, compName);
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                                "NOT APPLICABLE [AS-EJB bean-pool] : resize-quantity element not defined"));
                    }
                }catch(NumberFormatException nfex){
                    Verifier.debug(nfex);
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed3",
                            "FAILED [AS-EJB bean-pool] : The value [ {0} ] for resize-quantity is not a valid Integer number",
                            new Object[]{poolResizeQty}));
                }
            }else // if bean-pool is not defined
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                        "NOT APPLICABLE [AS-EJB] : bean-pool element not defined"));
            }
        }catch(Exception ex)
        {
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create the descriptor object"));
        }
        return result;
    }
}

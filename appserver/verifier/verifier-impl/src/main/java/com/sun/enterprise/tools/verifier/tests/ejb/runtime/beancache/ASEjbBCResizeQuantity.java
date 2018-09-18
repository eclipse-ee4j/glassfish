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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime.beancache;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;


/** ejb [0,n]
 *    bean-cache ?
 *        max-cache-size ? [String]
 *        resize-quantity ? [String]
 *        is-cache-overflow-allowed ? [String]
 *        cache-idle-timout-in-seconds ? [String]
 *        removal-timeout-in-seconds ? [String]
 *        victim-selection-policy ? [String]
 *
 * The max-cache-size specifies the maximum number of beans in the cache.
 * Valid values are between 1 and MAX_INT
 */

public class ASEjbBCResizeQuantity extends ASEjbBeanCache
{
    
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String beanCache = null;
        String resizeQty = null;
        String maxCacheSize = null;
        try{
            beanCache = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache");
            if(beanCache!=null)
            {
                try
                {
                    resizeQty = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache/resize-quantity");
                    if (resizeQty!=null)
                    {
                        resizeQty = resizeQty.trim();
                        if (resizeQty.length()==0)
                        {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed",
                                "FAILED [AS-EJB bean-cache] : resize-quantity cannot be empty"));

                        }else
                        {
                            int resizeQtyVal = Integer.parseInt(resizeQty);
                            if (resizeQtyVal < 1  || resizeQtyVal > Integer.MAX_VALUE)
                            {
                              addErrorDetails(result, compName);
                              result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                    "FAILED [AS-EJB bean-cache] : resize-quantity cannot be less than 1 or greater than {0}", new Object[]{new Integer(Integer.MAX_VALUE)}));
                            }else
                            {
                                int cacheSizeVal=0;
                                maxCacheSize = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache/max-cache-size");
                                if (maxCacheSize!=null)
                                {
                                    try{
                                        cacheSizeVal = Integer.parseInt(maxCacheSize);

                                    }catch(NumberFormatException nfe){
                                        addErrorDetails(result, compName);
                                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                            "FAILED [AS-EJB bean-cache] : The value [ {0} ] for max-cache-size is not a valid Integer number",
                                            new Object[]{maxCacheSize}));
                                        return result;
                                    }
                                    if (cacheSizeVal >= 1 && cacheSizeVal <= Integer.MAX_VALUE)
                                    {
                                        if (resizeQtyVal <= cacheSizeVal)
                                        {
                                            addGoodDetails(result, compName);
                                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                                                "PASSED [AS-EJB bean-cache] : resize-quantity is {0} and is less than max-cache-size {1}",
                                                 new Object[]{(new Integer(resizeQty)),new Integer(maxCacheSize)}));
                                        }
                                        else
                                        {
                                            addErrorDetails(result, compName);
                                            result.failed(smh.getLocalString(getClass().getName()+".failed3",
                                                    "FAILED[AS-EJB bean-cache] : resize-quantity {0} should be less than max-cache-size {1}",
                                                    new Object[]{new Integer(resizeQty), new Integer(maxCacheSize)}));
                                        }
                                    }else
                                    {
                                        addErrorDetails(result, compName);
                                        result.failed(smh.getLocalString(getClass().getName()+".failed4",
                                                "FAILED [AS-EJB" +"bean-cache] : resize-quantity should be less than max-cache-size and max-cache-size is not a valid integer"));

                                    }
                                }else
                                {
                                    addGoodDetails(result, compName);
                                    result.passed(smh.getLocalString(getClass().getName()+".passed1",
                                        "PASSED [AS-EJB bean-cache] : resize-quantity is {0}",
                                         new Object[]{(new Integer(resizeQty))}));
                                }
                            }
                        }

                    }else // resize-quantity not defined
                    {
                        addNaDetails(result, compName);
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                          "NOT APPLICABLE [AS-EJB bean-cache] : resize-quantity element is not defined"));

                    }
                }catch(NumberFormatException nfex)
                {
                    Verifier.debug(nfex);
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed5",
                            "FAILED [AS-EJB bean-cache] : The value [ {0} ] for resize-quantity is not a valid Integer number",
                            new Object[]{resizeQty}));


                }
            }else //bean-cache element not defined
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB] : bean-cache element not defined"));

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

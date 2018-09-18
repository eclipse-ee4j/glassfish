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
 *        is-cache-overflow-allowed ? [String]
 *        cache-idle-timout-in-seconds ? [String]
 *        removal-timeout-in-seconds ? [String]
 *        victim-selection-policy ? [String]
 *
 * The removal-timeout-in-seconds specifies the amount of time the bean remains
 * passivated.
 * Valid values are between 0 and MAX_LONG
 * @author Irfan Ahmed
 */

public class ASEjbBCRemovalTimeout extends ASEjbBeanCache
{
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String beanCache = null;
        String removeTime = null;
        try{
            beanCache = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache");
            if(beanCache!=null)
            {
                removeTime = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache/removal-timeout-in-seconds");
                if (removeTime!=null)
                {
                    try{
                        removeTime = removeTime.trim();
                        if (removeTime.length()==0)
                        {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed",
                                    "FAILED [AS-EJB bean-cache] : removal-timeout-in-seconds cannot be empty. It should be between 0 and {0}",
                                    new Object[]{new Long(Long.MAX_VALUE)}));
                        }else
                        {
                            long value = new Integer(removeTime).longValue();
                            if(value < 0  || value > Long.MAX_VALUE)
                            {
                                addErrorDetails(result, compName);
                                result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                    "FAILED [AS-EJB bean-cache] : removal-timeout-in-seconds cannot be {0}. It should be between 0 and {1}",
                                    new Object[]{new Long(value),new Long(Long.MAX_VALUE)}));
                            }else
                            {
                                addGoodDetails(result, compName);
                                result.passed(smh.getLocalString(getClass().getName()+".passed",
                                    "PASSED [AS-EJB bean-cache] : removal-timeout-in-seconds is {0}",
                                    new Object[]{new Long(value)}));
                            }
                        }
                    }catch(NumberFormatException nfex){
                        Verifier.debug(nfex);
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                                "FAILED [AS-EJB bean-cache] : [{0}] is not a valid Long number",new Object[]{removeTime}));
                    }
                } else // removal-timeout not defined
                {
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                                "NOT APPLICABLE [AS-EJB bean-cache] : removal-timeout-in-seconds element not defined"));
                }
            }else // bean-cache not defined
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
                            "NOT RUN [AS-EJB] : Could not create an descriptor object"));
        }
        return result;
    }
}

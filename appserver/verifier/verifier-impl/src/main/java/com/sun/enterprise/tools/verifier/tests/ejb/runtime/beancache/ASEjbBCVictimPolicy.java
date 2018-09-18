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
 * The victim-selection-policy specifies the algorithm that is used to select
 * victims.
 * Valid values are FIFO, LRU and NRU
 * @author Irfan Ahmed
 */

public class ASEjbBCVictimPolicy extends ASEjbBeanCache
{
    
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String beanCache = null;
        String victimPolicy = null;
        try{
            beanCache = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache");
            if(beanCache!=null)
            {
                victimPolicy = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache/victim-selection-policy");
                if(victimPolicy!=null)
                {
                    victimPolicy = victimPolicy.trim();
                    if(victimPolicy.length()==0)
                    {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB bean-cache] : victim-selection-policy cannot be empty. It has to be either FIFO, NRU or LRU"));
                    }else
                    {
                        if(!victimPolicy.equalsIgnoreCase("FIFO") && !victimPolicy.equalsIgnoreCase("NRU")
                                && !victimPolicy.equalsIgnoreCase("LRU"))
                        {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                    "FAILED [AS-EJB bean-cache] : victim-selection-policy cannot be [{0}]. It should be either FIFO, NRU or LRU [case insensitive]",
                                    new Object[]{victimPolicy}));
                        }
                        else
                        {
                            addGoodDetails(result, compName);
                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                                "PASSED [AS-EJB bean-cache] : victim-selection-policy is {0}",
                                 new Object[]{victimPolicy}));
                        }
                    }
                }else //victim-selection-policy not defined
                {
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB bean-cache] : victim-selection-policy element not defined"));
                
                }
            
            }else //bean-cache is not defined
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB] : bean-cache element not defined"));           
            
            }
        }catch(Exception ex){
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create the descriptor object"));        
        }
        return result;
    }
}

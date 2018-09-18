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


import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.BeanCacheDescriptor;


/** ejb [0,n]
 *    bean-cache ?
 *        max-cache-size ? [String]
 *        is-cache-overflow-allowed ? [String]
 *        cache-idle-timout-in-seconds ? [String]
 *        removal-timeout-in-seconds ? [String]
 *        victim-selection-policy ? [String]
 *
 * The bean-cache element specifies the bean cache properties for the bean.
 * This is valid only for entity beans and stateful session beans
 * @author Irfan Ahmed
 */

public class ASEjbBeanCache extends EjbTest implements EjbCheck { 

    public BeanCacheDescriptor beanCache;
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
	    ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String beanCache = null;
        try{
            beanCache = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/bean-cache");
            if(beanCache!=null)
            {
                if(descriptor instanceof EjbEntityDescriptor
                        || (descriptor instanceof EjbSessionDescriptor
                        && ((EjbSessionDescriptor)descriptor).getSessionType().equals(EjbSessionDescriptor.STATEFUL)))
                {
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                                "PASSED [AS-EJB ejb] : bean-cache Element parsed"));
                }
                else
                {
                    addWarningDetails(result, compName);
                    result.warning(smh.getLocalString(getClass().getName()+".warning1",
                            "WARNING [AS-EJB ejb] : bean-cache should be defined only for Stateful Session and Entity Beans"));
                }
            }
            else
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : bean-cache element not defined"));
            }
        }catch(Exception ex)
        {
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                    (getClass().getName() + ".notRun",
                            "NOT RUN [AS-EJB] : Could not get a beanCache object"));
        }
        return result;
    }
}

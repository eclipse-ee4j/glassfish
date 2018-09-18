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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;


/** ejb [0,n]
 *    jms-durable-subscription-name ? [String]
 *
 * The jms-durable-subscription-name is the name of a durable subscription associated
 * with a message-driven bean.
 * The name should not be a null string.
 * The value is required if destination-type is Topic
 * and subscription-durability is durable
 * @author Irfan Ahmed
 */
public class ASEjbJMSDurableSubscriptionName extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean oneFailed = false;
        try{
            EjbMessageBeanDescriptor msgBeanDesc = (EjbMessageBeanDescriptor)descriptor;
            String jmsDurableName = msgBeanDesc.getDurableSubscriptionName();
            if(jmsDurableName != null){
                if(jmsDurableName.length()==0)
                {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB ejb] : jms-durable-subscription-name cannot be an empty string value"));
                }else{
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : jms-durable-subscription-name is {0}", new Object[]{jmsDurableName}));
                }
            }else{
                if(descriptor instanceof EjbMessageBeanDescriptor){
                    if(msgBeanDesc.hasTopicDest() && msgBeanDesc.hasDurableSubscription()){
                        boolean failed = false;
                        int count = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter");
                        if (count > 0) {
                            String value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter/resource-adapter-mid");
                            if(value==null || value.length()==0){
                                failed = true;
                            }
                        } 
                        else {
                            failed = true;
                        }
                        if (failed) {
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                "FAILED [AS-EJB ejb] : jms-durable-subscription-name should be defined for an MDB with"+
                                " destination type Topic and Durable subscription type"));
                        }
                    }else{
                        addNaDetails(result, compName);
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                            "NOT APPLICABLE [AS-EJB ejb] : jms-durable-subscription-name element is not defined"));
                    }
                }else{
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : jms-durable-subscription-name element is not defined"));
                }
            }
        }catch(Exception ex){
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
        return result;
    }
}

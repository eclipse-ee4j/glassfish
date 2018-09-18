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
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;


/** ejb [0,n]
 *    jms-max-messages-load ? [String]
 *
 * The jms-max-messages-load specifies the maximum number of messages to
 * load into a JMS Session.
 * It is valid only for MDBs
 * The value should be between 1 and MAX_INT
 * @author Irfan Ahmed
 */
public class ASEjbJMSMaxMessagesLoad extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        try{
            IASEjbExtraDescriptors iasEjbDescriptor = descriptor.getIASEjbExtraDescriptors();
            int value = iasEjbDescriptor.getJmsMaxMessagesLoad();
            Integer jmsMaxMsgs = new Integer(value);
            if (jmsMaxMsgs != null){
                if(value<1 || value>Integer.MAX_VALUE){
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB ejb] : {0} is not a valid value for jms-max-messages-load. It should be " + '\n' + 
                        "between 0 and MAX_INT", new Object[]{new Integer(value)}));
                }else{
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : jms-max-messages-load is {0}", new Object[]{jmsMaxMsgs}));
                }
            }else{
                if(descriptor instanceof EjbMessageBeanDescriptor){
                    //<addition author="irfan@sun.com" [bug/rfe]-id="4724447" >
                    //Change in message output ms->jms //
                    addWarningDetails(result, compName);
                    result.warning(smh.getLocalString(getClass().getName()+".warning",
                        "WARNING [AS-EJB ejb] : jms-max-messages-load should be defined for MDBs"));
                }else{
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : jms-max-messages-load element is not defined"));
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

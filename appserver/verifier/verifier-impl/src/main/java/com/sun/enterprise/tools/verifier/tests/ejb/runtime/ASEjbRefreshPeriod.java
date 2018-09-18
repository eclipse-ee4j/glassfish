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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;


/** ejb [0,n]
 *    refresh-period-in-seconds ? [String]
 *
 * The refresh-period-in-seconds denotes the rate at which a read-only-bean
 * is refreshed.
 * Is applicable only if it is a ROB.
 * The value should be between 0 and MAX_INT
 * @author
 */
public class ASEjbRefreshPeriod extends EjbTest implements EjbCheck {

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean oneWarning = false;
        boolean oneFailed = false;
        boolean isReadOnly = false;
        String refreshPeriod = null;
        try{
            String s1 = ("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/refresh-period-in-seconds");
            refreshPeriod = getXPathValue(s1);
            
            IASEjbExtraDescriptors iasEjbExtraDesc = descriptor.getIASEjbExtraDescriptors();
            isReadOnly = iasEjbExtraDesc.isIsReadOnlyBean();
            
            if(refreshPeriod!=null)
            {
                refreshPeriod=refreshPeriod.trim();
                if(refreshPeriod.length()==0)
                {
                    oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB ejb] : refresh-period-in-seconds is invalid. It should be between 0 and " + Integer.MAX_VALUE));
                }else{
                    if(!(descriptor instanceof EjbEntityDescriptor
                    && isReadOnly)) 
                    {
                            oneWarning = true;
                            result.addWarningDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                            result.warning(smh.getLocalString(getClass().getName()+".warning",
                            "WARNING [AS-EJB ejb] : refresh-period-in-seconds should be defined for Read Only Beans."));
                        return result;
                    }
                    try{
                        int refValue = Integer.parseInt(refreshPeriod);
                        if(refValue<0 || refValue>Integer.MAX_VALUE)
                        {
                            oneFailed = true;
                            result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
                            result.failed(smh.getLocalString(getClass().getName()+".failed1",
                                "FAILED [AS-EJB ejb] : refresh-period-in-seconds cannot be greater than " + Integer.MAX_VALUE + " or less than 0"));
                        }
                        else
                            result.addGoodDetails(smh.getLocalString
                                ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                                "PASSED [AS-EJB ejb] : refresh-period-in-seconds is {0}",new Object[]{new Integer(refValue)}));
                    }catch(NumberFormatException nfex){
                        oneFailed = true;
                        Verifier.debug(nfex);
                        result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB ejb] : refresh-period-in-seconds is invalid. It should be between 0 and " + Integer.MAX_VALUE ));
                    }
                }
            }else {
                if((descriptor instanceof EjbEntityDescriptor)
                        && (isReadOnly))
                {
                    oneWarning = true;
                    result.addWarningDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                    result.warning(smh.getLocalString(getClass().getName()+".warning",
                    "WARNING [AS-EJB ejb] : refresh-period-in-seconds should be defined for Read Only Beans"));
                }
                else
                {
                    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : refresh-period-in-seconds is not defined"));
                }
            }
            if(oneWarning)
                result.setStatus(Result.WARNING);
            if(oneFailed)
                result.setStatus(Result.FAILED);
        }catch(Exception ex){
            result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
            result.failed(smh.getLocalString(getClass().getName()+".notRun",
                "NOT RUN [AS-EJB cmp] Could not create descriptor Object."));
        }
        return result;
    }
    }

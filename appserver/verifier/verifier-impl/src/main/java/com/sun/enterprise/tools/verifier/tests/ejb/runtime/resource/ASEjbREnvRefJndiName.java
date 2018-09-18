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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime.resource;

import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *   resource-env-ref [0,n]
 *       resource-env-ref-name [String]
 *       jndi-name [String]
 *
 * The jndi-name tag specifies the jndi name to which the resource environment reference
 * name is binded.
 *
 * The value of this elemnet should not be null 
 *
 * @author Irfan Ahmed
 */

public class ASEjbREnvRefJndiName extends ASEjbResEnvRef {
    public Result check(EjbDescriptor descriptor)
    {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        try
        {
            int ejbResEnvReference = getCountNodeSet("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/resource-env-ref");
            if (ejbResEnvReference>0)
            {
                for (int i=1;i<=ejbResEnvReference;i++)
                {
                    String refName = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/resource-env-ref["+i+"]/resource-env-ref-name");
                    String refJndiName = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/resource-env-ref["+i+"]/jndi-name");
                    /* Bug: 4954967. JNDI name does not have any restriction of starting with "jms/" 
                       only checking for null is sufficient for this test. */    
                    if(refJndiName == null || refJndiName.equals("")) {
                        result.failed(smh.getLocalString
                                ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB res-env-ref] : res-ref with res-ref-name {0} is not defined in the ejb-jar.xml",
                            new Object[]{refName}));
                    } else {
                        result.passed(smh.getLocalString
                                ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB res-env-ref] : jndi-name {0} is valid", new Object[]{refJndiName}));
                    }
                }
            }else
            {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
                         (getClass().getName() + ".notApplicable",
                                 "NOT-APPLICABLE: {0} Does not define any resource-env-ref Elements",
                                 new Object[] {descriptor.getName()}));
            }
        }catch(Exception ex)
        {
            result.addErrorDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.failed(smh.getLocalString(getClass().getName()+".notRun",
                    "NOT RUN [AS-EJB] Could not create descriptor Object."));

        }
        return result;
    }
}

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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *  resource-env-ref [0,n]
 *      resource-env-ref-name [String]
 *      jndi-name [String]
 *
 * The resource-env-ref holds all the runtime bindings for a resource
 * environment reference
 *
 *
 * @author Irfan Ahmed
 */

public class ASEjbResEnvRef extends EjbTest implements EjbCheck
 {
    public Result result;
    public ComponentNameConstructor compName;
    public Result check(EjbDescriptor descriptor)
    {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        try {
        int ejbResEnvReference = getCountNodeSet("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/resource-env-ref");
        if (ejbResEnvReference>0)
        {
            for (int i=1;i<=ejbResEnvReference;i++)
            {
                String refName = getXPathValue("/sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/resource-env-ref["+i+"]/resource-env-ref-name");
                try
                {
                    descriptor.getResourceEnvReferenceByName(refName);
                    result.passed(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB resource-env-ref] : res-env-ref-name {0} is verified with ejb-jar.xml",
                            new Object[]{refName}));
                }catch(IllegalArgumentException iaex)
                {
                    Verifier.debug(iaex);
                    result.failed(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB resource-env-ref] : The res-env-ref-name {0} is not defined in ejb-jar.xml for this bean",
                            new Object[]{refName}));
                }
            }
         }
        else
        {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                            " NOT-APPLICABLE: {0} Does not define any resource-env-ref Elements",
                            new Object[] {descriptor.getName()}));
        }
        } catch(Exception ex){
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

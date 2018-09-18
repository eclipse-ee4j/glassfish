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

import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;
import java.util.Set;

/** ejb [0,n]
 *  resource-ref [0,n]
 *      res-ref-name [String]
 *      jndi-name [String]
 *      default-resource-principal ?
 *          name [String]
 *          password [String]
 *
 * The resource-ref element holds the runtime bindings for a resource
 * reference declared in the ejb-jar.xml
 * @author Irfan Ahmed
 */

public class ASEjbResRef extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Set resRef = descriptor.getResourceReferenceDescriptors();
        boolean oneFailed = false;
        if(!(resRef.isEmpty()))
        {
            Iterator it = resRef.iterator();
            while (it.hasNext())
            {
                ResourceReferenceDescriptor resDesc = (ResourceReferenceDescriptor)it.next();
                String refName = resDesc.getName();
                
             try
                    {
                        descriptor.getResourceReferenceByName(refName);
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB resource-ref] : res-ref-name {0} is verified with ejb-jar.xml",
                            new Object[]{refName}));
                    }
                    catch(IllegalArgumentException iaex)
                    {
                        Verifier.debug(iaex);
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB resource-ref] : The res-ref-name {0} is not defined in ejb-jar.xml for this bean",
                            new Object[]{refName}));
                    }
             
            }
                
        }
        else
        {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                (getClass().getName() + ".notApplicable",
                    "{0} Does not define any resource-ref Elements",
                    new Object[] {descriptor.getName()}));
        }
    return result;
    }
}

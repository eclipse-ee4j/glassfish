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
 *    jndi-name ? [String]
 *
 * The jndi-name of an ejb is valid for MDBs.
 * The jndi-name should not be an empty string.
 * @author Irfan Ahmed
 */
public class ASEjbJndiName extends EjbTest implements EjbCheck {

    boolean oneFailed = false;
    boolean oneWarning = false;
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String ejbName = null, jndiName=null;
        ejbName = descriptor.getName();     //get ejb-name
        jndiName=getXPathValue("/sun-ejb-jar/enterprise-beans/ejb/jndi-name");
        if(jndiName != null){
            if(jndiName.trim().length()==0){
                check(result, descriptor, compName);
            }else{
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : jndi-name is {0}", new Object[]{jndiName}));
            }
        }else
            check(result, descriptor, compName);

        if(oneFailed)
            result.setStatus(Result.FAILED);
        else if(oneWarning)
            result.setStatus(Result.WARNING);
        return result;

    }

    public void check(Result result, EjbDescriptor descriptor, ComponentNameConstructor compName) {
        if(descriptor instanceof EjbMessageBeanDescriptor) {
            String mdbres = getXPathValue("sun-ejb-jar/enetrprise-beans/ejb/mdb-resource-adapter");
            if (mdbres != null) {
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                        "mdb-resource-adapter is defined for the EJB {0}", new Object[]{mdbres}));
            }else{
                oneFailed=true;
                addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString(getClass().getName()+".failed",
                        "jndi-name or mdb-resource-adapter should be defined for an MDB"));
            }
        }else if(descriptor.isRemoteInterfacesSupported()) {
         /** Bug#5060283 -- It is possible to use this ejb by referencing thru' ejb-ref/ejb-link.
            * Only thing is, the accessibility of the ejb is reduced.
            * It is only accessible to other clients bundled within this ear file.
            * Hence, report a warning, instead of an error.
            */
//            oneFailed=true;
//            addErrorDetails(result, compName);
//            result.addErrorDetails(smh.getLocalString(getClass().getName()+".failed1",
//                    "jndi-name should be defined for a bean implementing a remote interface"));
            oneWarning = true;
            addWarningDetails(result, compName);
            result.warning(smh.getLocalString(getClass().getName() + ".warning",
                    "WARNING [AS-EJB ejb] : jndi-name is not defined for the EJB {0} although it has a remote interface.",
                    new Object[]{descriptor.getName()}));

        }else {
            result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable", "NOT APPLICABLE"));
        }

    }
}

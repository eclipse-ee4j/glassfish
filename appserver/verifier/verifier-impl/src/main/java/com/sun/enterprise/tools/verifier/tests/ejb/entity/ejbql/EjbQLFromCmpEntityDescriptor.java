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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbql;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator;
import com.sun.jdo.spi.persistence.support.ejb.ejbqlc.EJBQLException;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;

import java.util.Collection;
import java.util.Iterator;



/**
 * This test verifies whether EJB QLs specified under <entity>
 * element have any syntax or semantic errors.
 *
 * @author	Qingqing Ouyang
 * @version
 */
public class EjbQLFromCmpEntityDescriptor extends EjbTest implements EjbCheck {

    /**
     * Implements the check on EJB QL's syntax and semantics.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
    Result result = getInitializedResult();
    ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
    try {

        if (descriptor instanceof IASEjbCMPEntityDescriptor) {
            Collection col = null;
            if(getVerifierContext().getJDOException()!=null){
                result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                result.failed (smh.getLocalString(getClass().getName() + ".failed1",
                            "Error: Exception [ {0} ] while initialising JDO code generator.",
                            new Object[] {getVerifierContext().getJDOException().getMessage()}));

                return result;
            }else{
                try{
                    JDOCodeGenerator jdc= getVerifierContext().getJDOCodeGenerator();
                    col = jdc.validate((IASEjbCMPEntityDescriptor)descriptor);
                }catch(Exception ex){
                    result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.failed (smh.getLocalString(getClass().getName() + ".failed",
                            "Error: Exception [ {0} ] when calling JDOCodeGenerator.validate().",
                            new Object[] {ex.getMessage()}));
                    return result;
                }
            }
            if (col.isEmpty()){
              result.addGoodDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
              result.passed(smh.getLocalString(getClass().getName() + ".passed",
                            "Syntax and Semantics of EJBQL Queries (if any) are correct."));

            }else{
               // collect all the EJBQL errors
               String allErrors = null;
               Iterator it = col.iterator();
               while (it.hasNext()) {
                  Exception e = (Exception)it.next();
                  if (e instanceof EJBQLException) {
                     allErrors = e.getMessage() + "\n\n";
                  }
               }

               if (allErrors != null) {
                 result.addErrorDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                 result.failed(smh.getLocalString(getClass().getName() + ".parseError",
                            "Error: Entity bean [ {0} ] has the following EJBQL error(s) [ {1} ]."
                            , new Object[] {descriptor.getEjbClassName(), "\n" + allErrors} ));

               }
               else {
                 result.addGoodDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                 result.passed(smh.getLocalString(getClass().getName() + ".passed",
                            "Syntax and Semantics of EJBQL Queries (if any) are correct."));
               }
            }

	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(
                    smh.getLocalString(getClass().getName() + ".notApplicable",
                            "Not applicable: Test only applies to container managed EJBs"));
	}
    } catch(Exception e) {
      result.addErrorDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
      result.failed (smh.getLocalString(getClass().getName() + ".failed",
                            "Error: Exception [ {0} ] when calling JDOCodeGenerator.validate().",
                            new Object[] {e.getMessage()}));
    }
      return result;
    }
}

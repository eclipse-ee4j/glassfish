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

package com.sun.enterprise.tools.verifier.tests.ejb.entity;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

/**
 * @author Sudipto Ghosh
 *
 */
public class AbstractSchemaNameIsValidJavaIdentifier extends EjbTest implements EjbCheck{
    /**
     * For an entity-bean the abstract-schema-name must be a valid Java identifier.
     * See ejb specification 2.1 section 10.3.13
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor){
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String abstractSchema = null;

        if(descriptor instanceof EjbEntityDescriptor) {
            if (((EjbEntityDescriptor)descriptor).getPersistenceType().equals(EjbEntityDescriptor.CONTAINER_PERSISTENCE)) {
                if (((EjbCMPEntityDescriptor) descriptor).getCMPVersion()==EjbCMPEntityDescriptor.CMP_2_x) {
                    abstractSchema = ((EjbCMPEntityDescriptor)descriptor).getAbstractSchemaName();
                    if(abstractSchema!=null) {
                        boolean isJavaIdentifier=true;
                        boolean startChar=Character.isJavaIdentifierStart(abstractSchema.charAt(0));
                        if (startChar) {
                            for(int i=1;i<abstractSchema.length();i++)
                                if(!Character.isJavaIdentifierPart(abstractSchema.charAt(i))) {
                                    isJavaIdentifier=false;
                                    break;
                                }
                        } else {
                            isJavaIdentifier=false;
                        }
                        if(isJavaIdentifier) {
                            result.addGoodDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                            result.passed(smh.getLocalString
                                    (getClass().getName() + ".passed",
                                            "abstract-schema-name [ {0} ] within bean [ {1} ] is a valid java identifier",
                                            new Object[] {abstractSchema, descriptor.getName()}));
                        }else{
                            result.addErrorDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[] {compName.toString()}));
                            result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                            "abstract-schema-name [ {0} ] within bean [ {1} ] is not a valid java identifier",
                                            new Object[] {abstractSchema, descriptor.getName()}));
                        }
                    }
                }
            }
        }
        if(abstractSchema==null){
            result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                            "abstract-schema-name is not defined or this is not applicable for this bean"));
        }
        return result;
    }
}

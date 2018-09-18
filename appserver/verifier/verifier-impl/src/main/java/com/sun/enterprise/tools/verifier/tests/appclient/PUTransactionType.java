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

package com.sun.enterprise.tools.verifier.tests.appclient;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import com.sun.enterprise.tools.verifier.Result;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Assertions :
 *
 *  1) A persistence unit with JTA transaction type is not supported in application client.
 *  2) Reference to a PU whose transaction type is JTA is not supported in application client.
 *
 * @author bshankar@sun.com
 */

public class PUTransactionType extends VerifierTest implements VerifierCheck {
    
    public Result check(Descriptor descriptor) {
        ApplicationClientDescriptor appClient = (ApplicationClientDescriptor) descriptor;
        Result result = getInitializedResult();
        addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); // default status is PASSED
        
        for(PersistenceUnitsDescriptor pus : appClient.getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
            for(PersistenceUnitDescriptor nextPU : pus.getPersistenceUnitDescriptors()) {
                if("JTA".equals(nextPU.getTransactionType())) {
                    result.failed(smh.getLocalString(getClass().getName() + ".puName",
                            "Found a persistence unit by name [ {0} ] in persistence unit root [ {1} ] with JTA transaction type.",
                            new Object[]{nextPU.getName(), nextPU.getPuRoot()}));
                }
            }
        }
        
        for(EntityManagerFactoryReferenceDescriptor emfRef : appClient.getEntityManagerFactoryReferenceDescriptors()) {
            String unitName = emfRef.getUnitName();
            PersistenceUnitDescriptor nextPU = appClient.findReferencedPU(unitName);
            if(nextPU == null) continue;
            if("JTA".equals(nextPU.getTransactionType())) {
                result.failed(smh.getLocalString(getClass().getName() + ".puRefName",
                        "Found a reference to a persistence unit by name [ {0} ] in persistence unit root [ {1} ] with JTA transaction type.",
                        new Object[]{nextPU.getName(), nextPU.getPuRoot()}));
            }
        }
        
        return result;
    }
    
}

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

package com.sun.enterprise.tools.verifier.tests.persistence;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitsDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.VerifierCheck;
import com.sun.enterprise.tools.verifier.tests.VerifierTest;
import java.util.List;
import java.util.ArrayList;

/**
 * A persistence unit must have a name.
 * Only one persistence unit of any given name may be defined
 * within a single EJB-JAR file, within a single WAR file,
 * within a single application client jar, or within
 * an EAR (in the EAR root or lib directory).
 * See section #6.2 of EJB 3.0 Persistence API spec
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DuplicatePUNameTest extends VerifierTest implements VerifierCheck {
    public Result check(Descriptor descriptor) {
        PersistenceUnitDescriptor pu = PersistenceUnitDescriptor.class.cast(
                descriptor);
        Result result = getInitializedResult();
        addErrorDetails(result, getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); // default status is PASSED
        int count = 0;
        for(PersistenceUnitDescriptor nextPU : getPUsInSameScope(pu)) {
            result.addErrorDetails(smh.getLocalString(getClass().getName() + "puName",
                    "Found a persistence unit by name [ {0} ] in persistence unit root [ {1} ]",
                    new Object[]{nextPU.getName(), nextPU.getPuRoot()}));
                    if (nextPU.getName().equals(pu.getName())) count++;
        }
        if (count != 1) {
            result.failed(smh.getLocalString(getClass().getName() + "failed",
                    "There are [ {0} ] number of persistence units by name [ {1} ]",
                    new Object[]{count, pu.getName()}));
        }
        return result;
    }
    
    /**
     * @return the list of PersistenceUnits which will be tested to see if they contain duplicate PU name.
     */
    private List<PersistenceUnitDescriptor> getPUsInSameScope(PersistenceUnitDescriptor pu) {
        List<PersistenceUnitDescriptor> result;
        if(pu.getParent().getParent().isApplication()) {
            // for ear, the PU name has to be unique only within a jar file.
            result = pu.getParent().getPersistenceUnitDescriptors();
        } else {
            // for war/ejb-jar/appclient-jar, PU name has to be unique within the whole BundleDescriptor.
            result = new ArrayList<PersistenceUnitDescriptor>();
            for (PersistenceUnitsDescriptor pus : pu.getParent().getParent().getExtensionsDescriptors(PersistenceUnitsDescriptor.class)) {
                for(PersistenceUnitDescriptor nextPU : pus.getPersistenceUnitDescriptors()) {
                    result.add(nextPU);
                }
            }
        }
        return result;
    }
    
}

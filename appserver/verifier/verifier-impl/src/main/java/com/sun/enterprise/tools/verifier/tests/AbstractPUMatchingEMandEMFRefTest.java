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

package com.sun.enterprise.tools.verifier.tests;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.deployment.common.Descriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * For every entity manager reference and entity manager factory reference
 * in a component (ejb/servlet/app-client etc), there must be a matching
 * persistence unit defined in the scope of that component.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractPUMatchingEMandEMFRefTest extends VerifierTest
        implements VerifierCheck {
    final static String className = AbstractPUMatchingEMandEMFRefTest.class.getName();
    public Result check(Descriptor descriptor) {
        // initialize the result object
        Result result = getInitializedResult();
        addErrorDetails(result,
                getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); //default status is PASSED
        
        BundleDescriptor bundleDescriptor = getBundleDescriptor(descriptor);
        
        for (EntityManagerReferenceDescriptor emRefDesc : getEntityManagerReferenceDescriptors(descriptor)) {
            String referringUnitName = emRefDesc.getUnitName();
            PersistenceUnitDescriptor pu = bundleDescriptor.findReferencedPU(referringUnitName);
            if (pu == null) {
                result.failed(smh.getLocalString(
                        className + "failed",
                        "There is no unique persistence unit found by name " +
                        "[ {0} ] in the scope of this component.",
                        new Object[]{referringUnitName}));
            } else {
                result.passed(smh.getLocalString(
                        className + "passed",
                        "Found a persistence unit by name [ {0} ] in the scope of this component",
                        new Object[]{referringUnitName}));
            }
        }
        for (EntityManagerFactoryReferenceDescriptor emfRefDesc : getEntityManagerFactoryReferenceDescriptors(descriptor)) {
            String referringUnitName = emfRefDesc.getUnitName();
            PersistenceUnitDescriptor pu = bundleDescriptor.findReferencedPU(referringUnitName);
            if (pu == null) {
                result.failed(smh.getLocalString(
                        className + "failed",
                        "There is no unique persistence unit found by name " +
                        "[ {0} ] in the scope of this component.",
                        new Object[]{referringUnitName}));
            } else {
                result.passed(smh.getLocalString(
                        className + "passed",
                        "Found a persistence unit by name [ {0} ] in the scope of this component",
                        new Object[]{referringUnitName}));
            }
        }
        
        StringBuilder visiblePUNames = new StringBuilder();
        final Map<String, PersistenceUnitDescriptor> visiblePUs =
                bundleDescriptor.getVisiblePUs();
        int count = 0;
        for(String puName : visiblePUs.keySet()) {
            visiblePUNames.append(puName);
            if(visiblePUs.size() != ++count) { // end not yet reached
                visiblePUNames.append(", ");
            }
        }
        String message = smh.getLocalString(className + ".puList",
                "PUs that are visible to this component are: [ {0} ]",
                new Object[]{visiblePUNames});
        result.addErrorDetails(message);
        result.addGoodDetails(message);
        
        return result;
    }
    
    protected abstract BundleDescriptor getBundleDescriptor(
            Descriptor descriptor);
    
    protected abstract Collection<EntityManagerReferenceDescriptor>
            getEntityManagerReferenceDescriptors(Descriptor descriptor);
    
    protected abstract Collection<EntityManagerFactoryReferenceDescriptor>
            getEntityManagerFactoryReferenceDescriptors(Descriptor descriptor);
    
}

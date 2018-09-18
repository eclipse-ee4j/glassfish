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

package com.sun.enterprise.tools.verifier.tests.web;


import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;

import java.util.Enumeration;
import java.util.Vector;



/**
 *
 *  @author Jerome Dochez
 */
public class FilterInitParamValue extends WebTest implements WebCheck {

    /**
     * Param Value exists test.
     *
     * @param descriptor the Web deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneWarning = false, onePassed = false;

        Enumeration filterEnum = descriptor.getServletFilterDescriptors().elements();
        if (filterEnum.hasMoreElements()) {
            // get the filters in this .war
            while (filterEnum.hasMoreElements()) {
                ServletFilterDescriptor filter = (ServletFilterDescriptor) filterEnum.nextElement();
                Vector epVector = filter.getInitializationParameters();

                if (epVector.size() != 0) {
                    for ( int i = 0; i < epVector.size(); i++) {
                        EnvironmentProperty ep = (EnvironmentProperty)epVector.elementAt(i);
                        String epValue = ep.getValue();
                        if (epValue.length() != 0) {
                            onePassed=true;
                            addGoodDetails(result, compName);
                            result.addGoodDetails(smh.getLocalString
                                              ("com.sun.enterprise.tools.verifier.tests.web.FilterInitParamValue" + ".passed",
                                               "Param value exists for the filter [ {0} ].",
                                               new Object[] {filter.getName()}));
                        } else {
                            oneWarning = true;
                            addWarningDetails(result, compName);
                            result.addWarningDetails(smh.getLocalString
                                    ("com.sun.enterprise.tools.verifier.tests.web.FilterInitParamValue" + ".warning",
                                            "WARNING: Param value entry for the filter [ {0} ] should be of finite length.",
                                            new Object[] {filter.getName()}));
                        }
                    }
                } else {
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString
                            ("com.sun.enterprise.tools.verifier.tests.web.FilterInitParamValue" + ".notApplicable",
                                    "There are no initialization parameters for the filter [ {0} ] within the web archive [ {1} ]",
                                    new Object[] {filter.getName(), descriptor.getName()}));

                }
            }
            if (oneWarning) {
                result.setStatus(Result.WARNING);
            } else if (onePassed){
                result.setStatus(Result.PASSED);
            }
        } else {
            result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.web.FilterInitParamValue" + ".notApplicable1",
                            "There are no filters defined within the web archive [ {0} ]",
                            new Object[] {descriptor.getName()}));
        }
        return result;
    }
}

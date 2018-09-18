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

/** enterprise-beans
 *   ejb [1,n]
 *     mdb-resource-adapter ?
 *       resource-adapter-mid  [String]
 *       activation-config ?
 *         description ?  [String]
 *         activation-config-property +
 *           activation-config-property-name  [String]
 *           activation-config-property-value  [String]
 *
 * This is the name of the enterprise java bean.
 * @author
 */
public class ASEjbMDBResourceAdapter extends EjbTest implements EjbCheck {

    /**
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        boolean oneFailed = false;
	    Result result = getInitializedResult();
	    ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        String value=null;
        int count = 0;
        try{
            count = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter");
            if (count>0){
                value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter/resource-adapter-mid");
                if(value==null || value.length()==0){
                    oneFailed=true;
                    result.addErrorDetails(smh.getLocalString
                        ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString
                        (getClass().getName() + ".failed1",
                        "FAILED [AS-EJB mdb-resource-adapter] : resource-adapter-mid cannot be empty.",
                        new Object[] {descriptor.getName()}));
                }else{
                    result.addGoodDetails(smh.getLocalString
                        ("tests.componentNameConstructor",
                        "For [ {0} ]",
                        new Object[] {compName.toString()}));
                    result.passed(smh.getLocalString(
                                    getClass().getName() + ".passed1",
                        "PASSED [AS-EJB mdb-resource-adapter] : resource-adapter-mid is {1}",
                        new Object[] {descriptor.getName(),value}));
                }
                //activation-config
                count = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter/activation-config");
                if (count>0){
                    count = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter/activation-config/activation-config-property");
                    if (count>0){
                        for (int i=1;i<=count;i++){
                            value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter/activation-config/activation-config-property/activation-config-property-name");
                            if(value==null || value.length()==0){
                                oneFailed=true;
                                result.addErrorDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                                result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed2",
                                    "FAILED [AS-EJB mdb-resource-adapter] : activation-config-property-name cannot be empty.",
                                    new Object[] {descriptor.getName()}));
                            }else{
                                result.addGoodDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                                result.passed(smh.getLocalString(
                                                getClass().getName() + ".passed2",
                                    "PASSED [AS-EJB mdb-resource-adapter] : activation-config-property-name is {1}",
                                    new Object[] {descriptor.getName(),value}));
                            }

                            value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/mdb-resource-adapter/activation-config/activation-config-property/activation-config-property-value");
                            if(value==null || value.length()==0){
                                oneFailed=true;
                                result.addErrorDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                                result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed3",
                                    "FAILED [AS-EJB mdb-resource-adapter] : activation-config-property-value cannot be empty.",
                                    new Object[] {descriptor.getName()}));
                            }else{
                                result.addGoodDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                                result.passed(smh.getLocalString(
                                                getClass().getName() + ".passed3",
                                    "PASSED [AS-EJB mdb-resource-adapter] : activation-config-property-value is {1}",
                                    new Object[] {descriptor.getName(),value}));
                            }
                        }
                    }else{
                        oneFailed=true;
                        result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
                        result.failed(smh.getLocalString
                            (getClass().getName() + ".failed4",
                            "FAILED [AS-EJB mdb-resource-adapter] : activation-config-property is not defined",
                            new Object[] {descriptor.getName()}));
                    }
                }
            }else{
                    result.addNaDetails(smh.getLocalString
				        ("tests.componentNameConstructor",
				        "For [ {0} ]",
				        new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : mdb-resource-adapter is not defined."));
            }
        }catch(Exception ex){
            oneFailed = true;
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create descriptor object"));
        }
        if(oneFailed)
            result.setStatus(Result.FAILED);
        return result;
    }
}

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

import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Arrays;

/** enterprise-bean
 *    cmp-resource ?
 *        jndi-name [String]
 *        default-resource-principal ?
 *          name [String]
 *          password [String]
 *        //added new in sun-ejb-jar_2_1-0.dtd
 *        property *
 *          name [String]
 *          value [String]
 *        schema-generator-properties ?
 *          name [String]
 *          value [String]
 *
 * The cmp-resource contains the database to be used for storing the cmp beans
 * in the ejb-jar.xml
 * The jndi-name should not be null and should start with jdbc/
 * @author Irfan Ahmed
 */
public class ASEntBeanCmpResource extends EjbTest implements EjbCheck {
    boolean oneFailed=false;
    boolean oneWarning=false;

    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        try{
            EjbBundleDescriptorImpl ejbBundleDesc = descriptor.getEjbBundleDescriptor();
            ResourceReferenceDescriptor cmpResource = ejbBundleDesc.getCMPResourceReference();
            
            if(cmpResource!=null){
//                String jndiName = cmpResource.getJndiName();
                String jndiName = getXPathValue("sun-ejb-jar/enterprise-beans/cmp-resource/jndi-name");
                if(jndiName == null || jndiName.length()==0){
                    oneFailed=true;//4698046
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB cmp-resource] : jndi-name cannot be an empty string"));
                }else{
                    if(jndiName.startsWith("jdbc/")|| jndiName.startsWith("jdo/")) {
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB cmp-resource] : jndi-name is {0}",new Object[]{jndiName}));
                    }else{
                        oneWarning=true;//4698046
                        addWarningDetails(result, compName);
                        result.warning(smh.getLocalString(getClass().getName()+".warning",
                            "WARNING [AS-EJB cmp-resource] : The jndi-name  is {0}, the preferred jndi-name should start with  jdbc/ or jdo/"
                            , new Object[]{jndiName}));
                    }
                }

                ResourcePrincipal defPrincipal = cmpResource.getResourcePrincipal();
                if(defPrincipal!=null){
//                    String name = defPrincipal.getName();
                    String name = getXPathValue("sun-ejb-jar/enterprise-beans/cmp-resource/default-resource-principal/name");
                    if( name == null || name.length()==0){
                        oneFailed=true; //4698046
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failed2",
                            "FAILED [AS-EJB default-resource-principal] :  name cannot be an empty string"));
                    }else{
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passed1",
                            "PASSED [AS-EJB default-resource-principal] : name is {0}",new Object[]{name}));
                    }

//                    String password = defPrincipal.getPassword();
                    char[] password = getXPathValueasCharArray("sun-ejb-jar/enterprise-beans/cmp-resource/default-resource-principal/password");
                    if(password == null || password.length==0){
                        oneWarning=true;//4698046
                        addWarningDetails(result, compName);
                        result.warning(smh.getLocalString(getClass().getName()+".warning1",
                            "WARNING [AS-EJB default-resource-principal] : password is an empty string"));
                    }else{
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passed2",
                            "PASSED [AS-EJB default-resource-principal] : password is  {0}",new Object[]{new String(password)}));
                    }
                    Arrays.fill(password,' ');
                }else{
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB cmp-resource] : default-resource-principal Element not defined"));
                }

                Float specVer = getRuntimeSpecVersion();
                if ((Float.compare(specVer.floatValue(), (new Float("2.1")).floatValue()) >= 0)){
                    //property
                    result = testProperty("property", result, "sun-ejb-jar/enterprise-beans/cmp-resource/property", compName, descriptor);
                    //schema-generator-properties
                    result = testProperty("schema-generator-properties", result, "sun-ejb-jar/enterprise-beans/cmp-resource/schema-generator-properties/property", compName, descriptor);
                }

                if(oneFailed)//4698046
                    result.setStatus(Result.FAILED);
                else if(oneWarning)
                    result.setStatus(Result.WARNING);
            }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB enterprise-beans] : cmp-resource element is not defined"));
            }
            
        }catch(Exception ex){
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString(getClass().getName()+".notRun",
                "NOT RUN [AS-EJB cmp] Could not create descriptor Object."));
        }
        return result;
    }

    private Result testProperty(String testFor, Result result, String xpath, ComponentNameConstructor compName, EjbDescriptor descriptor){
        String name=null;
        String value=null;
        int count = getCountNodeSet(xpath);
        if (count>0){
            for(int i=1;i<=count;i++){
                name = getXPathValue(xpath+"/name");
                if(name==null || name.length()==0){
                    oneFailed=true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                        (getClass().getName() + ".failed3",
                        "FAILED [AS-EJB cmp-resource {1}] : name cannot be an empty string",
                        new Object[] {descriptor.getName(),testFor}));
                }else{
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(
                        getClass().getName() + ".passed3",
                        "PASSED [AS-EJB cmp-resource {2}] : name is {1}",
                        new Object[] {descriptor.getName(),name, testFor}));
                }
                value = getXPathValue(xpath+"/value");
                if(value==null || value.length()==0){
                    oneFailed=true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                        (getClass().getName() + ".failed4",
                        "FAILED [AS-EJB cmp-resource {1}] : value cannot be an empty string",
                        new Object[] {descriptor.getName(), testFor}));
                }else{
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(
                        getClass().getName() + ".passed4",
                        "PASSED [AS-EJB cmp-resource {2}] : value is {1}",
                        new Object[] {descriptor.getName(),value, testFor}));
                }
            }
        }
        return result;
    }
}

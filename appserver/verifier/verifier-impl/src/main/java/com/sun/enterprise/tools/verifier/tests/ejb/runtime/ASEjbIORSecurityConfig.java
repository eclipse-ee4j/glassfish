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

/** ejb [0,n]
 *    ior-security-config ?
 *        transport-config?
 *            integrity [String]
 *            confidentiality [String]
 *            establish-trust-in-client [String]
 *            establish-trust-in-target [String]
 *        as-context?
 *            auth-method [String]
 *            realm [String]
 *            required [String]
 *        sas-context?
 *            caller-propagation [String]
 *
 * The tag describes the security configuration for the IOR
 * @author Irfan Ahmed
 */
public class ASEjbIORSecurityConfig extends EjbTest implements EjbCheck { 
    boolean oneFailed = false;

    /** The function that performs the test.
     *
     * @param descriptor EjbDescriptor object representing the bean.
     */    
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        int count = 0;
        try{
//            Set ejbIORConfDescSet = descriptor.getIORConfigurationDescriptors();
            count = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config");
            if (count>0){
                for(int i=0;i<count;i++){
                    testTranConfig(i, descriptor, compName, result);
                    testAsContext(i, descriptor, compName, result);
                    testSasContext(i, descriptor, compName, result);
                }
            }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB ior-security-config] : ior-security-config Element not defined"));
            }

            if(oneFailed)
                result.setStatus(Result.FAILED);
        }catch(Exception ex){
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
        return result;

    }
    /** This function tests the <transport-config> tag for valid values
     *
     * @param
     * @param result Result - The Result object
     */    
    protected void testTranConfig(int i, EjbDescriptor descriptor, ComponentNameConstructor compName, Result result)
    {
        try{
            int counter = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config/transport-config");
            if (counter>0){
                //integrity
                String integrity = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/transport-config/integrity");
                if (integrity!=null){
                    testMsgs(integrity,result,"transport-config","integrity", compName);
                }

                //confidentiality
                String confdn = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/transport-config/confidentiality");
                if (confdn!=null){
                    testMsgs(confdn,result,"transport-config","confidentiality", compName);
                }

                //establish-trust-in-target
                String trustTarget = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/transport-config/establish-trust-in-target");
                if (trustTarget!=null){
                    testMsgs1(trustTarget,result,"transport-config","extablish-trust-in-target", compName);
                }

                //establish-trust-in-client
                String trustClient = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/transport-config/establish-trust-in-client");
                if (trustClient!=null){
                    testMsgs(trustClient,result,"transport-config","establish-trust-in-client", compName);
                }
            }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable1",
                    "NOT APPLICABLE [AS-EJB ior-security-config] : transport-config Element not defined"));
            }
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
    }
    /** The function tests the <as-context> tag for valid values
     *
     * @param
     * @param result Result object
     */    
    protected void testAsContext(int i, EjbDescriptor descriptor, ComponentNameConstructor compName, Result result)
    {
        try{
            int counter = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config/as-context");
            if (counter>0){
                //auth-method
                String value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/as-context/auth-method");
                if(value==null || value.length()==0){
                    oneFailed = true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failedAsContextAuthMethod",
                        "FAILED [AS-EJB as-context] : auth-method cannot be an empty string"));
                }else{
                    if(value.equals("USERNAME_PASSWORD")){
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passedAsContextAuthMethod",
                            "PASSED [AS-EJB as-context] : auth-method is {0}", new Object[] {value}));
                    }else{
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failedAsContextAuthMethod1",
                            "FAILED [AS-EJB as-context] : auth-method cannot be {0}. It can only be USERNAME_PASSWORD"
                            ,new Object[]{value}));
                    }
                }
                //realm
                value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/as-context/realm");
                if (value != null){
                    if(value.length()==0){
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRealm",
                            "FAILED [AS-EJB as-context] : realm cannot be an empty string"));

                    }else{
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passedAsContextRealm",
                            "PASSED [AS-EJB as-context] : realm is {0}", new Object[] {value}));
                    }
                } else {
                    oneFailed = true;
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRealm1",
                            "FAILED [AS-EJB as-context] : realm cannot be null"));
                }
                //required
                value = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/as-context/required");
                if(value==null || value.length()==0){
                    oneFailed = true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRequired1",
                        "FAILED [AS-EJB as-context] : required cannot be an empty string"));
                }else{
                    if(value.equals("true") || value.equals("false")){
                        addGoodDetails(result, compName);
                        result.passed(smh.getLocalString(getClass().getName()+".passedAsContextRequired",
                            "PASSED [AS-EJB as-context] : required is {0}", new Object[]{new Boolean(value)}));
                    }else{
                        oneFailed = true;
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString(getClass().getName()+".failedAsContextRequired2",
                            "FAILED [AS-EJB as-context] : required cannot be {0}. It can only be USERNAME_PASSWORD"
                            ,new Object[]{value}));
                    }
                }
            }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable2",
                    "NOT APPLICABLE [AS-EJB ior-security-config] :  Element not defined"));
            }

        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
        
    }
    /**
     * @param
     * @param result  */    
    protected void testSasContext(int i, EjbDescriptor descriptor, ComponentNameConstructor compName, Result result)
    {
        try{
            int counter = getCountNodeSet("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config/sas-context");
            if (counter>0){
                String caller = getXPathValue("sun-ejb-jar/enterprise-beans/ejb[ejb-name=\""+descriptor.getName()+"\"]/ior-security-config[\""+i+"\"]/sas-context/caller-propagation");
                if (caller!=null){
                    testMsgs1(caller,result,"sas-context","caller-propagation", compName);
                }
            }
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
    }
    
    private void testMsgs(String tCase, Result result, String parentElement, String testElement, ComponentNameConstructor compName)
    {
        if(tCase.length()==0)
        {
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString(getClass().getName()+".failedTestMsg",
                "FAILED [AS-EJB {1}] : {2} cannot be an empty String",
                new Object[]{tCase, parentElement, testElement}));
        }
        else
        {
            if(!tCase.equals("NONE") && !tCase.equals("SUPPORTED") 
                && !tCase.equals("REQUIRED"))
            {
                oneFailed = true;
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(getClass().getName()+".failedTestMsg1",
                    "FAILED [AS-EJB {1}] : {2} cannot be {0}. It can be either NONE, SUPPORTED or REQUIRED",
                    new Object[]{tCase, parentElement, testElement}));
            }
            else {
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passedTestMsg",
                    "PASSED [AS-EJB "+ parentElement+"] : " + testElement +" is {0}", new Object[]{tCase}));
            }
        }
    }
    
    /**
     * This method will check for values that should be either NONE or SUPPORTED
     */ 
    private void testMsgs1(String tCase, Result result, String parentElement, String testElement, ComponentNameConstructor compName)
    {
        if(tCase.length()==0)
        {
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString(getClass().getName()+".failedTestMsg",
                "FAILED [AS-EJB {1}] : {2} cannot be an empty String",
                new Object[]{tCase, parentElement, testElement}));
        }
        else
        {
            if(!tCase.equals("NONE") && !tCase.equals("SUPPORTED"))
            {
                oneFailed = true;
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(getClass().getName()+".failedTestMsg2",
                    "FAILED [AS-EJB {1}] : {2} cannot be {0}. It can be either NONE or SUPPORTED.",
                    new Object[]{tCase, parentElement, testElement}));
            }
            else {
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passedTestMsg",
                    "PASSED [AS-EJB "+ parentElement+"] : " + testElement +" is {0}", new Object[]{tCase}));
            }
        }
    }
}

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
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbCMPFinder;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/** ejb [0,n]
 *      cmp ?
 *          mapping-properties ? [String]
 *          is-one-one-cmp ? [String]
 *          one-one-finders ?
 *              finder [1,n]
 *                  method-name [String]
 *                  query-params ? [String]
 *                  query-filter ? [String]
 *                  query-variables ? [String]
 *                  query-ordering ? [String]
 *
 * The cmp element describes the runtime information for a CMP enitity bean.
 * mapping-properties - The vendor specific O/R mapping file
 * is-one-one-cmp - boolean field used to identify CMP 1.1 descriptors
 * one-one-finders - The finders for CMP 1.1
 *
 * @author Irfan Ahmed
 */
public class ASEjbCMP extends EjbTest implements EjbCheck {

    public boolean oneFailed = false;
    public boolean oneWarning = false;
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        try {
        if (descriptor instanceof IASEjbCMPEntityDescriptor){
            
            IASEjbCMPEntityDescriptor cmpBean = (IASEjbCMPEntityDescriptor)descriptor;

            String mappingProps = cmpBean.getMappingProperties();
            if(mappingProps == null){
                oneWarning = true;
                addWarningDetails(result, compName);
                result.warning(smh.getLocalString(getClass().getName()+".warning",
                    "WARNING [AS-EJB cmp] : mapping-properties Element is not defined"));
            }else{
                if(mappingProps.length()==0) {
                    oneFailed = true;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName()+".failed",
                        "FAILED [AS-EJB cmp] : mapping-properties field must contain a vaild non-empty value"));
                }
                else{               //4690436
//                        File f = Verifier.getArchiveFile(descriptor.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri());
                    JarFile jarFile = null;
                    ZipEntry deploymentEntry=null;
//                        try {
//                            jarFile = new JarFile(f);
                          if(jarFile!=null)
                              deploymentEntry = jarFile.getEntry(mappingProps);
//                        }catch(IOException e){}
//                        finally{
//                           try{  if(jarFile!=null) jarFile.close();} 
//                           catch(IOException e){}
//                        }

                    if(deploymentEntry !=null){
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB cmp] : mapping-properties file is {0}",
                        new Object[]{mappingProps}));
                    }else{
                        addErrorDetails(result, compName);
                        //invalid entry
                        result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB cmp] : mapping-properties field must contain a vaild non-empty value"));
                    }
                }
            }
            
            try{
                boolean oneoneCmp = cmpBean.getCMPVersion()== EjbCMPEntityDescriptor.CMP_1_1;
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString(getClass().getName()+".passed1",
                    "PASSED [AS-EJB cmp] : is-one-one-cmp is {0}",
                    new Object[]{new Boolean(oneoneCmp)}));
            }catch(Exception ex){
                oneWarning = true;
                addWarningDetails(result, compName);
                result.warning(smh.getLocalString(getClass().getName()+".warning1",
                    "WARNING [AS-EJB cmp] : is-one-one-cmp Element is not defined"));
            }
            
            try{
//EXCEPTION is thrown here as getOneOneFinders() internally uses queryPArser which is null. Exception as:
//Apr 4, 2003 11:18:22 AM com.sun.enterprise.deployment.IASEjbCMPEntityDescriptor getOneOneFinder
                Map finders = cmpBean.getOneOneFinders();
                if(finders!=null){
                    testFinders(finders,result);
                }else{
                    oneWarning = true;
                    addWarningDetails(result, compName);
                    result.warning(smh.getLocalString(getClass().getName()+".warning2",
                        "WARNING [AS-EJB cmp] : one-one-finders Element is not defined"));
                }
            }catch(Exception ex){
                oneFailed = true;
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(getClass().getName()+".failed1",
                    "FAILED [AS-EJB cmp] : getOneOneFinders Failed.",
                    new Object[]{cmpBean}));
            }
            
            if(oneFailed){
                result.setStatus(Result.FAILED);}
            else{ if(oneWarning)
                result.setStatus(Result.WARNING);}
            
        }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB cmp] : {0} is not a CMP Entity Bean.",
                    new Object[] {descriptor.getName()}));
        }
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString(getClass().getName()+".notRun",
                "NOT RUN [AS-EJB cmp] Could not create descriptor Object."));
        }
        return result;
    }
    

    public void testFinders(Map finders, Result result) {
        try{
        Set keySet = finders.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()){
            Object obj = it.next();
            IASEjbCMPFinder finder = (IASEjbCMPFinder)finders.get(obj);
            
            //method-name
            String methodName = finder.getMethodName();
            if(methodName.length()==0){
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failed2",
                    "FAILED [AS-EJB finder] : method-name cannot be an empty string."));
            }else{
                result.passed(smh.getLocalString(getClass().getName()+".passed2",
                    "PASSED [AS-EJB finder] : method-name is {0}",
                    new Object[]{methodName}));
            }

            //query-params
            String value = finder.getQueryParameterDeclaration();
            testQuery(value,result,"finder","query-params");

            //query-filter
            value = finder.getQueryFilter();
            testQuery(value,result,"finder","query-filter");

            //query-variables
            value = finder.getQueryVariables();
            testQuery(value,result,"finder","query-variables");

            //query-ordering
            value = finder.getQueryOrdering();
            testQuery(value,result,"finder","query-ordering");
        }
        }catch(Exception ex){
            oneFailed = true;
            result.failed(smh.getLocalString(getClass().getName()+".notRun",
                "NOT RUN [AS-EJB cmp] Could not create descriptor Object."));
        }
    }

    public void testQuery(String value, Result result,String parent, String child){
        if(value == null){
            oneWarning = true;
            result.warning(smh.getLocalString(getClass().getName()+".warning3",
                "WARNING [AS-EJB {0}] : {1} Element is not defined",
                new Object[]{parent,child}));
        }else{
            if(value.length()==0){
                oneFailed = true;
                result.failed(smh.getLocalString(getClass().getName()+".failed3",
                    "FAILED [AS-EJB {0}] : {1} cannot be an empty string",
                    new Object[]{parent,child}));
            }else{
                result.passed(smh.getLocalString(getClass().getName()+".passed3",
                    "PASSED [AS-EJB {0}] : {1} is/are {2}",
                    new Object[]{parent, child, value}));
            }
        }
    }
}

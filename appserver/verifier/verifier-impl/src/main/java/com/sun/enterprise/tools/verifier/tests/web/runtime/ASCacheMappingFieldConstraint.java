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

package com.sun.enterprise.tools.verifier.tests.web.runtime;

import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.tests.web.*;
import org.glassfish.web.deployment.runtime.Cache;
import org.glassfish.web.deployment.runtime.CacheMapping;
import org.glassfish.web.deployment.runtime.ConstraintField;
import org.glassfish.web.deployment.runtime.SunWebAppImpl;

//<addition author="irfan@sun.com" [bug/rfe]-id="4711198" >
/* Changed the result messages to reflect consistency between the result messages generated 
 * for the EJB test cases for SunONE specific deployment descriptors*/
//</addition>

public class ASCacheMappingFieldConstraint extends ASCache implements WebCheck {



    public Result check(WebBundleDescriptor descriptor) {


	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        boolean oneFailed = false;
        boolean notApp = false;
        boolean doneAtleastOnce=false;
              
        try{
        Cache cache = ((SunWebAppImpl)descriptor.getSunDescriptor()).getCache();
        CacheMapping[] cacheMapp=null;
        String servletName;
        String urlPattern;
        ConstraintField[] fieldConstraints=null;

        String mappingFor=null;

        if (cache != null ){
          cacheMapp=cache.getCacheMapping();
        }


         if (cache != null && cacheMapp !=null && cacheMapp.length !=0 ) {
            for(int rep=0;rep < cacheMapp.length;rep++){

		servletName = cacheMapp[rep].getServletName();
                urlPattern = cacheMapp[rep].getURLPattern();

                if(servletName !=null)
                    mappingFor=servletName;
                else
                    mappingFor=urlPattern;

                fieldConstraints = cacheMapp[rep].getConstraintField();
                if(fieldConstraints !=null && fieldConstraints.length != 0)
                {
                    for(int rep1=0;rep1 < fieldConstraints.length;rep1++){
                        if(fieldConstraints[rep1] !=null){
                            doneAtleastOnce=true;
                            if(checkFieldConstraint(fieldConstraints[rep1],result,mappingFor,descriptor,compName)){
                                //nothing more required
                            }
                            else{
                                oneFailed = true;
                                addErrorDetails(result, compName);
                                result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "FAILED [AS-WEB cache-mapping] List of field-constraint in cache-mapping for [ {0} ] is not proper,  within the web archive/(gf/)sun-web.xml of [ {1} ].",
                                    new Object[] {mappingFor,descriptor.getName()}));

                            }
                        } 
                    }//end of for(int rep1=0;rep1 < fieldConstraints.length;rep1++)
                } 
                
            }//end of for(int rep=0;rep < cacheMapp.length;rep++)

          }else {
              notApp=true;
          }

        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if(notApp || !doneAtleastOnce) {
            result.setStatus(Result.NOT_APPLICABLE);
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                                     (getClass().getName() + ".notApplicable",
                                      "NOT APPLICABLE [AS-WEB cache-mapping ] constraint-field not defined in [ {0} ].",
                                      new Object[] {descriptor.getName()}));



        }else {
            result.setStatus(Result.PASSED);
        } 
        }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                    "FAILED [AS-WEB cache-mapping] could not create the cache object"));
        }
	return result;
    }

    

    boolean checkFieldConstraint(ConstraintField fieldCons, Result result,String mappingFor,
                        WebBundleDescriptor descriptor,ComponentNameConstructor compName) {

          boolean valid=true;
          String fieldName;
          String[] values;
          if (fieldCons != null) {
            // fieldName= fieldCons.getAttributeValue("name");
             fieldName= fieldCons.getAttributeValue(ConstraintField.NAME);
             values=fieldCons.getValue();
             if(fieldName!=null && ! fieldName.equals("")){
                addGoodDetails(result, compName);
                result.passed(smh.getLocalString
					  (getClass().getName() + ".passed1",
					   "PASSED [AS-WEB cache-mapping] Proper field-constraint/fieldName  [ {0} ]  defined for [ {1} ].",
					   new Object[] {fieldName,mappingFor}));

             }else{
                valid=false;
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed1",
                                      "FAILED [AS-WEB cache-mapping] field-constraint/fieldName [ {0} ] defined for [ {1} ], attribute can not be empty.",
                                      new Object[] {fieldName,mappingFor}));

             }

             for(int rep=0;values !=null && rep < values.length;rep++){
                if(values[rep]!=null && ! values[rep].equals("")) {
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString
					  (getClass().getName() + ".passed2",
					   "PASSED [AS-WEB cache-mapping]Proper field-constraint/value   [ {0} ]  defined for [ {1} ], within the web archive/(gf/)sun-web.xml of [ {2} ].",
					   new Object[] {values[rep],mappingFor,descriptor.getName()}));

                }else {
                    valid=false;
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                      (getClass().getName() + ".failed2",
                                      "FAILED [AS-WEB cache-mapping] field-constraint/value [ {0} ] defined for [ {1} ], can not be empty, within the web archive/(gf/)sun-web.xml of [ {2} ].",
                                      new Object[] {values[rep],mappingFor,descriptor.getName()}));

                }

             }
          }

          return valid;
    }
     

}


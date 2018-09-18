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


import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.web.deployment.runtime.*;

/*  servlet
 *     servlet-name
 *     principal-name ?
 *     webservice-endpoint *
 */   

public class ASServlet extends WebTest implements WebCheck {

    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        //String[] servlets=null;//######
        String servletName;
        String prinName;
	boolean oneFailed = false;

        try{
            Float runtimeSpecVersion = getRuntimeSpecVersion();
            Servlet[] servlets = ((SunWebAppImpl)descriptor.getSunDescriptor()).getServlet();
            if (servlets !=null && servlets.length > 0){
	    for (int rep=0; rep<servlets.length; rep++ ){
                servletName=servlets[rep].getServletName();//######
                // <addition> srini@sun.com Bug : 4699658
                //prinName=servlets[rep].getPrincipalName();//######
               // prinName=servlets[rep].getPrincipalName().trim();//######
                // </addition> Bug : 4699658
                
                      if(validServletName(servletName,descriptor)){

                      addGoodDetails(result, compName);
                      result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "PASSED [AS-WEB servlet] servlet-name [ {0} ] properly defined in the war file.",
					   new Object[] {servletName}));

                      }else{

                	addErrorDetails(result, compName);
		        result.failed(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "FAILED [AS-WEB servlet] servlet-name [ {0} ] is not a valid, either empty or not defined in web.xml.",
					    new Object[] {servletName}));
		        oneFailed = true;

                      }
                      prinName=servlets[rep].getPrincipalName();
                      if(prinName != null && ! "".equals(prinName)){
                          addGoodDetails(result, compName);
                          result.passed(smh.getLocalString
			      (getClass().getName() + ".passed1",
			       "PASSED [AS-WEB servlet] principal-name [ {0} ] properly defined in the war file.",
			       new Object[] {prinName}));
                      }else{
                          if (runtimeSpecVersion.compareTo(new Float("2.4")) <0 ){
                              result.failed(smh.getLocalString
                                  (getClass().getName() + ".failed1",
                                  "FAILED [AS-WEB servlet ] principal-name [ {0} ] cannot be an empty string.",
                                  new Object[] {prinName}));
                              oneFailed = true;
                          }else{
                              addNaDetails(result, compName);
                              result.notApplicable(smh.getLocalString
                                  (getClass().getName() + ".notApplicable1",
                                  "NOT APPLICABLE [AS-WEB servlet] principal-name not defined",
                                  new Object[] {descriptor.getName()}));
                          }

                      }

	    }
    	}else{
            addNaDetails(result, compName);
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "NOT APPLICABLE [AS-WEB sun-web-app] servlet element(s) not defined in the web archive [ {0} ].",
				  new Object[] {descriptor.getName()}));
	    return result;
	}

	if (oneFailed)
	    {
		result.setStatus(Result.FAILED);
	    } else {
        	addGoodDetails(result, compName);
		result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed2",
		      "PASSED [AS-WEB sun-web-app] servlet element(s) are valid within the web archive [ {0} ] .",
                            new Object[] {descriptor.getName()} ));
	    }
       }catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                (getClass().getName() + ".failed2",
                    "FAILED [AS-WEB sun-web-app] could not create the servlet object"));
       }
	return result;
    }

    boolean validServletName(String servletName, WebBundleDescriptor descriptor){
          boolean valid=false;
          if (servletName != null && servletName.length() != 0) {
              Set servlets = descriptor.getServletDescriptors();
                    Iterator itr = servlets.iterator();
                    // test the servlets in this .war
                    while (itr.hasNext()) {
                        //ServletDescriptor servlet = (ServletDescriptor) itr.next();
                        WebComponentDescriptor servlet = (WebComponentDescriptor) itr.next();
                        String thisServletName = servlet.getCanonicalName();
			if (servletName.equals(thisServletName)) {
                            valid = true;
                            break;
                        }
                    }

          }
          return valid;
    }
}

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

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * Servlet Interface test.
 * Servlets must implement the javax.servlet.Servlet interface 
 * either directly or indirectly through GenericServlet or HttpServlet
 */
public class ServletInterface extends WebTest implements WebCheck { 

    final String servletClassPath = "WEB-INF/classes";
      
    /**
     * Servlet Interface test.
     * Servlets must implement the javax.servlet.Servlet interface 
     * either directly or indirectly through GenericServlet or HttpServlet
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (!descriptor.getServletDescriptors().isEmpty()) {
	    boolean oneFailed = false;
            boolean notPassOrFail = true;       
	    // get the servlets in this .war
	    Set servlets = descriptor.getServletDescriptors();
	    Iterator itr = servlets.iterator();
                     
	    result = loadWarFile(descriptor);

	    // test the servlets in this .war
            
            while (itr.hasNext()) {
		WebComponentDescriptor servlet = (WebComponentDescriptor)itr.next();
		String servletClassName = servlet.getWebComponentImplementation();
		Class c = loadClass(result, servletClassName);

                // if the class could not be loaded we dont want to fail
                // , it will be caught by the ServletClass test anyway
                if (c == null) {
                   continue;
                }
                if (isJAXRPCEndpoint(servlet)) {
	            result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	            result.addGoodDetails(smh.getLocalString
				 (getClass().getName() + ".notApplicable1",
				  "Not Applicable since, Servlet [ {0} ] is a JAXRPC Endpoint.",
				  new Object[] {servletClassName}));
                    notPassOrFail = false;
                }
		else if (isImplementorOf(c, "javax.servlet.Servlet")) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "Servlet class [ {0} ] directly or indirectly implements javax.servlet.Servlet",
					   new Object[] {servletClassName}));	    
                    notPassOrFail = false;
		} else {
		    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: Servlet class [ {0} ] does not directly or indirectly implement javax.servlet.Servlet",
					    new Object[] {servletClassName}));
                    notPassOrFail = false;
		}                       
	    }
            // this means classloader returned null for all servlets
            if (notPassOrFail) {
               result.addWarningDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]", new Object[] {compName.toString()}));
               result.warning(smh.getLocalString
                              (getClass().getName() + ".warning",
                               "Some servlet classes could not be loaded."));
            }
	    else if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no servlet components within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }

 private boolean isJAXRPCEndpoint(WebComponentDescriptor servlet) {
  
     String servletClassName = servlet.getWebComponentImplementation();

     if (servletClassName.equals(smh.getLocalString("JAXRPCServlet","com.sun.xml.rpc.server.http.JAXRPCServlet"))) {
        // This is a standard JAXRPC servlet
        return true;
     }

     WebBundleDescriptor descriptor = servlet.getWebBundleDescriptor();
     if (descriptor.hasWebServices()) {
        WebServicesDescriptor wsdesc = descriptor.getWebServices();
        if (wsdesc.hasEndpointsImplementedBy(servlet)) {
           return true;
        }
     }
    return false;
 }

}

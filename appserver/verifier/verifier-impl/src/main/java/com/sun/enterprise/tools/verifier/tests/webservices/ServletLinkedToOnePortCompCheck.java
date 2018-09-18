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

package com.sun.enterprise.tools.verifier.tests.webservices;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import java.util.*;
import com.sun.enterprise.tools.verifier.tests.*;
import java.lang.reflect.*;

/* 
 *   @class.setup_props: ; 
 */ 

/*  
 *   @testName: check  
 *   @assertion_ids:  JSR109_WS_43; 
 *   @test_Strategy: 
 *   @class.testArgs: Additional arguments (if any) to be passed when execing the client  
 *   @testDescription: A servlet must only be linked to by a single port-component. 
 */

public class ServletLinkedToOnePortCompCheck  extends WSTest implements WSCheck {

    /**
     * @param descriptor the WebServices  descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check (WebServiceEndpoint descriptor) {

	Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor.implementedByEjbComponent()) {
           result.addNaDetails(smh.getLocalString
                     ("tests.componentNameConstructor", "For [ {0} ]",
                      new Object[] {compName.toString()}));
           result.notApplicable(smh.getLocalString
                 (getClass().getName() + ".notapp",
                 "This is an EJB Service Endpoint"));
           return result;
        }

    
        if (isLinkedToSinglePortComp(getAllEndPointsInApp(descriptor),descriptor.getLinkName())) {
           // result.pass
           result.addGoodDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
           result.passed(smh.getLocalString
                   (getClass().getName() + ".passed",
           "The Servlet associated with this end-point is linked to by a single port-component."));

        }
        else {
          // result.fail
          result.addErrorDetails(smh.getLocalString
                                  ("tests.componentNameConstructor",
                                   "For [ {0} ]",
                                   new Object[] {compName.toString()}));
          result.failed(smh.getLocalString
                (getClass().getName() + ".failed",
                "The Servlet associated with this end-point is linked to by multiple port-components."));

        }

        return result;
    }

    Collection getAllEndPointsInApp(WebServiceEndpoint desc) {
       Collection allWebServices = desc.getWebService().getWebServicesDescriptor().getWebServices();
       Collection ret = new Vector();
       for (Iterator it = allWebServices.iterator(); it.hasNext();) {
           ret.addAll(((WebService)it.next()).getEndpoints());
       }

     return ret;
    }

    // the compLink here is either an ejb-link or a servlet-link
    boolean isLinkedToSinglePortComp(Collection endPoints, String compLink) {
       boolean single = true;
       boolean linkAlreadySeen = false;
       for (Iterator it = endPoints.iterator(); it.hasNext();) {
           String myCompLink = ((WebServiceEndpoint)it.next()).getLinkName();

           if (myCompLink.equals(compLink)) {
              if (!linkAlreadySeen) {
                 linkAlreadySeen = true;
              }
              else {
                 single = false;
                 break;
              }
           }
       }
     return single;
    }
 }


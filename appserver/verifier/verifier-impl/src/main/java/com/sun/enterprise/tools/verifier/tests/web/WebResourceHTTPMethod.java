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

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.web.WebResourceCollection;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.web.deployment.descriptor.SecurityConstraintImpl;

import java.util.Enumeration;


/**
 * The http-method element contains the name of web resource collection's HTTP
 * method
 */
public class WebResourceHTTPMethod extends WebTest implements WebCheck
{


    /**
     * The http-method element contains the name of web resource collection's HTTP
     * method
     *
     * @param descriptor the Web deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor)
    {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor.getSecurityConstraints().hasMoreElements())
        {
            boolean oneFailed = false;
            boolean foundIt = false;
            int na = 0;
            int noSc = 0;
            int naWRC = 0;
            int noWRC = 0;
            // get the http method's in this .war
            for (Enumeration e = descriptor.getSecurityConstraints(); e.hasMoreElements();)
            {
                foundIt = false;
                noSc++;
                SecurityConstraintImpl securityConstraintImpl = (SecurityConstraintImpl) e.nextElement();
                if (!securityConstraintImpl.getWebResourceCollections().isEmpty())
                {
                    for (WebResourceCollection webResourceCollection : securityConstraintImpl.getWebResourceCollections())
                    {
                        noWRC++;
                        if (!webResourceCollection.getHttpMethods().isEmpty())
                        {
                            for (String webRCHTTPMethod : webResourceCollection.getHttpMethods())
                            {
                                // valid methods are the following
                                if ((webRCHTTPMethod.equals("OPTIONS")) ||
                                        (webRCHTTPMethod.equals("GET")) ||
                                        (webRCHTTPMethod.equals("HEAD")) ||
                                        (webRCHTTPMethod.equals("POST")) ||
                                        (webRCHTTPMethod.equals("PUT")) ||
                                        (webRCHTTPMethod.equals("DELETE")) ||
                                        (webRCHTTPMethod.equals("TRACE")) ||
                                        (webRCHTTPMethod.equals("CONNECT")))
                                {
                                    foundIt = true;
                                }
                                else
                                {
                                    foundIt = false;
                                }

                                if (foundIt)
                                {
                                    result.addGoodDetails(smh.getLocalString
                                            ("tests.componentNameConstructor",
                                                    "For [ {0} ]",
                                                    new Object[]{compName.toString()}));
                                    result.addGoodDetails(smh.getLocalString
                                            (getClass().getName() + ".passed",
                                                    "http-method [ {0} ] is valid HTTP method name within web resource collection [ {1} ] in web application [ {2} ]",
                                                    new Object[]{webRCHTTPMethod, webResourceCollection.getName(), descriptor.getName()}));
                                }
                                else
                                {
                                    if (!oneFailed)
                                    {
                                        oneFailed = true;
                                    }
                                    result.addErrorDetails(smh.getLocalString
                                            ("tests.componentNameConstructor",
                                                    "For [ {0} ]",
                                                    new Object[]{compName.toString()}));
                                    result.addErrorDetails(smh.getLocalString
                                            (getClass().getName() + ".failed",
                                                    "Error: http-method [ {0} ] is not valid HTTP method name within web resource collection [ {1} ] in web application [ {2} ]",
                                                    new Object[]{webRCHTTPMethod, webResourceCollection.getName(), descriptor.getName()}));
                                }
                            }
                        }
                        else
                        {
                            result.addNaDetails(smh.getLocalString
                                    ("tests.componentNameConstructor",
                                            "For [ {0} ]",
                                            new Object[]{compName.toString()}));
                            result.notApplicable(smh.getLocalString
                                    (getClass().getName() + ".notApplicable1",
                                            "There are no web http-methods in the web resource collection [ {0} ] within [ {1} ]",
                                            new Object[]{webResourceCollection.getName(), descriptor.getName()}));
                            naWRC++;
                        }
                    }
                }
                else
                {
                    result.addNaDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[]{compName.toString()}));
                    result.notApplicable(smh.getLocalString
                            (getClass().getName() + ".notApplicable2",
                                    "There are no web web resource collections in the web security constraint within [ {0} ]",
                                    new Object[]{descriptor.getName()}));
                    na++;
                }
            }
            if (oneFailed)
            {
                result.setStatus(Result.FAILED);
            }
            else if ((na == noSc) || (naWRC == noWRC))
            {
                result.setStatus(Result.NOT_APPLICABLE);
            }
            else
            {
                result.setStatus(Result.PASSED);
            }
        }
        else
        {
            result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[]{compName.toString()}));
            result.notApplicable(smh.getLocalString
                    (getClass().getName() + ".notApplicable",
                            "There are no http-method elements within the web archive [ {0} ]",
                            new Object[]{descriptor.getName()}));
        }

        return result;
    }
}

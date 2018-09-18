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
 * The web-resource-name element contains the name of this web resource
 * collection
 */
public class WebResourceName extends WebTest implements WebCheck
{


    /**
     * The web-resource-name element contains the name of this web resource
     * collection
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
            int na = 0;
            int noSc = 0;
            boolean foundIt = false;
            // get the security constraint's in this .war
            for (Enumeration e = descriptor.getSecurityConstraints(); e.hasMoreElements();)
            {
                foundIt = false;
                noSc++;
                SecurityConstraintImpl securityConstraintImpl = (SecurityConstraintImpl) e.nextElement();
                if (!securityConstraintImpl.getWebResourceCollections().isEmpty())
                {
                    for (WebResourceCollection webResCollection: securityConstraintImpl.getWebResourceCollections())
                    {
                        String webRCName = webResCollection.getName();
                        // cannot be blank
                        if (webRCName.length() > 0)
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
                                            "web-resource-name [ {0} ] contains the name of this web resource collection within web application [ {1} ]",
                                            new Object[]{webRCName, descriptor.getName()}));
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
                                            "Error: web-resource-name [ {0} ] does not contain the name of this web resource collection within web application [ {1} ]",
                                            new Object[]{webRCName, descriptor.getName()}));
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
            else if (na == noSc)
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
                            "There are no web-resource-name elements within the web archive [ {0} ]",
                            new Object[]{descriptor.getName()}));
        }

        return result;
    }
}

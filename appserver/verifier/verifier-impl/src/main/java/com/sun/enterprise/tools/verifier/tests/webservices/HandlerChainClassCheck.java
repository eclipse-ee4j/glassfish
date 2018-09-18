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

import java.util.List;
import java.util.Iterator;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.WebServiceHandler;
import com.sun.enterprise.deployment.WebServiceHandlerChain;

/**
 * @author Sudipto Ghosh
 */
public class HandlerChainClassCheck extends WSTest implements WSCheck {

    public Result check (WebServiceEndpoint descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        //Handler chains are applicable only in the context of JAX-WS 2.0. So
        // version check for this test is not required.
        List handlerChain = descriptor.getHandlerChain();
        for (Iterator it = handlerChain.iterator(); it.hasNext();) {
            List handlers = ((WebServiceHandlerChain)it.next()).getHandlers();
            for(Iterator itr = handlers.iterator(); itr.hasNext();) {
                String hClass =  ((WebServiceHandler)itr.next()).getHandlerClass();
                try {
                    Class cl = Class.forName(hClass, false, getVerifierContext().getClassLoader());
                    if (!((javax.xml.ws.handler.Handler.class).isAssignableFrom(cl))) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString (getClass().getName() + ".failed",
                                "Handler Class [{0}] does not implement " +
                                "javax.xml.ws.handler.Handler Interface",
                                new Object[] {hClass}));
                    }
                } catch (ClassNotFoundException e) {
                    // result.fail, handler class not found
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString (
                            "com.sun.enterprise.tools.verifier.tests.webservices.clfailed",
                            "The [{0}] Class [{1}] could not be Loaded",
                            new Object[] {"Handler Class", hClass}));
                }
            }
        }
        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString (getClass().getName() + ".passed1",
                    "Handler chains, if any, are defined properly"));
        }
        return result;
    }
}

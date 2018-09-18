/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package endpoint;

import java.util.Map;

import javax.jws.WebService;
import javax.ejb.Stateless;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@WebService(endpointInterface="endpoint.Hello")
@Stateless
public class HelloEJB implements Hello {

    @Resource WebServiceContext wsc;

    public String sayHello(String who) {
        Map<String, Object> msgCtxt = wsc.getMessageContext();
        return msgCtxt.toString();
    }

    @AroundInvoke
    private Object interceptBusinessMethod(InvocationContext invCtx) {
	try {
            System.out.println("ContextData" + invCtx.getContextData());
            //This is just to get the invocation trace
            //remove once bug is fixed
            Exception e = new Exception();
            e.printStackTrace();
            if (invCtx.getContextData() instanceof javax.xml.ws.handler.MessageContext){
                System.out.println("ContextDataMap is an instance of javax.xml.ws.handler.MessageContext ");

                return invCtx.proceed();
           } else {
                  return null;
           }
	} catch(Throwable t) { t.printStackTrace();}
        return null;
    }
}

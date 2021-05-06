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

import jakarta.jws.WebService;
import jakarta.annotation.Resource;
import jakarta.xml.ws.WebServiceContext;
import jakarta.servlet.ServletContext;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;


@WebService
public class Hello {

    @Resource WebServiceContext wsc;

    public String sayHello(String param) {
        System.out.println("wsctxt-servlet wsc = " + wsc);
        if(wsc != null) {
                 ServletContext sc =
(ServletContext)wsc.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
                String a = null;
                if (sc!= null ) {
                 a = sc.getServletContextName();
                }
                return "Hello " + param +a;
        }
        return "WebService Context injection failed";
    }
}

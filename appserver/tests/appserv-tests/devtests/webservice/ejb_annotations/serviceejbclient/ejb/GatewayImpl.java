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

package ejb;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import jakarta.ejb.Stateless;

import jakarta.xml.ws.WebServiceRef;
import javax.naming.Context;
import javax.naming.InitialContext;


import endpoint.HelloImplService;
import endpoint.HelloImpl;

@WebService
@Stateless
@WebServiceRef(name="service/helloservice", type=HelloImplService.class)
public class GatewayImpl {

    @WebServiceRef
    HelloImplService serviceField;

    HelloImplService serviceMethod=null;

    // method injection has to be private to avoid being part of the
    // web service interface.
    @WebServiceRef()
    private void setService(HelloImplService service) {
        serviceMethod = service;
    }

    @WebMethod
    public String invokeMethod(String who) {
        HelloImpl port = serviceMethod.getPort(HelloImpl.class);
        return "METHOD " + port.sayHello(who);
    }

    @WebMethod
    public String invokeField(String who) {
        HelloImpl port = serviceField.getPort(HelloImpl.class);
        return "FIELD " + port.sayHello(who);
    }

    @WebMethod
    public String invokeDependency(String who) {
        String result = null;
        try {
                Context ic = new InitialContext();
                HelloImplService service = (HelloImplService) ic.lookup("java:comp/env/service/helloservice");
                HelloImpl port = service.getPort(HelloImpl.class);
                result = "JNDI " + port.sayHello(who);
                System.out.println(result);
        } catch(Throwable t) {
                t.printStackTrace();
                return "FAILED";
        }
        return result;
    }

}

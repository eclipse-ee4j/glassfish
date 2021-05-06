/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * %W% %E%
 */

package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Service Implementation Class - as outlined in JAX-RPC Specification

import jakarta.jws.WebService;

@jakarta.jws.WebService(
    serviceName="HttpTestService",
    endpointInterface="service.Hello",
    portName="HelloPort",
    targetNamespace="http://httptestservice.org/wsdl",
    wsdlLocation="WEB-INF/wsdl/HttpTestService.wsdl"
)
public class HelloImpl implements Hello {

    public HelloResponse hello(HelloRequest req) {
        System.out.println("Hello, " + req.getString() + "!");
        HelloResponse resp = new HelloResponse();
        resp.setString("Hello, " + req.getString() + "!");
        return resp;
    }

    public void helloOneWay(HelloOneWay req) {
        System.out.println("Hello OneWay, " + req.getString() + "!");
    }
}

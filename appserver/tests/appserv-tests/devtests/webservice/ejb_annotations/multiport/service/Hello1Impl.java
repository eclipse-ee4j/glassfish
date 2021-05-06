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

@jakarta.ejb.Stateless
@jakarta.jws.WebService(
    serviceName="HttpTestService",
    endpointInterface="service.Hello1",
    portName="Hello1Port",
    targetNamespace="http://httptestservice.org/wsdl",
    wsdlLocation="META-INF/wsdl/HttpTestService.wsdl"
)
public class Hello1Impl implements Hello1 {

    public Hello1Response hello1(Hello1Request req) {
        System.out.println("Hello1, " + req.getString() + "!");
        Hello1Response resp = new Hello1Response();
        resp.setString("Hello1, " + req.getString() + "!");
        return resp;
    }
}

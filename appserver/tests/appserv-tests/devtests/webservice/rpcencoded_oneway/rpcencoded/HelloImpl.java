/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package rpcencoded;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.xml.rpc.server.ServiceLifecycle;


// Service Implementation Class - as outlined in JAX-RPC Specification

public class HelloImpl implements jakarta.servlet.SingleThreadModel, ServiceLifecycle {

    private boolean gotInit = false;

    public void init(Object o) {
        System.out.println("Got ServiceLifecycle::init call " + o);
        gotInit = true;
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public String hello(String s) throws RemoteException {
        return "Hello, " + s + "!";
    }

    public void helloOneWay(String s) throws RemoteException {
        System.out.println("Hello one way, " + s + "!");
    }
}

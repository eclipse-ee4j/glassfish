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

package com.sun.s1asdev.security.wss.permethod.servlet;

import jakarta.servlet.SingleThreadModel;
import javax.xml.rpc.server.ServiceLifecycle;

public class HelloServlet implements
                        SingleThreadModel, ServiceLifecycle {

    public HelloServlet() {
        System.out.println("HelloServlet() instantiated");
    }

    public void init(Object context) {
        System.out.println("Got ServiceLifecycle::init call " + context);
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public String sayHello(String message) {
        System.out.println("sayHello invoked from servlet endpoint");
        return "reply from " + message;
    }

    public int sendSecret(String message) {
        System.out.println("sendSecret invoked from servlet endpoint");
        return message.hashCode();
    }

    public String getSecret(double key) {
        System.out.println("getSecret invoked from servlet endpoint");
        return "Secret-" + key;
    }
}

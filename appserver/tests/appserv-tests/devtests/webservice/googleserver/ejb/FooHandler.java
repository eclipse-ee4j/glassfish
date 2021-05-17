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

package googleserver;

import java.util.Map;

import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.namespace.QName;

import javax.naming.InitialContext;

public class FooHandler extends GenericHandler {

    public void destroy() {
        System.out.println("In FooHandler::destroy()");
    }

    public QName[] getHeaders() {
        return new QName[0];
    }

    public boolean handleFault(MessageContext context) {
        System.out.println("In FooHandler::handleFault()");
        return true;
    }

    public boolean handleRequest(MessageContext context) {
        System.out.println("In FooHandler::handleRequest()");
        return true;
    }

    public boolean handleResponse(MessageContext context) {
        System.out.println("In FooHandler::handleResponse()");
        return true;
    }

    public void init(HandlerInfo config) {
        System.out.println("In FooHandler::init()");
        try {
            InitialContext ic = new InitialContext();
            String envEntry = (String) ic.lookup("java:comp/env/entry1");
            System.out.println("env-entry = " + envEntry);
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Handler init params = " +
                           config.getHandlerConfig());
    }

}

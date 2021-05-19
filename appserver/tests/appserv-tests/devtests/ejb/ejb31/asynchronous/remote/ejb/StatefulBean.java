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

package com.acme;

import jakarta.ejb.*;
import jakarta.annotation.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Stateful
public class StatefulBean implements RemoteAsync2, RemoteAsync3 {

    @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In StatefulBean::init()");
    }

    @Asynchronous
    public Future<String> helloAsync() {
        return new AsyncResult<String>("hello, world\n");
    }

    @Asynchronous
    @Remove
    public Future<String> removeAfterCalling() {
        System.out.println("In StatefulBean::removeAfterCalling()");
        return new AsyncResult<String>("removed");
    }

    @Asynchronous
        public Future<String> throwException(String exception) throws CreateException {
        if( exception.equals("jakarta.ejb.CreateException") ) {
            throw new CreateException();
        } else if ( exception.equals("jakarta.ejb.EJBException") ) {
            throw new EJBException();
        }

        return new AsyncResult<String>("unsupported exception type");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBean::destroy()");
    }



}

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

import jakarta.ejb.Stateless;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import java.util.Map;

@Stateless
@Interceptors(InterceptorB.class)
public class StatelessBean {

    @Resource
    private SessionContext sessionCtx;

    @EJB(name = "stateless/singletonref")
    private SingletonBean singleton;

    @PostConstruct
    private void init() {
        System.out.println("In StatelessBean:init()");
    }

    public void hello() {
        System.out.println("In StatelessBean::hello()");

        Map<String, Object> ctxData = sessionCtx.getContextData();
        String fooctx = (String) ctxData.get("foo");
        System.out.println("foo from context data = " + fooctx);
        if (fooctx == null) {
            throw new EJBException("invalid context data");
        }
        ctxData.put("foobar", "foobar");

        // Make sure dependencies declared in java:comp are visible
        // via equivalent java:module entries since this is a
        // .war
        SessionContext sessionCtx2 = (SessionContext) sessionCtx.lookup("java:module/env/com.acme.StatelessBean/sessionCtx");

        SingletonBean singleton2 = (SingletonBean) sessionCtx2.lookup("java:module/env/stateless/singletonref");

        // Lookup a comp env dependency declared by another ejb in the .war
        SingletonBean singleton3 = (SingletonBean) sessionCtx2.lookup("java:comp/env/com.acme.SingletonBean/me");
    }

    @PreDestroy
    private void destroy() {
        System.out.println("In StatelessBean:destroy()");
    }

}

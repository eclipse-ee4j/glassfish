/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package numberguess;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateful;
import jakarta.interceptor.Interceptors;

@Stateful
@SomeBindingType
@Interceptors(InterceptorB.class)
public class StatefulBean {

    @Resource
    private SessionContext sessionCtx;

    @EJB
    private SingletonBean singleton;

    @PostConstruct
    public void init() {
    System.out.println("In StatefulBean::init()");
    System.out.println("sessionCtx = " + sessionCtx);
    if( sessionCtx == null ) {
        throw new EJBException("EE injection error");
    }
    singleton.hello();
    }

    public void hello() {
    System.out.println("In StatefulBean::hello()");
    }

    @PreDestroy
    public void destroy() {
    System.out.println("In StatefulBean::destroy()");
    }



}

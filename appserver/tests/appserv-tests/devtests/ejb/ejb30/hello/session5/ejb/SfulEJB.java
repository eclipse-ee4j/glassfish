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

package com.sun.s1asdev.ejb.ejb30.hello.session5;

import jakarta.ejb.Stateful;
import jakarta.ejb.Remote;
import jakarta.interceptor.Interceptors;
import jakarta.ejb.EJBException;

@Stateful
@Remote({Sful.class, Sful2.class})
@Interceptors(MyInterceptor.class)
public class SfulEJB implements Sful, Sful2
{

    @Interceptors(MyInterceptor2.class)
    public String hello() {
        System.out.println("In SfulEJB:hello()");

        if( !MyInterceptor.getPostConstructCalled() ) {
            throw new EJBException("MyInterceptor.postConstruct should have " +
                                   "been called");
        }

        if( MyInterceptor2.getPostConstructCalled() ) {
            throw new EJBException("MyInterceptor2.postConstruct should not " +
                                   "have been called.  Callback methods on " +
                                   "interceptor classes do not apply to " +
                                   "method-level interceptors");
        }

        return "hello";
    }

    public String hello2() {
        System.out.println("In SfulEJB:hello2()");
        return "hello2";
    }


}

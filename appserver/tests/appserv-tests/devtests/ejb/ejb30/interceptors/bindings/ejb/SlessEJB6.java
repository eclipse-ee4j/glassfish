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

package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import jakarta.ejb.Stateless;
import jakarta.ejb.EJBException;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;


// Each interceptor list must at least have InterceptorG for
// aroundInvokeCalled state to be set correctly.

@Stateless
@ExcludeDefaultInterceptors
public class SlessEJB6 implements Sless6
{
    boolean aroundInvokeCalled = false;


    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void ag() {
        System.out.println("in SlessEJB6:ag().  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void ag(int param1) {
        System.out.println("in SlessEJB6:ag(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);

        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    // bg() (but not bg(param)) marked through ejb-jar.xml
    // as having interceptors B,G
    public void bg() {
        System.out.println("in SlessEJB6:bg().  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void bg(int param1) {
        System.out.println("in SlessEJB6:bg(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);

        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    // overloaded version of interceptor-binding used in ejb-jar.xml to
    // mark all methods with name cg as having interceptors C,G
    public void cg() {
        System.out.println("in SlessEJB6:cg().  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void cg(int param1) {
        System.out.println("in SlessEJB6:cg(int).  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    public void cg(String param1, double param2) {
        System.out.println("in SlessEJB6:cg(String, double).  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }


    // Kind of like ag(), in that dg() is overloaded, but it's the
    // signature that has a parameter that is assigned interceptors using
    // @Interceptor.
    public void dg() {
        System.out.println("in SlessEJB6:dg().  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    @Interceptors({InterceptorD.class, InterceptorG.class})
    public void dg(int param1) {
        System.out.println("in SlessEJB6:dg(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }



    // Like dg(), except that dg(int param) is assigned its interceptor
    // chain through ejb-jar.xml
    public void eg() {
        System.out.println("in SlessEJB6:eg().  aroundInvokeCalled = " +
                           aroundInvokeCalled);

        if( aroundInvokeCalled ) {
            aroundInvokeCalled = false;
            throw new EJBException("bean class aroundInvoke was incorrectly called");
        }
    }

    public void eg(int param1) {
        System.out.println("in SlessEJB6:eg(int param).  " +
                           "aroundInvokeCalled = " + aroundInvokeCalled);

        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }



}



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

package com.sun.s1asdev.ejb.ejb30.interceptors.session;



import jakarta.ejb.Stateful;

import jakarta.interceptor.InvocationContext;

import jakarta.interceptor.AroundInvoke;

import jakarta.interceptor.Interceptors;

import jakarta.ejb.EJB;

import jakarta.ejb.Remote;





@Stateful

@Interceptors({

        LifecycleCallbackInterceptor.class,

        BaseLifecycleInterceptor.class

        })

public class SfulEJB implements Sful

{



    private int count = 0;



    private int id;

    private LifecycleCallbackInterceptor interceptor;



    @EJB private Sless sless;

    @EJB private Dummy dummy;



    public String hello() {

        System.out.println("In SfulEJB:hello()");

        return "hello";

    }



    @AroundInvoke

    private Object interceptCall(InvocationContext ctx)

           throws Exception

    {

        System.out.println("**Beans AROUNDINVOKE++ [@AroundInvoke]: " + ctx.getMethod());

        count++;

        try {

            if (ctx.getMethod().getName().equals("setID")) {

                java.util.Map map = ctx.getContextData();

                interceptor = (LifecycleCallbackInterceptor) map.get("LifecycleCallbackInterceptor");

            }

            return ctx.proceed();

        } catch(EatException ee) {

            return "ate exception -- yummy!!";

        }

    }



    public int getCount() {

            return count;

    }



    public void throwAppException(String msg)

        throws AppException

    {

        throw new AppException(msg);

    }



    public String computeMid(int min, int max)

            throws SwapArgumentsException

    {

            return sless.sayHello()

                            + ", Midpoint of " + min + ", " + max + "; "

                        +  sless.computeMidPoint(min, max);

    }



    public String callDummy()

            throws Exception

    {

            return dummy.dummy();

    }



    public String eatException()

        throws EatException

    {

        System.out.println("In SfulEJB::eatException()");

        throw new EatException("SfulEJB::eatException()");

    }



    public int getPrePassivateCallbackCount() {

        return LifecycleCallbackInterceptor.getPrePassivateCallbackCount();

    }



    public int getPostActivateCallbackCount() {

        return LifecycleCallbackInterceptor.getPostActivateCallbackCount();

    }



    public void resetLifecycleCallbackCounters() {

        LifecycleCallbackInterceptor.resetLifecycleCallbackCounters();

    }



    public void setID(int val) {

        this.id = val;

        interceptor.setInterceptorID(val);

    }



    public boolean isStateRestored() {

        return interceptor.checkInterceptorID(id);

    }



    public String isInterceptorCallCounOK()

    {

                try {

                        return dummy.isInterceptorCallCounOK();

                } catch (Exception ex) {

                        System.out.println("*********");

                        ex.printStackTrace();

                        System.out.println("*********");

                }



                return null;

        }



            public String isPostConstructCallCounOK()

            {

                        try {

                                return dummy.isPostConstructCallCounOK();

                        } catch (Exception ex) {

                                System.out.println("*********");

                                ex.printStackTrace();

                                System.out.println("*********");

                        }



                        return null;

        }





}


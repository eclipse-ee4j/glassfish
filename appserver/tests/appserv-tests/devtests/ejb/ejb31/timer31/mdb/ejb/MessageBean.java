/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ejb.MessageDriven;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Timer;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.Interceptors;

import jakarta.annotation.Resource;
import jakarta.ejb.MessageDrivenContext;
import jakarta.jms.MessageListener;
import jakarta.jms.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.*;
import jakarta.ejb.EJBException;

@Interceptors(InterceptorA.class)
@MessageDriven(mappedName="jms/ejb_ejb31_timer31_mdb_InQueue", description="mymessagedriven bean description")
 @RolesAllowed("foo")
public class MessageBean implements MessageListener {

    String mname;

    @EJB
    private SingletonBean singleton;

    @Resource
        private MessageDrivenContext mdc;

    @PostConstruct
    public void init() {
        System.out.println("In MessageBean::init()");
    }

    public void onMessage(Message message) {
        System.out.println("In MessageBean::onMessage()");
        System.out.println("getCallerPrincipal = " + mdc.getCallerPrincipal());
        verifyMethodName("onMessage");
    }

    @Schedule(second="*/1", minute="*", hour="*")
    private void onTimeout() {
        System.out.println("In MessageBean::onTimeout()");
        System.out.println("getCallerPrincipal = " + mdc.getCallerPrincipal());

        verifyMethodName("onTimeout");
        try {
            System.out.println("IsCallerInRole('foo')= " +
                               mdc.isCallerInRole("foo"));
            throw new EJBException("Expecting IllegalStateEXception for call to isCallerInRole() from timer callback");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully received exception for invocation of isCallerInRole from timer callback");
        }

        if (singleton.getAroundTimeoutCalled(null)) {
            singleton.test1Passed();
        }
    }

    private void onDDTimeout(Timer t) {
        System.out.println("In MessageBean::onDDTimeout()");
        if (singleton.getAroundTimeoutCalled((String)t.getInfo())) {
            singleton.test2Passed();
        }
    }


    @PreDestroy
    public void destroy() {
        System.out.println("In MessageBean::destroy()");
    }

    @AroundTimeout
    private Object around_timeout(InvocationContext ctx) throws Exception {
        String info = (String)((Timer)ctx.getTimer()).getInfo();
        System.out.println("In MessageBean::AroundTimeout() for info " + info);
        singleton.setAroundTimeoutCalled(info);
        return ctx.proceed();
    }

    private void verifyMethodName(String name) {
        try {
            if (mname == null || !mname.equals(name))
                throw new EJBException("Expecting method named " + name + " got " + mname);
        } finally {
            mname = null;
        }
    }

}

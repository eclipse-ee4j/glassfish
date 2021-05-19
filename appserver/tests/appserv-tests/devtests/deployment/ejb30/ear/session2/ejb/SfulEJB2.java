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

package com.sun.s1asdev.ejb.ejb30.hello.session2;

import jakarta.ejb.Stateful;
import jakarta.ejb.Remote;
import jakarta.ejb.EJB;
import jakarta.annotation.PostConstruct;
import jakarta.interceptor.Interceptors;
import jakarta.ejb.EJBs;
import jakarta.ejb.Remove;
import jakarta.ejb.SessionSynchronization;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import javax.naming.InitialContext;

import jakarta.annotation.Resource;
import jakarta.transaction.UserTransaction;

import java.util.Collection;
import java.util.HashSet;

@Stateful
public class SfulEJB2 implements Sful2, SessionSynchronization
{

    // use some package-local mutable static state to check whether
    // session synch callbacks are called correctly for @Remove methods.
    // This provides a simple way to check the results since the bean
    // instance is no longer available to the caller.  The caller must
    // always at most one SFSBs of this bean type at a time for this
    // to work.
    static boolean afterBeginCalled = false;
    static boolean beforeCompletionCalled = false;
    static boolean afterCompletionCalled = false;

    private @Resource SessionContext sc;

    public String hello() {
        System.out.println("In SfulEJB2:hello()");

        return "hello";
    }

    @Remove(retainIfException=true)
    public void removeRetainIfException(boolean throwException)
        throws Exception {

        System.out.println("In SfulEJB2 " +
                           " removeRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeNotRetainIfException(boolean throwException)
        throws Exception {

        System.out.println("In SfulEJB2 " +
                           "removeNotRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeMethodThrowSysException(boolean throwException) {

        System.out.println("In SfulEJB2 " +
                           "removeMethodThrowSysException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new EJBException
                ("throwing system exception from @Remove method");
        }
    }

    public void afterBegin() {

        afterBeginCalled = true;
        beforeCompletionCalled = false;
        afterCompletionCalled = false;

        System.out.println("In SfulEJB2::afterBegin()");
    }

    public void beforeCompletion() {
        System.out.println("In SfulEJB2::beforeCompletion()");
        beforeCompletionCalled = true;
    }

    public void afterCompletion(boolean committed) {
        afterCompletionCalled = true;
        System.out.println("In SfulEJB2::afterCompletion()");
    }

}

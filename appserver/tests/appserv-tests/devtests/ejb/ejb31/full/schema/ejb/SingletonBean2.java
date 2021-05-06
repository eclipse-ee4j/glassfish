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

public class SingletonBean2 {

    private int initTx = -1;

    private void init() {
        System.out.println("In SingletonBean2::init()");
        try {
            javax.naming.InitialContext ic = new javax.naming.InitialContext();
            jakarta.transaction.TransactionSynchronizationRegistry tr =
                 (jakarta.transaction.TransactionSynchronizationRegistry)
                 ic.lookup("java:comp/TransactionSynchronizationRegistry");
            System.out.println("In SingletonBean2::init() tx status: " + tr.getTransactionStatus());
            initTx = tr.getTransactionStatus();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void foo() {
        System.out.println("In SingletonBean2::foo()");
    }

    public void foo2() {
        System.out.println("In SingletonBean2::foo2()");
        if (initTx != 6)
            throw new RuntimeException("initTx is " + initTx);
    }

    public void fooAsync(int sleepSeconds) {
        System.out.println("In SingletonBean2::fooAsync() Sleeping for " +
                           sleepSeconds + " seconds...");
        try {
            Thread.sleep(sleepSeconds * 1000);
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("fooAsync() awoke from Sleep");
    }

    private void destroy() {
        System.out.println("In SingletonBean2::destroy()");
    }

    private void myTimeout() {
        System.out.println("In SingletonBen2::myTimeout()");
    }

}

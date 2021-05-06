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

package com.sun.s1asdev.ejb.txprop.cmttimeout;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;


public class HelloBean implements SessionBean {

    private SessionContext sc;

    public HelloBean() {}

    public void ejbCreate() throws RemoteException {
        System.out.println("In HelloBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void compute(int timeout) {
        System.out.println("hello from HelloBean::compute() " + timeout);
        long deadline = System.currentTimeMillis() + (timeout * 1000);
        for (int n = 1; true; n = n << 1) {
            long now = System.currentTimeMillis();

            if (now > deadline) {
                break;
            }
            System.out.println("Hello, Sorting for n = " + n
                    + "; time left = " + ((deadline - now) / 1000)
                    + " seconds.");
            sortArray(1024);
            try {Thread.sleep(10);} catch (Exception ex) {}
        }
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    private void sortArray(int n) {
        int[] a = new int[n];
        for (int i=0; i < n; i++) {
            for (int j=i+1; j<n; j++) {
                if (a[j] < a[i]) {
                    int temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
    }

}

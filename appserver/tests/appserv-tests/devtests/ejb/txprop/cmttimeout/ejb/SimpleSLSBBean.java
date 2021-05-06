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

package com.sun.s1asdev.ejb.slsb;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;


public class SimpleSLSBBean
    implements SessionBean
{

    private SessionContext sc;

    public SimpleSLSBBean() {
    }

    public void ejbCreate() throws RemoteException {
            System.out.println("In SimpleSLSBHome::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
            this.sc = sc;
    }

    public boolean doSomething(int timeout) {
        boolean result = sc.getRollbackOnly();

        try {
            System.out.println("Inside doSomething(" + timeout + ")");
            System.out.println("Before entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);
            long deadline = System.currentTimeMillis() + (timeout * 1000);
            for (int n = 1; true; n = n << 1) {
                long now = System.currentTimeMillis();

                if (now > deadline) {
                    break;
                }
                result = sc.getRollbackOnly();
                System.out.println("Hello, Sorting for n = " + n
                        + "; time left = " + ((deadline - now) / 1000)
                        + " seconds. result: " + result);
                sortArray(1024);
                try {Thread.sleep(5 * 1000);} catch (Exception ex) {}
            }

            result = sc.getRollbackOnly();
            System.out.println("After entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);
        } catch (Exception ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
    }

    public boolean doSomethingAndRollback() {
        boolean result = sc.getRollbackOnly();

        try {
            System.out.println("Inside doSomethingAndRollback()");
            System.out.println("Before entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);

            sc.setRollbackOnly();

            result = sc.getRollbackOnly();
            System.out.println("After entity.invoke(), "
                    + "sctx.getRollbackOnly(): " + result);
        } catch (Exception ex) {
            ex.printStackTrace();
            result = false;
        }

        return result;
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

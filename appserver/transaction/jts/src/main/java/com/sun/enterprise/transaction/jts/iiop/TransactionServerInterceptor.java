/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.jts.iiop;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.hk2.api.ServiceLocator;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

public class TransactionServerInterceptor extends LocalObject
        implements ServerRequestInterceptor, Comparable {

    private static final String name = "TransactionServerInterceptor";
    private int order;

    private JavaEETransactionManager tm;
    private GlassFishORBHelper gfORBHelper = null;

    /**
     * Construct the interceptor.
     * @param the order in which the interceptor should run.
     */
    public TransactionServerInterceptor(int order, ServiceLocator habitat) {
        this.order = order;
        gfORBHelper = habitat.getService(GlassFishORBHelper.class);
        tm = habitat.getService(JavaEETransactionManager.class);
    }

    public String name() {
        return name;
    }

    public void receive_request_service_contexts(ServerRequestInfo sri) { }


    public int compareTo(Object o) {
        int otherOrder = -1;
        if (o instanceof TransactionServerInterceptor) {
            otherOrder = ((TransactionServerInterceptor) o).order;
        }
        if (order < otherOrder) {
            return -1;
        } else if (order == otherOrder) {
            return 0;
        }
        return 1;
    }


    public void destroy() {
    }

    public void receive_request(ServerRequestInfo sri) {
    }

    public void send_reply(ServerRequestInfo sri) {
        checkTransaction(sri);
    }

    public void send_exception(ServerRequestInfo sri) {
        checkTransaction(sri);
    }

    public void send_other(ServerRequestInfo sri) {
        checkTransaction(sri);
    }

    private void checkTransaction(ServerRequestInfo sri) {
        try {
            if (tm != null)
                tm.checkTransactionImport();
        } finally {
            if (gfORBHelper.isEjbCall(sri)) {
                tm.cleanTxnTimeout();
            }
        }
    }
}

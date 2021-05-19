/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.api.ServiceLocator;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

public class TransactionClientInterceptor extends LocalObject
        implements ClientRequestInterceptor, Comparable<TransactionClientInterceptor> {

    private String name;
    private int order;

    private JavaEETransactionManager tm;

    /**
     * Create the interceptor.
     * @param the name of the interceptor.
     * @param the order in which the interceptor should be invoked.
     */
    public TransactionClientInterceptor(String name, int order, ServiceLocator habitat) {
        this.name = name;
        this.order = order;
        tm = habitat.getService(JavaEETransactionManager.class);
    }

    public int compareTo(TransactionClientInterceptor o) {
        int otherOrder = o.order;

        if (order < otherOrder) {
            return -1;
        } else if (order == otherOrder) {
            return 0;
        }
        return 1;
    }

    /**
     * Return the name of the interceptor.
     * @return the name of the interceptor.
     */
    public String name() {
        return name;
    }


    public void send_request(ClientRequestInfo cri) {
        // Check if there is an exportable transaction on current thread
        Object target = cri.effective_target();
        if (tm != null) {
            tm.checkTransactionExport(StubAdapter.isLocal(target));
        }
    }

    public void destroy() {
    }

    public void send_poll(ClientRequestInfo cri) {
    }

    public void receive_reply(ClientRequestInfo cri) {
    }

    public void receive_exception(ClientRequestInfo cri) {
    }

    public void receive_other(ClientRequestInfo cri) {
    }
}


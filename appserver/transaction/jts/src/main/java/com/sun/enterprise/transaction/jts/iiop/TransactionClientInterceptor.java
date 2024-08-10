/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import org.glassfish.hk2.api.ServiceLocator;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

import static com.sun.corba.ee.spi.presentation.rmi.StubAdapter.isLocal;

public class TransactionClientInterceptor extends LocalObject implements ClientRequestInterceptor, Comparable<TransactionClientInterceptor> {

    private static final long serialVersionUID = 1L;

    private String name;
    private int order;
    private JavaEETransactionManager eeTransactionManager;

    /**
     * Create the interceptor.
     *
     * @param the name of the interceptor.
     * @param the order in which the interceptor should be invoked.
     */
    public TransactionClientInterceptor(String name, int order, ServiceLocator serviceLocator) {
        this.name = name;
        this.order = order;
        eeTransactionManager = serviceLocator.getService(JavaEETransactionManager.class);
    }

    @Override
    public int compareTo(TransactionClientInterceptor o) {
        int otherOrder = o.order;

        if (order < otherOrder) {
            return -1;
        }

        if (order == otherOrder) {
            return 0;
        }

        return 1;
    }

    /**
     * Return the name of the interceptor.
     *
     * @return the name of the interceptor.
     */
    @Override
    public String name() {
        return name;
    }

    @Override
    public void send_request(ClientRequestInfo clientRequestInfo) {
        // Check if there is an exportable transaction on current thread
        Object target = clientRequestInfo.effective_target();
        if (eeTransactionManager != null) {
            eeTransactionManager.checkTransactionExport(isLocal(target));
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void send_poll(ClientRequestInfo cri) {
    }

    @Override
    public void receive_reply(ClientRequestInfo cri) {
    }

    @Override
    public void receive_exception(ClientRequestInfo cri) {
    }

    @Override
    public void receive_other(ClientRequestInfo cri) {
    }
}

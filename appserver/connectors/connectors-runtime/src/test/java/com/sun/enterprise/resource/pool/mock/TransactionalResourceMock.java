/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.mock;

import jakarta.transaction.Transaction;

import javax.transaction.xa.XAResource;

/**
 * Mock class without any implementation
 */
public class TransactionalResourceMock implements com.sun.enterprise.transaction.spi.TransactionalResource {

    @Override
    public void closeUserConnection() throws Exception {
    }

    @Override
    public void enlistedInTransaction(Transaction arg0) throws IllegalStateException {
    }

    @Override
    public Object getComponentInstance() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public XAResource getXAResource() {
        return null;
    }

    @Override
    public boolean isEnlisted() {
        return false;
    }

    @Override
    public boolean isEnlistmentSuspended() {
        return false;
    }

    @Override
    public boolean isShareable() {
        return false;
    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public void setComponentInstance(Object arg0) {
    }

    @Override
    public boolean supportsXA() {
        return false;
    }

}

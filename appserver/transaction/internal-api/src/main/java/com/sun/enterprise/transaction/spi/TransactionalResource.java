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

package com.sun.enterprise.transaction.spi;

import jakarta.transaction.Transaction;

import javax.transaction.xa.XAResource;

/**
 * TransactionalResource interface to be implemented by the resource handlers
 * to be able to communicate with used the transaction manager components
 *
 * @author Marina Vatkina
 */

public interface TransactionalResource {

    public boolean isTransactional();

    //TODO V3 not needed as of now.
    public boolean isEnlistmentSuspended();

    public XAResource getXAResource();

    public boolean supportsXA();

    public Object getComponentInstance();

    public void setComponentInstance(Object instance);

    public void closeUserConnection() throws Exception;

    public boolean isEnlisted();

    public boolean isShareable();

    /**
     * @return the String that can identify this resource
     */
    public String getName();

    /**
     * Indicates that a resource has been enlisted in the transaction.
     * @param tran Transaction to which the resource is enlisted
     * @throws IllegalStateException when unable to enlist the resource
     */
    void enlistedInTransaction(Transaction tran) throws IllegalStateException;
}

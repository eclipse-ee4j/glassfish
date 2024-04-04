/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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
 * TransactionalResource interface to be implemented by the resource handlers to be able to communicate with the
 * transaction manager components.
 *
 * @author Marina Vatkina
 */
public interface TransactionalResource {

    /**
     * Returns true if the resource is part of a transaction.
     *
     * @return true if the resource is part of a transaction.
     */
    public boolean isTransactional();

    /**
     * To check whether lazy enlistment is suspended or not.<br>
     * If {@code true}, transaction manager will not do enlist/lazy enlist.
     *
     * @return true if enlistment is suspended, otherwise false.
     */
    public boolean isEnlistmentSuspended();

    /**
     * Returns the (optional) XAResource reference for this resource handle.
     *
     * @return the XAResource reference for this resource handle or null if no reference is set.
     */
    public XAResource getXAResource();

    /**
     * Returns true if the ResourceHandle is supported in an XA transaction.
     *
     * @return true if the ResourceHandle is supported in an XA transaction, otherwise false.
     */
    public boolean supportsXA();

    /**
     * Returns the component instance holding this resource handle.
     *
     * @return the component instance holding this resource handle.
     */
    public Object getComponentInstance();

    /**
     * Sets the component instance holding this resource handle.
     *
     * @param instance the component instance holding this resource handle.
     */
    public void setComponentInstance(Object instance);

    /**
     * Closes the (optional) 'userConnection' / 'connection handle' (used by the application code to refer to the underlying
     * physical connection). Example: the ManagedConnection represented by this ResourceHandle is closed / cleaned up.
     *
     * @throws PoolingException wrapping any 'userConnection' specific exception that might occur during the close call.
     * @throws UnsupportedOperationException when the method is not implemented.
     */
    public void closeUserConnection() throws Exception;

    /**
     * Returns true if the resource handle is enlisted in a transaction.
     *
     * @return true if the resource handle is enlisted in a transaction.
     */
    public boolean isEnlisted();

    /**
     * Returns true if the resource handle is sharable within the component.
     *
     * @return true if the resource handle is sharable within the component.
     */
    public boolean isShareable();

    /**
     * Returns the String that can identify this resource.
     *
     * @return the String that can identify this resource.
     */
    public String getName();

    /**
     * Indicates that a resource has been enlisted in the transaction.
     *
     * @param transaction the Transaction to which the resource is enlisted.
     * @throws IllegalStateException when unable to enlist the resource.
     */
    void enlistedInTransaction(Transaction transaction) throws IllegalStateException;
}

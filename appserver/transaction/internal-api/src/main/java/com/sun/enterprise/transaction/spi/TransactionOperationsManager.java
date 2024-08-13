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

import javax.naming.NameNotFoundException;

import org.jvnet.hk2.annotations.Contract;

/**
 * TransactionOperationsManager interface to be used by various components
 * to perform notifications by UserTransaction instance.
 *
 * @author Marina Vatkina
 */
@Contract
public interface TransactionOperationsManager {
    /**
     * Called by the UserTransaction implementation to verify
     * access to the UserTransaction methods.
     */
    boolean userTransactionMethodsAllowed();

    /**
     * Called by the UserTransaction lookup to verify
     * access to the UserTransaction itself.
     */
    void userTransactionLookupAllowed() throws NameNotFoundException;

    /**
     * Called by the UserTransaction when transaction is started.
     */
    void doAfterUtxBegin();
}

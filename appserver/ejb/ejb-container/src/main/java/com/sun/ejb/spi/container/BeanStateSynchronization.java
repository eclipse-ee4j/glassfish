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

package com.sun.ejb.spi.container;

import jakarta.ejb.DuplicateKeyException;

/**
 * There are cases where the container would need to interact with the
 * persistence manager. Some known cases are listed below
 * 1. provide the user with a mechanism to flush changes to the database
 *    at the end of a method with out waiting until the end of the transaction.
 * 2. for read only beans provide a mechanism to have the master copy of the bean
 *    sync up with the database record.
 *
 * Currently the bean concrete implementation that is created as part of the codegen
 * would implement this interface.
 *
 * @author Pramod Gopinath
 */


public interface BeanStateSynchronization {
    /**
     * Provides a mechanism to flush changes to the database w/o waiting for
     * the end of the transaction, based on some descriptor values set by the user.
     * The container would call this method in the postInvoke(), only if the flush
     * is enabled for the current method and there were no other exceptions set
     * into inv.exception.
     */
    public void ejb__flush()
        throws DuplicateKeyException;

    /**
     * On receiving this message the PM would update its master copy
     * by fetching the latest information for the primary key from the database
     */
    public void ejb__refresh(Object primaryKey);


    /**
     * On receiving this message the PM would delete from its master copy
     * the details related to the primaryKey
     */
    public void ejb__remove(Object primaryKey);
}

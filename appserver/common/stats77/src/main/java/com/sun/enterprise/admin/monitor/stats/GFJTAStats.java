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

package com.sun.enterprise.admin.monitor.stats;

import java.util.List;
import java.util.Map;

import org.glassfish.j2ee.statistics.JTAStats;

/** Defines additional Sun ONE Application Server specific statistic to transaction service.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.4 $
 */
public interface GFJTAStats extends JTAStats {

    /** Returns the IDs of the transactions that are currently active, as a StrignStatistic.
     * An active transaction is same as an in-flight transaction. Every such transaction can be rolled back after
     * freezing the transaction service.
     * @see org.glassfish.j2ee.statistics.JTAStats#getActiveCount
     * @return        a comma separated String of IDs
     */
    public StringStatistic getActiveIds();

    /** Returns the current state of the transaction service as a StringStatistic.
     *
     * @return        String representing the current state of the transaction service.
     */
    public StringStatistic getState();

    /** Freezes the transaction subsystem. This operation is advised before
     * an attempt is made to rollback any transactions to avoid the possibility of
         * transaction completion before the rollback request is issued. The transaction subsystem
     * is expected be active before it can be frozen. Calling this methd on
     * an already frozen transaction subsystem has no effect.
     */
    public void freeze();

    /** Unfreezes the transaction subsystem. It is required to unfreeze the
     * transaction subsystem after it is frozen earlier.
     * Calling this method when system is not active, has no effect.
     */
    public void unfreeze();

    /**
     * Rolls back a given transaction. It is advisable to call this method
         * when the transaction subsystem is in a frozen state so that transactions
         * won't be completed before this request. It is left to implementation how
     * the transactions are rolled back.
     * @param    String representing the unique id of the transaction that
     * needs to be rolled-back. Every transaction that can be rolled back
     * has to be an in flight transaction.
         * @return String contains the status of the rollback operation for the given txnId.
         * status contains "Rollback successful",
         * "Rollback unsuccessful. Current Thread is not associated with the transaction",
         * "Rollback unsuccessful. Thread is not allowed to rollback the transaction",
         * "Rollback unsuccessful. Unexpected error condition encountered by Transaction Manager".
     */
    public String rollback(String txnId);


    /**
     * Rolls back the given transactions. It is advisable to call this method
         * when the transaction subsystem is in a frozen state so that transactions
         * won't be completed before this request. It is left to implementation how
     * the transactions are rolled back.
     * @param    String array representing the unique ids of the transactions that
     * need to be frozen. Every transaction that can be rolled back has to be an
     * in flight transaction.
     * @return    String[] containing the status for the given transaction ids.
         * status contains "Successful","Invalid transaction Id or Transaction is over",
         * "Rollback unsuccessful. Current Thread is not associated with the transaction",
         * "Rollback unsuccessful. Unexpected error condition encountered by Transaction Manager".

    public String[] rollback(String[] txnIds);
     */

        // To be used for GUI ...
        public List<Map<String, String>> listActiveTransactions();

    /**
     * Utility method to find out if in place recovery is required.
     * @return true if the recovery is required, else false
     */
    public Boolean isRecoveryRequired();
}


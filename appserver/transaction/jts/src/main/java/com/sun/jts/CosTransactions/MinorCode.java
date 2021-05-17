/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1995-1997 IBM Corp. All rights reserved.
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

//----------------------------------------------------------------------------
//
// Module:      MinorCode.java
//
// Description: JTS standard exception minor codes.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

/**
 * This class simply contains minor code values for standard exceptions thrown
 * by the JTS.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
 */
public interface MinorCode  {

    /**
     * This minor code is used on standard exceptions.
     * <p> It indicates that there is no
     * further information for the exception.
     */
    public static int Undefined = 0x0000;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates that the transaction is invalid because
     * it has unfinished subtransactions.
     */
    public static int UnfinishedSubtransactions = 0x0001;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that the transaction is invalid because it has outstanding work
     * (other threads either in the same process or other processes
     * which are still associated with the transaction).
     */
    public static int DeferredActivities = 0x0002;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that the transaction has completed and the operation is not valid.
     */
    public static int Completed = 0x0003;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that the TransactionFactory was unable to create the transaction.
     */
    public static int FactoryFailed = 0x0004;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that an XA Resource Manager is doing work outside of a transaction
     * on the current thread and cannot allow the begin or resume operation.
     */
    public static int XAOutside = 0x0005;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates that a reply is returning when a different transaction
     * is active from the one active when the request was imported.
     */
    public static int WrongContextOnReply = 0x0006;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that the is_same_transaction operation has been invoked with a parameter
     * which represents a Coordinator object from a different implementation
     * of the OTS interfaces, and that Coordinator object is in the process
     * of ending the transaction.  In this case, the JTS cannot obtain the
     * necessary information to determine equality of the Coordinator objects.
     */
    public static int CompareFailed = 0x0007;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates
     * that the object could not locate the Coordinator for its transaction.
     */
    public static int NoCoordinator = 0x0101;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p>It indicates
     * that the object did not have access to the global identifier for the
     * transaction which it represents.
     */
    public static int NoGlobalTID = 0x0102;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates that the object represents a subtransaction
     * and was called for a top-level transaction operation.
     */
    public static int TopForSub = 0x0103;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates
     * that the object represents a top-level transaction and was called for a
     * subtransaction operation.
     */
    public static int SubForTop = 0x0104;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates that a stacked Control object already exists
     * when beginning a subtransaction.
     */
    public static int AlreadyStacked = 0x0105;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates
     * that an internal logic error was detected.
     */
    public static int LogicError = 0x0106;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates
     * that a Resource could not be registered by a subordinate.
     */
    public static int NotRegistered = 0x0107;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates
     * that a RecoveryCoordinator could not be created.
     */
    public static int RecCoordCreateFailed = 0x0108;

    /**
     * This minor code is used on the INTERNAL exception.
     * <p> It indicates
     * that the TransactionService could not be created.
     */
    public static int TSCreateFailed = 0x0109;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that recreating a imported transaction has failed.
     */
    public static int TX_RECREATE_FAILED = 0x010A;

    /**
     * This minor code is used on the INVALID_TRANSACTION exception.
     * <p> It indicates
     * that concurrent activity within a transaction is disallowed.
     */
    public static int TX_CONCURRENT_WORK_DISALLOWED = 0x010B;
}

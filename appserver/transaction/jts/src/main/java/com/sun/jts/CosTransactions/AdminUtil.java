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

package com.sun.jts.CosTransactions;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * AdminUtil - Utility class for monitoring and administering jts.
 *
 * This class is used by admin utilities for performing monitoring and
 * administration.
 * @author <a href="mailto:ajay.kr@sun.com">Ajay Kumar</a>
 * @version 1.0
 */
public class AdminUtil
{
    //freezing functionality

    /**
     * freezeAll
     *
     * Freeze all transactional activity.
     *
     * Part of freezing functions that will be used to achive a
     * transactional quiet period before taking any action or collecting
     * any statistics. This call returns when all the activities are frozen.
     * All the states would be allowed to complete before freezing on. For
     * example a transaction in preparing state would be allowed to transition
     * to next state - the prepared state.
     *
     */
    public static void freezeAll()
    {
        TransactionState.freezeLock.acquireWriteLock();
    }

    /**
     * unfreezeAll
     *
     * Unfreeze all transactional activity frozen by a freezeAll call.
     *
     * Part of freezing functions that will be used to achive a
     * transactional quiet period before taking any action or collecting
     * any statistics. This call returns almost immediately.
     *
     */
    public static void unfreezeAll()
    {
        TransactionState.freezeLock.releaseWriteLock();
    }


    /**
     * isFrozenAll
     *
     * Get the current state.
     *
     * Part of freezing functions that will be used to achive a
     * transactional quiet period before taking any action or collecting
     * any statistics. This call returns almost immediately.
     *
     */
    public static boolean isFrozenAll()
    {
        return TransactionState.freezeLock.isWriteLocked();
    }



    /* For future use
     *
    public static void freeze(Control ctrl)
    {
    }
     */


    /* For future use.
     *
    public static void unfreeze(Control ctrl)
    {
    }
     */

    //Sampling control

    /**
     * startSampling
     *
     * Start the sampling window. The sample window determines the duration
     * in which the data is collected. Start and stop calls demarcate the
     * the window. The sampling data could be number of transactions committed
     * ,rolledback etc and transient data like pending etc. It also resets
     * various counters.
     *
     * @see stopSampling
     */
    public static void startSampling()
    {
        if(!bSampling){
            try
            {
                statisticsLock.acquireWriteLock();
                lSampleEndTime = 0 ;
                    lSampleStartTime = System.currentTimeMillis();
                    iCommits = 0;
                iAborts = 0;
                iPending = 0;
                iRecCommits = 0;
                iRecAborts = 0;
                //iImmigerent = 0;
                //iEmmigerent = 0;
                Iterator iter = getAllTransactions().iterator();

                while ( iter.hasNext() )
                {
                    CoordinatorImpl coord = (CoordinatorImpl)iter.next();
                    if ( coord.get_status() ==
                     org.omg.CosTransactions.Status.StatusPrepared )
                    iPending++;
                   }

                bSampling = true;
            }
            finally
               {
                    statisticsLock.releaseWriteLock();
            }
        }
    }


    /**
     * stopSampling
     *
     * Stop sampling the statitics values. This is used to indicate end of
     * sampling window.
     *
     * @see startSampling
     *
     */
    public static void stopSampling()
    {
        try
        {
            statisticsLock.acquireWriteLock();
            lSampleEndTime = System.currentTimeMillis();
            bSampling = false;
        }
        finally
        {
            statisticsLock.releaseWriteLock();
        }
    }

    //data collection

    /**
     * Increment the count of committed transactions.
     */
    static void incrementCommitedTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkCommits )
            {
                iCommits++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }

    /**
     * Increment the count of transactions that were rolled back.
     *
     */
    static void incrementAbortedTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkAborts )
            {
                iAborts++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }

    /**
     * Increment the count of transactions that were rolled back and ne'er went through prepare phase.
     *
     */
    static void incrementUnpreparedAbortedTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkUAborts )
            {
                iUAborts++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }


    /**
     * Increment the count of pending transactions
     * - i.e. the transactions entering the prepared state.
     *
     */
    static void incrementPendingTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkPending )
            {
                iPending++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }

    /**
     * Increments the number of transactions that were commited as part of
     * the recovery process.
     */
    public static void incrementRecoveryCommitedTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkRecCommits )
            {
                iRecCommits++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }


    /**
     * Increments the number of transactions that were rolled back as part
     * of the recovery process.
     */
    public static void incrementRecoveryAbortedTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkRecAborts )
            {
                iRecAborts++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }


    /* For future use
     *
    public static void incrementImmigerentTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkImmigerent )
            {
                iImmigerent++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }
     */

    /* For future use
     *
    public static void incrementEmmigerentTransactionCount()
    {
        try
        {
            statisticsLock.acquireReadLock();
            synchronized ( lkEmmigerent )
            {
                iEmmigerent++;
            }
        }
        finally
        {
            statisticsLock.releaseReadLock();
        }
    }
     */


    //gathering statistics

    /**
     * Return the committed transactions count.
     *
     */
    public static long getCommitedTransactionCount()
    {
        return iCommits;
    }

    /**
     * Return the count of rolledback transactions.
     *
     */
    public static long getAbortedTransactionCount()
    {
        return iAborts;
    }

    /**
     * Return the number of transactions that are active currently.
     *
     */
    public static long getActiveTransactionCount()
    {
        return RecoveryManager.getCoordsByGlobalTID().size()
            - getPendingTransactionCount();
    }

    /**
     * Return the count of transactions that are prepared - but not completed.
     * This includes the total transactions that ever entered into prepared state
     * minus ones which got committed, rolledback, taking care of ones that were
     * rolledback without entering prepared state.
     *
     */
    public static long getPendingTransactionCount()
    {
        return iPending-iAborts-iCommits+iUAborts;
    }

    /**
     * Return the count of transactions that were commited as part of the
     * recovery process.
     */
    public static long getRecoveryCommitedTransactionCount()
    {
        return iRecCommits;
    }


    /**
     * Return the number of transactions that were rolled back as part of the
     * recovery process.
     */
    public static long getRecoveryAbortedTransactionCount()
    {
        return iRecAborts;
    }


    /**
     *
    public static long getImmigerentTransactionCount()
    {

        return iImmigerent;
    }
     */

    /**
     *
    public static long getEmmigerentTransactionCount()
    {
        return iEmmigerent;
    }
     */

    /**
     * Returns the time in milliseconds, since the begining of time, when the
     * sampling window was started.
     */
    public static long getSamplingStartTime()
    {
        return lSampleStartTime;
    }


    /**
     * Returns the time in millisecond , since the begining of time, when the
     * sampling window was closed.
     */
    public static long getSamplingEndTime()
    {
        return lSampleEndTime;
    }

    /**
     * Returns the duration for which the last sampling window was open, in milliseconds.
     * If the sampling window is still open, then the running duration is returned.
     */
    public static long getSamplingDuration()
    {
        return ( lSampleEndTime == 0 && lSampleStartTime != 0 )
            ? System.currentTimeMillis() - lSampleStartTime
            : lSampleEndTime - lSampleStartTime;
    }


    // Enumerating...

    /**
     * Returns the collection of existing coordinators.
     *
     */
    public static Collection/*<Coordinator>*/ getAllTransactions()
    {
        return RecoveryManager.getCoordsByGlobalTID().values();
    }

    /**
     * Returned enumeration of globaltids of existing transactions.
     *
     */
    public static Enumeration/*<GlobalTID>*/ getAllTIDs()
    {
        return RecoveryManager.getCoordsByGlobalTID().keys();
    }


    private static RWLock statisticsLock = new RWLock() ;
    private static long lSampleStartTime = 0 ;
    private static long lSampleEndTime = 0 ;
    static boolean bSampling = false ;
    private static int iCommits = 0;
    private static int iAborts = 0;
    private static int iUAborts=0;
    private static int iPending = 0;
    static int iRecCommits = 0;
    static int iRecAborts = 0;
    /*
     * for future use
     *
    static int iImmigerent = 0;
    static int iEmmigerent = 0;
     */
    private static Object lkCommits = new Object();
    private static Object lkAborts = new Object();
    private static Object lkUAborts = new Object();
    private static Object lkPending = new Object();
    static Object lkRecCommits = new Object();
    static Object lkRecAborts = new Object();

    /*
     * for future use
     *
    static Object lkImmigerent = new Object();
    static Object lkEmmigerent = new Object();
     */

}

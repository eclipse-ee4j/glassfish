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
// Module:      EventSemaphore.java
//
// Description: Event semaphore implementation.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

/**The EventSemaphore interface provides operations that wait for and post an
 * event semaphore.
 * <p>
 * This is specifically to handle the situation where the event may have been
 * posted before the wait method is called.  This behaviour is not supported by
 * the existing wait and notify methods.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
 */
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//-----------------------------------------------------------------------------

public class EventSemaphore {
    boolean posted = false;

    /**Default EventSemaphore constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    EventSemaphore() {
    }

    /**Creates the event semaphore in the given posted state.
     *
     * @param posted  Indicates whether the semaphore should be posted.
     *
     * @return
     *
     * @see
     */
    EventSemaphore( boolean posted ) {
        this.posted = posted;
    }

    /**
     * @return true if semaphore has already been posted.
     */
    synchronized public boolean isPosted() {
        return posted;
    }

    /**Waits for the event to be posted.
     * <p>
     * If the event has already been posted, then the operation returns immediately.
     *
     * @param
     *
     * @return
     *
     * @exception InterruptedException  The wait was interrupted.
     *
     * @see
     */
    synchronized public void waitEvent()
            throws InterruptedException {
        if( !posted )
            wait();
    }

   /*Waits for the event to be posted. Release the thread waiting after the CMT
     * Timeout period if no event has been posted during this timeout interval.
     * <p>
     * If the event has already been posted, then the operation returns immediately.
     *
     * @param cmtTimeout - container managed transaction timeout
     *
     * @return
     *
     * @exception InterruptedException  The wait was interrupted.
     *
     * @see
     */

    synchronized public void waitTimeoutEvent(int cmtTimeout)
            throws InterruptedException {

        if (!posted) {
            long timeout = (System.currentTimeMillis() / 1000) + cmtTimeout;
            while (!posted && timeout - (System.currentTimeMillis() / 1000) > 0) {
                wait(timeout - (System.currentTimeMillis() / 1000));
            }
        }
    }

    /**Posts the event semaphore.
     * <p>
     * All waiters are notified.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    synchronized void post() {
        if( !posted )
            notifyAll();
        posted = true;
    }

    /**Clears a posted event semaphore.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    synchronized void clear() {
        posted = false;
    }
}

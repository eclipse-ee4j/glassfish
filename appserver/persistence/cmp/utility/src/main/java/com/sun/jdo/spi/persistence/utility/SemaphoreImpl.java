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

package com.sun.jdo.spi.persistence.utility;

import com.sun.jdo.spi.persistence.utility.logging.Logger;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;


/** Implements a simple semaphore.
 *
 * @author Dave Bristor
 * @author Marina Vatkina
 */
// db13166: I would rather we use Doug Lea's stuff, but don't want to
// introduce that magnitude of change at this point in time.
public class SemaphoreImpl implements Semaphore {
    /** Where to log messages about locking operations
     */
    private static final Logger _logger = LogHelperUtility.getLogger();

    /** For logging, indicates on whose behalf locking is done.
     */
    private final String _owner;

    /** Synchronizes the lock.
     */
    private final Object _lock = new Object();

    /** Thread which holds the lock.
     */
    private Thread _holder = null;

    /** Semaphore counter.
     */
    private int _counter = 0;

    /**
     * I18N message handler
     */
    private final static ResourceBundle messages =
        I18NHelper.loadBundle(SemaphoreImpl.class);


    public SemaphoreImpl(String owner) {
        _owner = owner;
    }

    /** Acquire a lock.
     */
    public void acquire() {
        boolean debug = _logger.isLoggable(Logger.FINEST);

        if (debug) {
            Object[] items = new Object[] {_owner, Thread.currentThread(),new Integer(_counter)};
            _logger.finest("utility.semaphoreimpl.acquire",items); // NOI18N
        }

        synchronized (_lock) {
            //
            // If the current thread already holds this lock, we simply
            // update the count and return.
            //
            if (Thread.currentThread() == _holder) {
                _counter++;

            } else {
                while (_counter > 0) {
                    try {
                        // wait for the lock to be released
                        _lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                _holder = Thread.currentThread();
                _counter++;

                if (debug) {
                    Object[] items = new Object[] {_owner, Thread.currentThread(),new Integer(_counter)};
                    _logger.finest("utility.semaphoreimpl.gotlock",items); // NOI18N
                }
            }
        }
    }

    /** Release a lock.
     */
    public void release() {
        boolean debug = _logger.isLoggable(Logger.FINEST);

        if (debug) {
            Object[] items = new Object[] {_owner, Thread.currentThread(),new Integer(_counter)};
            _logger.finest("utility.semaphoreimpl.release",items); // NOI18N
        }

        synchronized (_lock) {
            //
            // If the current thread already holds this lock, we simply
            // update the count and return.
            //
            if (Thread.currentThread() == _holder) {
                if (--_counter == 0) {
                    _holder = null;
                    _lock.notify();
                }
            } else {
                throw new IllegalMonitorStateException(
                  I18NHelper.getMessage(messages,
                                        "utility.semaphoreimpl.wrongthread", // NOI18N
                                        new Object[] {_owner, Thread.currentThread()}));
            }
        }
    }
}

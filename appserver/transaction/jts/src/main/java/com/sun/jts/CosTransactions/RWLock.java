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
import com.sun.logging.LogDomains;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A <i>RWLock</i> provides concurrency control for multiple readers single writer
 * access patterns. This lock can provide access to multiple reader threads simultaneously
 * as long as there are no writer threads. Once a writer thread gains access to the
 * instance locked by a RWLock, all the reader threads wait till the writer completes
 * accessing the instance in question.
 * <p>
 * A RWLock is extremely useful in scenarios where there are lots more readers and
 * very few writers to a data structure. Also if the read operation by the reader
 * thread could take significant amount of time (binary search etc.)
 * <p>
 * The usage of Lock can be see as under:
 *  <p><hr><blockquote><pre>
 *    public class MyBTree {
 *      private RWLock lock = new Lock();
 *      .....
 *      .....
 *      public Object find(Object o) {
 *        try {
 *          lock.acquireReadLock();
 *          ....perform complex search to get the Object ...
 *          return result;
 *        } finally {
 *          lock.releaseReadLock();
 *        }
 *      }
 *
 *      public void insert(Object o) {
 *        try {
 *          lock.acquireWriteLock();
 *          ....perform complex operation to insert object ...
 *        } finally {
 *          lock.releaseWriteLock();
 *        }
 *      }
 *    }
 * </pre></blockquote><hr>
 * <p>
 * @author Dhiru Pandey 8/7/2000
 */
public class RWLock {

    int currentReaders;
    int pendingReaders;
    int currentWriters;
    /*
      Logger to log transaction messages
     */
    static Logger _logger = LogDomains.getLogger(RWLock.class, LogDomains.TRANSACTION_LOGGER);

    Queue writerQueue = new Queue();
    /**
     * This method is used to acquire a read lock. If there is already a writer thread
     * accessing the object using the RWLock then the reader thread will wait until
     * the writer completes its operation
     */
    public synchronized void acquireReadLock() {
        if (currentWriters == 0 && writerQueue.size() == 0) {
            ++currentReaders;
        } else {
            ++pendingReaders;
            try {
                wait();
            } catch(InterruptedException ie) {
                _logger.log(Level.FINE,"Error in acquireReadLock",ie);
            }
        }
    }

    /**
     * This method is used to acquire a write lock. If there are already reader threads
     * accessing the object using the RWLock, then the writer thread will wait till all
     * the reader threads are finished with their operations.
     */
    public void acquireWriteLock() {
        Object lock = new Object();

        synchronized(lock) {
            synchronized(this) {
                if (writerQueue.size() == 0 && currentReaders == 0 && currentWriters == 0) {
                    ++currentWriters;
                    // Use logging facility if you need to log this
                    //_logger.log(Level.FINE," RW: incremented WriterLock count");
                    return;
                }
                writerQueue.enQueue(lock);
                // Use logging facility if you need to log this
                //_logger.log(Level.FINE," RW: Added WriterLock to queue");
            }
            try {
                lock.wait();
            } catch(InterruptedException ie) {
                _logger.log(Level.FINE,"Error in acquireWriteLock",ie);
            }
        }
    }

    /**
     * isWriteLocked
     *
     * returns true if the RWLock is in a write locked state.
     *
     */
    public boolean isWriteLocked()
    {
        return currentWriters > 0 ;
    }

    /**
     * This method is used to release a read lock.
     * It also notifies any waiting writer thread
     * that it could now acquire a write lock.
     */
    public synchronized void releaseReadLock() {
        if (--currentReaders == 0) {
            notifyWriters();
        }
    }

    /**
     * This method is used to release a write lock. It also notifies any pending
     * readers that they could now acquire the read lock. If there are no reader
     * threads then it will try to notify any waiting writer thread that it could now
     * acquire a write lock.
     */
    public synchronized void releaseWriteLock() {
        --currentWriters;
        if (pendingReaders > 0) {
            notifyReaders();
        } else {
            notifyWriters();
        }
    }
    private void notifyReaders() {
        currentReaders += pendingReaders;
        pendingReaders = 0;
        notifyAll();
    }

    private void notifyWriters() {
        if (writerQueue.size() > 0) {
            Object lock = writerQueue.deQueueFirst();
            ++currentWriters;
            synchronized(lock) {
                lock.notify();
            }
        }
    }

    class Queue extends LinkedList {

        public Queue() {
            super();
        }

        public void enQueue(Object o) {
            super.addLast(o);
        }

        public Object deQueueFirst() {
            return super.removeFirst();
        }

    }
}

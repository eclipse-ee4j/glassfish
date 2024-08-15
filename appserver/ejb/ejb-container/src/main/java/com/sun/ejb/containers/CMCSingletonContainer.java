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

package com.sun.ejb.containers;

import com.sun.ejb.ComponentContext;
import com.sun.ejb.EjbInvocation;
import com.sun.ejb.InvocationInfo;
import com.sun.ejb.MethodLockInfo;
import com.sun.enterprise.security.SecurityManager;

import jakarta.ejb.ConcurrentAccessException;
import jakarta.ejb.ConcurrentAccessTimeoutException;
import jakarta.ejb.IllegalLoopbackException;
import jakarta.ejb.LockType;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/**
 * @author Mahesh Kannan
 */
public class CMCSingletonContainer
        extends AbstractSingletonContainer {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    private final static long NO_BLOCKING = 0;
    private final static long BLOCK_INDEFINITELY = -1;

    private final MethodLockInfo defaultMethodLockInfo;

    public CMCSingletonContainer(EjbDescriptor desc, ClassLoader cl, SecurityManager sm)
            throws Exception {
        super(desc, cl, sm);

        // In absence of any method lock info default is WRITE lock with no timeout.
        defaultMethodLockInfo = new MethodLockInfo();
        defaultMethodLockInfo.setLockType(LockType.WRITE);

    }

    /*
     * Findbugs complains that the lock acquired in this method is not
     *  unlocked on all paths in this method.
     *
     * Even though the method doesn't unlock the (possibly) acquired
     * lock, the lock is guaranteed to be unlocked in releaseContext()
     * even in the presence of (both checked and unchecked) exceptions.
     *
     * The general pattern used by various parts of the EJB container code is:
     *
     * try {
     *      container.preInvoke(inv);
     *      returnValue = container.intercept(inv);
     * } catch (Exception1 e1) {
     *      ...
     * } catch (Exception2 e2) {
     *      ...
     * } finally {
     *      container.postInvoke();
     * }
     *
     * Thus, it is clear that, BaseContainer.postInvoke() which in turn
     * calls releaseContext() will be called if container.preInvoke()
     * is called. This ensures that CMCSingletonContainer (this class)
     * releases the lock acquired by _getContext().
     *
     * Also, note that the above works even for loopback methods as
     * container.preInvoke() and container,postInvoke() will be called
     * before every bean method.
     *
     */
    protected ComponentContext _getContext(EjbInvocation inv) {
        checkInit();
        InvocationInfo invInfo = inv.invocationInfo;

        MethodLockInfo lockInfo = (invInfo.methodLockInfo == null)
                ? defaultMethodLockInfo : invInfo.methodLockInfo;
        Lock theLock = lockInfo.isReadLockedMethod() ? readLock : writeLock;

        if ( (rwLock.getReadHoldCount() > 0) &&
             (!rwLock.isWriteLockedByCurrentThread()) ) {
            if( lockInfo.isWriteLockedMethod() ) {
                throw new IllegalLoopbackException("Illegal Reentrant Access : Attempt to make " +
                        "a loopback call on a Write Lock method '" + invInfo.targetMethod1 +
                        "' while a Read lock is already held");
            }
        }


        /*
         * Please see comment at the beginning of the method.
         * Even though the method doesn't unlock the (possibly) acquired
         * lock, the lock is guaranteed to be unlocked in releaseContext()
         * even if exceptions were thrown in _getContext()
         */
        if (!lockInfo.hasTimeout() ||
            ( (lockInfo.hasTimeout() && (lockInfo.getTimeout() == BLOCK_INDEFINITELY) )) ) {
            theLock.lock();
        } else {
            try {
                boolean lockStatus = theLock.tryLock(lockInfo.getTimeout(), lockInfo.getTimeUnit());
                if (! lockStatus) {
                    String msg = "Couldn't acquire a lock within " + lockInfo.getTimeout() +
                            " " + lockInfo.getTimeUnit();
                    if( lockInfo.getTimeout() == NO_BLOCKING ) {
                        throw new ConcurrentAccessException(msg);
                    } else {
                        throw new ConcurrentAccessTimeoutException(msg);
                    }
                }
            } catch (InterruptedException inEx) {
                String msg = "Couldn't acquire a lock within " + lockInfo.getTimeout() +
                        " " + lockInfo.getTimeUnit();
                ConcurrentAccessException cae = (lockInfo.getTimeout() == NO_BLOCKING) ?
                        new ConcurrentAccessException(msg) : new ConcurrentAccessTimeoutException(msg);
                cae.initCause(inEx);
                throw cae;
            }
        }


        //Now that we have acquired the lock, remember it
        inv.setCMCLock(theLock);

        //Now that we have the lock return the singletonCtx
        return singletonCtx;
    }

    /**
     * This method must unlock any lock that might have been acquired
     * in _getContext().
     *
     * @see com.sun.ejb.containers.CMCSingletonContainer#_getContext(EjbInvocation inv)
     * @param inv The current EjbInvocation that was passed to _getContext()
     */
    public void releaseContext(EjbInvocation inv) {
        Lock theLock = inv.getCMCLock();
        if (theLock != null) {
            theLock.unlock();
        }
    }

}

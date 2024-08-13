/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests.concurrent;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Concurrent access to the configuarion APIs related tests
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
public class ConcurrentAccessTest {
    @Inject
    private ServiceLocator locator;
    @Inject
    private Logger logger;

    @Test
    public void waitAndSuccessTest() throws Exception {
        ConfigSupport.lockTimeOutInSeconds = 1;
        runTest(200);
    }

    @Test
    public void waitAndTimeOutTest() throws Exception {
        ConfigSupport.lockTimeOutInSeconds = 1;
        assertThrows(TransactionFailure.class, () -> runTest(1200));
    }

    private void runTest(final int waitTime) throws Exception {
        final Domain domain = locator.getService(Domain.class);

        // my lock.
        final Semaphore lock = new Semaphore(1);
        lock.acquire();

        // end of access
        final Semaphore endOfAccess = new Semaphore(1);
        endOfAccess.acquire();

        final long begin = System.currentTimeMillis();
        final SingleConfigCode<Domain> configCode = domain1 -> {
            logger.fine("got the lock at " + (System.currentTimeMillis() - begin));
            lock.release();
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.fine("release the lock at " + (System.currentTimeMillis() - begin));
            return null;
        };
        // let's start a thread to hold the lock on Domain...
        Runnable job = () -> {
            try {
                ConfigSupport.apply(configCode, domain);
            } catch (TransactionFailure e) {
                e.printStackTrace();
            } finally {
                endOfAccess.release();
            }
        };
        final Thread t = new Thread(job);
        t.start();

        // let's change the last modified date...
        lock.acquire();
        logger.fine("looking for second lock at " + (System.currentTimeMillis() - begin));

        final SingleConfigCode<Domain> releaseLockCode = domain1 -> {
            logger.fine("got the second lock at " + (System.currentTimeMillis() - begin));
            lock.release();
            return null;
        };
        try {
            ConfigSupport.apply(releaseLockCode, domain);
        } finally {
            endOfAccess.acquire();
        }
    }
}

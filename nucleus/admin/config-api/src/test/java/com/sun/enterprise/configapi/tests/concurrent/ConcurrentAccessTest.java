/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import org.junit.Test;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.concurrent.Semaphore;

/**
 * Concurrent access to the configuarion APIs related tests
 * @author Jerome Dochez
 */

public class ConcurrentAccessTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void waitAndSuccessTest() throws TransactionFailure, InterruptedException {
        ConfigSupport.lockTimeOutInSeconds=1;
        runTest(200);
    }

    @Test(expected=TransactionFailure.class)
    public void waitAndTimeOutTest() throws TransactionFailure, InterruptedException {
        ConfigSupport.lockTimeOutInSeconds=1;
        try {
            runTest(1200);
        } catch (TransactionFailure transactionFailure) {
            logger.fine("Got expected transaction failure, access timed out");
            throw transactionFailure;
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }



    private void runTest(final int waitTime) throws TransactionFailure, InterruptedException {

        final Domain domain = getHabitat().getService(Domain.class);

        // my lock.
        final Semaphore lock = new Semaphore(1);
        lock.acquire();

        // end of access
        final Semaphore endOfAccess = new Semaphore(1);
        endOfAccess.acquire();

        final long begin = System.currentTimeMillis();

        // let's start a thread to hold the lock on Domain...
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ConfigSupport.apply(new SingleConfigCode<Domain>() {
                        @Override
                        public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                            logger.fine("got the lock at " + (System.currentTimeMillis() - begin));
                            lock.release();
                            try {
                                Thread.sleep(waitTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            logger.fine("release the lock at " + (System.currentTimeMillis() - begin));
                            return null;
                        };
                    }, domain);
                } catch(TransactionFailure e) {
                    e.printStackTrace();
                }
                endOfAccess.release();
            }
        });
        t.start();


        // let's change the last modified date...
        lock.acquire();
        logger.fine("looking for second lock at " + (System.currentTimeMillis() - begin));

        try {
            ConfigSupport.apply(new SingleConfigCode<Domain>() {
                @Override
                public Object run(Domain domain) throws PropertyVetoException, TransactionFailure {
                    logger.fine("got the second lock at " + (System.currentTimeMillis() - begin));
                    lock.release();
                    return null;
                }
            }, domain);
        } finally {
            endOfAccess.acquire();
        }


    }

}

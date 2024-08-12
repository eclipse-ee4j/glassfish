/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.amx.impl.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.glassfish.admin.amx.util.jmx.JMXUtil;

/**
Blocks until an MBean is UNregistered using a CountdownLatch (highly efficient).

 */
public final class UnregistrationListener implements NotificationListener {

    final MBeanServerConnection mMBeanServer;
    final ObjectName mObjectName;
    final CountDownLatch mLatch;

    public UnregistrationListener(final MBeanServerConnection conn, final ObjectName objectName) {
        mMBeanServer = conn;
        mObjectName = objectName;
        mLatch = new CountDownLatch(1);
        // DO NOT listen here; thread-safety problem
    }

    public void handleNotification(final Notification notifIn, final Object handback) {
        if (notifIn instanceof MBeanServerNotification) {
            final MBeanServerNotification notif = (MBeanServerNotification) notifIn;

            if (notif.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION) &&
                    mObjectName.equals(notif.getMBeanName())) {
                mLatch.countDown();
            }
        }
    }

    /**
    Wait (block) until the MBean is unregistered.
    @return true if unregistered, false if an error
     */
    public boolean waitForUnregister(final long timeoutMillis) {
        boolean unregisteredOK = false;

        try {
            // could have already been unregistered
            if (mMBeanServer.isRegistered(mObjectName)) {
                try {
                    // CAUTION: we must register first to avoid a race condition
                    JMXUtil.listenToMBeanServerDelegate(mMBeanServer, this, null, mObjectName);

                    if (mMBeanServer.isRegistered(mObjectName)) {
                        // block
                        final boolean unlatched = mLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
                        unregisteredOK = unlatched; // otherwise it timed-out
                    } else {
                        unregisteredOK = true;
                    }
                } catch (final java.lang.InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (final InstanceNotFoundException e) {
                    // fine, we're expecting it to be unregistered anyway
                } finally {
                    mMBeanServer.removeNotificationListener(JMXUtil.getMBeanServerDelegateObjectName(), this);
                }
            } else {
                unregisteredOK = true;
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return unregisteredOK;
    }
}
































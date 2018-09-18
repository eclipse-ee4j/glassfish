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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/base/NotificationEmitterServiceTest.java,v 1.5 2007/05/05 05:23:53 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:53 $
*/
package org.glassfish.admin.amxtest.base;

import com.sun.appserv.management.base.NotificationEmitterService;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.jmx.NotificationBuilder;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.Notification;
import javax.management.NotificationListener;


/**
 */
public final class NotificationEmitterServiceTest
        extends AMXTestBase {
    public NotificationEmitterServiceTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(true);
    }

    public NotificationEmitterService
    getNotificationEmitterService() {
        return getDomainRoot().getDomainNotificationEmitterService();
    }

    public void
    testGet() {
        assert getNotificationEmitterService() != null;
    }


    private final static class testEmitListener
            implements NotificationListener {
        static final String TEST_TYPE = "unittests.testEmitListener";
        private Notification mNotification;
        private int mNumHeard;

        public testEmitListener() {
            mNumHeard = 0;
            mNotification = null;
        }

        public void
        handleNotification(
                final Notification notif,
                final Object handback) {
            mNotification = notif;
            ++mNumHeard;
        }

        public Notification getLast() { return mNotification; }

        public int getNumHeard() { return mNumHeard; }

        public void clear() {
            mNumHeard = 0;
            mNotification = null;
        }
    }

    private static final String TEST_SOURCE = "NotificationEmitterServiceTest";
    private static final String TEST_MESSAGE = "Message";
    private static final String TEST_KEY = "TestKey";
    private static final String TEST_VALUE = "test value";

    public void
    testEmit() {
        final NotificationEmitterService nes = getNotificationEmitterService();

        final NotificationBuilder builder =
                new NotificationBuilder(testEmitListener.TEST_TYPE, TEST_SOURCE);

        final testEmitListener listener = new testEmitListener();
        nes.addNotificationListener(listener, null, null);
        final Notification notif = builder.buildNew(TEST_MESSAGE);
        builder.putMapData(notif, TEST_KEY, TEST_VALUE);

        // call emitNotification() and verify it was emitted
        nes.emitNotification(notif);
        while (listener.getLast() == null) {
            // wait...
            mySleep(20);
        }
        final Notification retrieved = listener.getLast();
        assert (retrieved.getType().equals(notif.getType()));
        assert (Util.getAMXNotificationValue(retrieved, TEST_KEY, String.class).equals(TEST_VALUE));
        assert (retrieved.getSource().equals(TEST_SOURCE));
        assert (retrieved.getMessage().equals(TEST_MESSAGE));

        // now emit many Notifications.
        listener.clear();
        long start = now();
        final int ITER = 200;
        for (int i = 0; i < ITER; ++i) {
            final Notification temp = builder.buildNew(TEST_MESSAGE);
            builder.putMapData(notif, TEST_KEY, TEST_VALUE);
            nes.emitNotification(temp);
        }
        printElapsedIter("Emitted Notifications", start, ITER);
        start = now();
        while (listener.getNumHeard() < ITER) {
            mySleep(10);
        }
        printElapsedIter("After sending, received emitted Notifications", start, ITER);
    }
}











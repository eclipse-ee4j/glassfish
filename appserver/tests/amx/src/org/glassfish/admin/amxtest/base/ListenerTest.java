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

package org.glassfish.admin.amxtest.base;

import com.sun.appserv.management.base.AMX;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import java.util.Set;


/**
 */
public final class ListenerTest
        extends AMXTestBase {
    public ListenerTest() {
    }

    private static class DummyListener
            implements NotificationListener {
        public DummyListener() {}

        public void
        handleNotification(
                final Notification notif,
                final Object handback) {
        }
    }

    public void
    testAddRemoveNotificationListener()
            throws Exception {
        final long start = now();
        final Set<AMX> all = getAllAMX();

        final NotificationListener listener1 = new DummyListener();
        final NotificationListener listener2 = new DummyListener();

        final NotificationFilter filter = new NotificationFilterSupport();
        final Object handback = "handback";

        for (final AMX amx : all) {
            amx.getNotificationInfo();
            amx.addNotificationListener(listener1, null, null);
            amx.addNotificationListener(listener2, filter, handback);
        }

        for (final AMX amx : all) {
            amx.removeNotificationListener(listener1);
            amx.removeNotificationListener(listener2, filter, handback);
        }

        printElapsed("Added/removed NotificationListener", all.size(), start);
    }


}



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

import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.base.NotificationServiceMgr;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.helper.NotificationServiceHelper;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 */
public final class NotificationServiceTest
        extends AMXTestBase {
    public NotificationServiceTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(true);
    }

    public NotificationService
    create() {
        final NotificationServiceMgr proxy = getNotificationServiceMgr();

        return (proxy.createNotificationService("test", 512));
    }

    public void
    testCreate()
            throws Exception {
        final NotificationService proxy = create();

        removeNotificationService(proxy);
    }

    public void
    testGetFromEmpty()
            throws Exception {
        final NotificationService proxy = create();

        assert (proxy.getListeneeSet().size() == 0);
        final Object id = proxy.createBuffer(10, null);
        final Map<String, Object> result = proxy.getBufferNotifications(id, 0);
        final Notification[] notifs = (Notification[]) result.get(proxy.NOTIFICATIONS_KEY);
        assertEquals(0, notifs.length);
    }

    private void
    removeNotificationService(final NotificationService service)
            throws InstanceNotFoundException {
        getNotificationServiceMgr().removeNotificationService(service.getName());
    }


    private static final class MyListener
            implements NotificationListener {
        private final List<Notification> mReceived;
        private final CountDownLatch mLatch;

        public MyListener( final int numNeeded ) {
            mReceived = Collections.synchronizedList(new ArrayList<Notification>());
            mLatch     = new CountDownLatch(numNeeded);
        }

        public void
        handleNotification(
                final Notification notif,
                final Object handback) {
            mReceived.add(notif);
            mLatch.countDown();
        }

        public boolean await( final long amt, final TimeUnit units )
            throws InterruptedException
        {
           return mLatch.await( amt, units);
        }

        public int
        getCount() {
            return (mReceived.size());
        }
    }

    private static void
    sleep(int duration) {
        try {
            Thread.sleep(duration);
        }
        catch (InterruptedException e) {
        }
    }

    public void
    testListen()
            throws Exception {
        //trace( "testListen: START" );
        final NotificationService proxy = create();

        final QueryMgr queryMgr = getQueryMgr();
        final ObjectName objectName = Util.getObjectName(queryMgr);

        final Object id = proxy.createBuffer(10, null);
        final NotificationServiceHelper helper = new NotificationServiceHelper(proxy, id);
        proxy.listenTo(objectName, null);
        assert (proxy.getListeneeSet().size() == 1);
        assert (Util.getObjectName((Util.asAMX(proxy.getListeneeSet().iterator().next()))).equals(objectName));

        //trace( "testListen: NEWING" );
        final MyListener myListener = new MyListener(2);    // we expect two changes, see below
        proxy.addNotificationListener(myListener, null, null);
        final String saveLevel = queryMgr.getMBeanLogLevel();
        queryMgr.setMBeanLogLevel("" + Level.FINEST);
        queryMgr.setMBeanLogLevel(saveLevel);

        //trace( "testListen: WAITING" );
        // delivery may be asynchronous; wait until done
        if ( ! myListener.await( 5, TimeUnit.SECONDS ) )
        {
            //trace( "testListen: FAILED TIMEOUT" );
            assert false : "NotificationServiceTest.testListen():  TIMED OUT waiting for Notifications";
        }

        //trace( "testListen: NOT FAILED" );
        assert (myListener.getCount() == 2);

        Notification[] notifs = helper.getNotifications();

        assertEquals(2, notifs.length);
        assert (notifs[0].getType().equals(AttributeChangeNotification.ATTRIBUTE_CHANGE));
        assert (notifs[1].getType().equals(AttributeChangeNotification.ATTRIBUTE_CHANGE));
        notifs = helper.getNotifications();
        assert (notifs.length == 0);


        proxy.dontListenTo(objectName);
        assert (proxy.getListeneeSet().size() == 0);

        removeNotificationService(proxy);
        //trace( "testListen: EXIT" );
    }

}



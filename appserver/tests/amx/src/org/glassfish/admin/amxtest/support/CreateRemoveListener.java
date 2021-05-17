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

package org.glassfish.admin.amxtest.support;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.util.stringifier.SmartStringifier;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;


/**
 A NotificationListener which expects to receive a
 CONFIG_CREATED_NOTIFICATION_TYPE and CONFIG_REMOVED_NOTIFICATION_TYPE
 from an MBean with a particular j2eeType and name.
 */
public final class CreateRemoveListener
        implements NotificationListener {
    private final String mNameExpected;
    private final String mJ2EETypeExpected;
    private final Container mSource;

    private Notification mCreateNotif;
    private Notification mRemoveNotif;

    public CreateRemoveListener(
            final Container source,
            final String j2eeTypeExpected,
            final String nameExpected) {
        mSource = source;
        mNameExpected = nameExpected;
        mJ2EETypeExpected = j2eeTypeExpected;

        mSource.addNotificationListener(this, null, null);
    }

    public void
    handleNotification(
            final Notification notifIn,
            final Object handback) {
        final String type = notifIn.getType();

        //final Map<String,Serializable>    m    = getAMXNotificationData * notifIn );
        final ObjectName objectName =
                Util.getAMXNotificationValue(notifIn, AMXConfig.CONFIG_OBJECT_NAME_KEY, ObjectName.class);

        //trace( "CreateRemoveListener:\n" + SmartStringifier.toString( notifIn ) + ":\n" + objectName );

        if (Util.getJ2EEType(objectName).equals(mJ2EETypeExpected) &&
                Util.getName(objectName).equals(mNameExpected)) {
            if (type.equals(AMXConfig.CONFIG_CREATED_NOTIFICATION_TYPE)) {
                mCreateNotif = notifIn;
            } else if (type.equals(AMXConfig.CONFIG_REMOVED_NOTIFICATION_TYPE)) {
                mRemoveNotif = notifIn;
            }
        }
    }

    protected void
    trace(Object o) {
        System.out.println(SmartStringifier.toString(o));
    }

    public static void
    mySleep(final long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
        }
    }


    public void
    waitCreate() {
        long millis = 10;
        while (mCreateNotif == null) {
            mySleep(millis);
            trace("waiting " + millis + "ms for CONFIG_CREATED_NOTIFICATION_TYPE for " + mNameExpected);
            millis *= 2;
        }
    }

    public void
    waitRemove() {
        long millis = 10;
        while (mRemoveNotif == null) {
            mySleep(millis);
            trace("waiting " + millis + "ms for CONFIG_REMOVED_NOTIFICATION_TYPE for " + mNameExpected);
            millis *= 2;
        }
    }


    public void
    waitNotifs() {
        waitCreate();
        waitRemove();

        try {
            mSource.removeNotificationListener((NotificationListener) this, null, null);
        }
        catch (ListenerNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}


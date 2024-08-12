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

package org.glassfish.admin.amx.util.jmx;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;

/**
Base class for building AMX Notifications.  AMX Notifications
all place a Map in the userData field.  This class takes care
of building Notifications with correct time stamp, sequence number,
etc.  It also enforces certain conventions.
<p>
A convenience routine is provided for adding additional fields to
the Map--putMapData().
 */
public class NotificationBuilder
{
    private long mSequenceNumber = 0;

    private final String mNotificationType;

    private final Object mSource;

    protected synchronized long nextSequenceNumber()
    {
        return (mSequenceNumber++);
    }

    public NotificationBuilder(
            final String notificationType,
            final Object source)
    {
        mNotificationType = notificationType;
        mSource = source;
    }

    public final String getNotificationType()
    {
        return (mNotificationType);
    }

    public final Object getSource()
    {
        return (mSource);
    }

    protected final long now()
    {
        return (System.currentTimeMillis());
    }

    /**
    Build a new Notification with an existing Map.
     */
    public Notification buildNewWithMap(
            final String message,
            final Map<String, Serializable> userDataMap)
    {
        final Notification notif = new Notification(
                mNotificationType,
                mSource,
                nextSequenceNumber(),
                now(),
                message);

        if (userDataMap != null)
        {
            notif.setUserData(userDataMap);
        }
        else
        {
            notif.setUserData(new HashMap<String, Serializable>());
        }

        return (notif);
    }

    /**
    Build a new Notification without any values in its Map
    and no message.
     */
    public Notification buildNew()
    {
        return buildNew(mNotificationType);
    }

    /**
    Build a new Notification without any values in its Map.
    @param message
     */
    public Notification buildNew(final String message)
    {
        return buildNewWithMap(message, null);
    }

    /**
    Build a new Notification with one key/value for the Map.
    public Notification
    buildNew(
    final String            key,
    final Serializable        value )
    {
    if ( value instanceof Map )
    {
    throw new IllegalArgumentException("use buildNewWithMap" );
    }

    final Notification        notif = buildNew();

    if ( key != null )
    {
    putMapData( notif, key, value );
    }

    return( notif );
    }
     */
    /**
    Build a new Notification with one key/value for the Map.
    public Notification
    buildNew(
    final String        key,
    final Serializable  value,
    final String        message )
    {
    final Notification        notif = buildNew( message );

    if ( key != null )
    {
    putMapData( notif, key, value );
    }

    return( notif );
    }
     */
    /**
    Put a single key/value pair into the user data Map.
     */
    public static final void putMapData(
            final Notification notif,
            final String keyToInsert,
            final Serializable valueToInsert)
    {
        final Map<String, Serializable> userData =
                JMXUtil.getUserDataMapString_Serializable(notif);

        userData.put(keyToInsert, valueToInsert);
    }

    /**
    Put all key/value pairs into the user data Map.
     */
    public static final <T extends Serializable> void putAllMapData(
            final Notification notif,
            final Map<String, T> additionalUserData)
    {
        final Map<String, Serializable> userData =
                JMXUtil.getUserDataMapString_Serializable(notif);

        userData.putAll(additionalUserData);
    }

}






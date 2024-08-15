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

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;

/**
Convenience base class for listening to
{@link MBeanServerNotification} notifications.
A class extending this class must implement {@link #mbeanRegistered}
and {@link #mbeanUnregistered}.
<p>
The class is designed to start listening upon creation.
The caller should call cleanup() when listening is no longer
desired.  Once cleanup() is called, no further listening can
be done; a new MBeanRegistrationListener should be instantiated
if further listening is desired.
 */
public abstract class MBeanRegistrationListener extends NotificationListenerBase
{
    private final ObjectName mRegUnregFilter;

    private final String mDefaultDomain;

    /**
    If 'constrain' is non-null, then all registration and unregistration
    events will be filtered through it.  Only those MBeans
    matching will be passed through to {@link #mbeanRegistered}
    and {@link #mbeanUnregistered}.

    @param conn
    @param constrain     optional fixed or pattern ObjectName
     */
    protected MBeanRegistrationListener(
            final String name,
            final MBeanServerConnection conn,
            final ObjectName constrain)
            throws IOException
    {
        super(name, conn, JMXUtil.getMBeanServerDelegateObjectName());
        mRegUnregFilter = constrain;

        mDefaultDomain = conn.getDefaultDomain();
    }

    /**
    Calls this( conn, null ).
    @param conn
     */
    protected MBeanRegistrationListener(
            final String name,
            final MBeanServerConnection conn)
            throws IOException
    {
        this(name, conn, (ObjectName) null);
    }

    protected abstract void mbeanRegistered(final ObjectName objectName);

    protected abstract void mbeanUnregistered(final ObjectName objectName);

    public void handleNotification(final Notification notifIn, final Object handback)
    {
        if (!(notifIn instanceof MBeanServerNotification))
        {
            throw new IllegalArgumentException(notifIn.toString());
        }

        final MBeanServerNotification notif = (MBeanServerNotification) notifIn;
        final ObjectName objectName = notif.getMBeanName();
        final String type = notif.getType();

        final boolean matchesFilter = (mRegUnregFilter == null) ||
                                      JMXUtil.matchesPattern(mDefaultDomain, mRegUnregFilter, objectName);

        if (matchesFilter)
        {
            if (type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
            {
                mbeanRegistered(objectName);
            }
            else if (type.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
            {
                mbeanUnregistered(objectName);
            }
        }
    }

}





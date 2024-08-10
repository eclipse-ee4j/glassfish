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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.glassfish.admin.amx.util.SetUtil;

/**
Convenience base class for listening for Notifications
from one or more MBeans, which may be specified as
a specific MBean ObjectName, or an ObjectName pattern.
If the ObjectName is a pattern, the list of listenees
is dynamically maintained.
<p>
Caller should call {@link #cleanup} when done, because
a listener is maintained on the MBeanServer delegate.

 */
public abstract class NotificationListenerBase
        implements NotificationListener
{
    private final MBeanServerConnection mConn;

    /** actual MBean ObjectNames, not patterns */
    private final Set<ObjectName> mListenees;

    /** targets as specified by caller, may be a pattern or fixed ObjectName */
    private final ObjectName mPattern;

    private final NotificationFilter mFilter;

    private RegistrationListener mDelegateListener;

    private volatile boolean mSetupListening;

    /**
    Calls this( conn, listenTo, null, null ).
    <p><b>Instantiating code must call setupListening() in order to initiate
    listening</b>
     */
    protected NotificationListenerBase(
            final String name,
            final MBeanServerConnection conn,
            final ObjectName pattern)
            throws IOException
    {
        this(name, conn, pattern, null);
    }

    /**
    Listen to all MBean(s) which match the pattern 'listenTo'.
    <p><b>Instantiating code must call setupListening() in order to initiate
    listening</b>
    @param name arbitrary name of this listener
    @param conn the MBeanServerConnection or MBeanServer
    @param pattern an MBean ObjectName, or an ObjectName pattern
    @param filter optional NotificationFilter
     */
    protected NotificationListenerBase(
            final String name,
            final MBeanServerConnection conn,
            final ObjectName pattern,
            final NotificationFilter filter)
            throws IOException
    {
        mConn = conn;
        mPattern = pattern;
        mFilter = filter;
        mDelegateListener = null;
        mSetupListening = false;

        mListenees = Collections.synchronizedSet(new HashSet<ObjectName>());

        // test connection for validity
        if (!conn.isRegistered(JMXUtil.getMBeanServerDelegateObjectName()))
        {
            throw new IllegalArgumentException();
        }
    }

    /**
    Subclass should implement this routine.
     */
    @Override
    public abstract void handleNotification(final Notification notif, final Object handback);

    protected synchronized void listenToMBean(final ObjectName objectName)
            throws InstanceNotFoundException, IOException
    {
        if (!mListenees.contains(objectName))
        {
            mListenees.add(objectName);
            getMBeanServerConnection().addNotificationListener(
                    objectName, this, mFilter, null);
        }
    }

    public synchronized void startListening()
            throws InstanceNotFoundException, IOException
    {
        if (mSetupListening)
        {
            throw new IllegalStateException("setupListening() must be called exactly once");
        }

        if (mPattern.isPattern())
        {
            // it's crucial we listen for registration/unregistration events
            // so that any patterns are maintained.
            // do this BEFORE the code below, of we could
            // miss a registration.
            mDelegateListener = new RegistrationListener();
            JMXUtil.listenToMBeanServerDelegate(mConn,
                    mDelegateListener, null, null);
        }


        Set<ObjectName> s;

        if (mPattern.isPattern())
        {
            s = JMXUtil.queryNames(getConn(), mPattern, null);
        }
        else
        {
            s = SetUtil.newSet(mPattern);
        }

        for (final ObjectName objectName : s)
        {
            listenToMBean(objectName);
        }

        mSetupListening = true;
    }

    /**
    Get the filter originally specified when constructing this object.
     */
    public final NotificationFilter getNotificationFilter(final ObjectName objectName)
    {
        return mFilter;
    }

    protected synchronized void listenToIfMatch(final ObjectName objectName)
            throws IOException, InstanceNotFoundException
    {
        if (!mListenees.contains(objectName))
        {
            final String defaultDomain = getConn().getDefaultDomain();

            if (JMXUtil.matchesPattern(defaultDomain, mPattern, objectName))
            {
                listenToMBean(objectName);
            }
        }
    }

    /**
    tracks coming and going of MBeans being listened to which
    match our patterns.
     */
    private final class RegistrationListener implements NotificationListener
    {
        public RegistrationListener()
        {
        }

        @Override
        public void handleNotification(
                final Notification notifIn,
                final Object handback)
        {
            if (notifIn instanceof MBeanServerNotification)
            {
                final MBeanServerNotification notif = (MBeanServerNotification) notifIn;

                final ObjectName objectName = notif.getMBeanName();
                final String type = notif.getType();

                try
                {
                    if (type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
                    {
                        listenToIfMatch(objectName);
                    }
                    else if (type.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
                    {
                        mListenees.remove(objectName);
                    }
                }
                catch (Exception e)
                {
                    // nothing can be done...
                }
            }
        }

    }

    /**
    Reset everything so that no listening is occuring and
    all lists are empty.
     */
    public synchronized void cleanup()
    {
        try
        {
            if (mDelegateListener != null)
            {
                // it's crucial we listen for registration/unregistration events
                // so that any patterns are maintained.
                getConn().removeNotificationListener(
                        JMXUtil.getMBeanServerDelegateObjectName(),
                        mDelegateListener, null, null);
                mDelegateListener = null;
            }

            for (final ObjectName objectName : mListenees)
            {
                getConn().removeNotificationListener(
                        objectName, this, mFilter, null);
            }
        }
        catch (JMException e)
        {
        }
        catch (IOException e)
        {
        }

        mListenees.clear();
    }

    /**
    @return a copy of the MBean currently being listened to.
     */
    public synchronized Set<ObjectName> getListenees()
    {
        final Set<ObjectName> objectNames = new HashSet<ObjectName>();

        synchronized (mListenees)
        {
            objectNames.addAll(mListenees);
        }

        return (objectNames);
    }

    /**
    @return the MBeanServerConnection in use.
    @throws an Exception if no longer alive ( isAlive() returns false).
     */
    public final MBeanServerConnection getMBeanServerConnection()
    {
        return getConn();
    }

    protected final MBeanServerConnection getConn()
    {
        return mConn;
    }

    protected final void checkAlive()
            throws IOException
    {
        if (!isAlive())
        {
            throw new IOException("MBeanServerConnection failed");
        }
    }

    /**
    @return true if still listening and the connection is still alive
     */
    public boolean isAlive()
    {
        boolean isAlive = true;

        if (!(mConn instanceof MBeanServer))
        {
            // remote, check if it is alive
            try
            {
                mConn.isRegistered(JMXUtil.getMBeanServerDelegateObjectName());
            }
            catch (Exception e)
            {
                isAlive = false;
            }
        }
        return isAlive;
    }

}







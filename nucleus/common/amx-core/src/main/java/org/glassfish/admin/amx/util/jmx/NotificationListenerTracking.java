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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 */
public class NotificationListenerTracking
{
    // NotificationListeners are not unique, so we can't use a Map
    private final List<NotificationListenerInfo> mInfos;

    public NotificationListenerTracking(boolean synchronize)
    {
        final List<NotificationListenerInfo> infos =
                new ArrayList<NotificationListenerInfo>();

        mInfos = synchronize ? Collections.synchronizedList(infos) : infos;
    }

    public void addNotificationListener(
            NotificationListener listener,
            NotificationFilter filter,
            Object handback)
    {
        final NotificationListenerInfo info =
                new NotificationListenerInfo(listener, filter, handback);

        mInfos.add(info);
    }

    public int getListenerCount()
    {
        return mInfos.size();
    }

    private final boolean listenersEqual(
            final NotificationListener listener1,
            final NotificationListener listener2)
    {
        return (listener1 == listener2);
    }

    private final boolean handbacksEqual(
            final Object handback1,
            final Object handback2)
    {
        return (handback1 == handback2);
    }

    /**
    Remove <b>all instances</b> of the specified listener and return
    their corresponding NotificationListenerInfo.
    This behavior matches the behavior of
    {@link javax.management.NotificationEmitter}.

    @return list of NotificationListenerInfo
     */
    public List<NotificationListenerInfo> removeNotificationListener(final NotificationListener listener)
    {
        final Iterator iter = mInfos.iterator();

        final List<NotificationListenerInfo> results = new ArrayList<NotificationListenerInfo>();

        while (iter.hasNext())
        {
            final NotificationListenerInfo info =
                    (NotificationListenerInfo) iter.next();

            if (listenersEqual(listener, info.getListener()))
            {
                iter.remove();
                results.add(info);
            }
        }

        return (results);
    }

    /**
    Remove <b>the first instance</b> of the specified listener/filter/handback
    combination and return its corresponding NotificationListenerInfo.
    This behavior matches the behavior of
    {@link javax.management.NotificationEmitter}.

    @return list of NotificationListenerInfo
     */
    public NotificationListenerInfo removeNotificationListener(
            final NotificationListener listener,
            final NotificationFilter filter,
            final Object handback)
    {
        final Iterator iter = mInfos.iterator();
        NotificationListenerInfo result = null;

        while (iter.hasNext())
        {
            final NotificationListenerInfo info =
                    (NotificationListenerInfo) iter.next();

            if (listenersEqual(listener, info.getListener()) &&
                handbacksEqual(handback, info.getHandback()))
            {
                iter.remove();
                result = info;
                break;
            }
        }

        return (result);
    }

}

























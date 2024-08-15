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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.relation.MBeanServerNotificationFilter;

import org.glassfish.admin.amx.util.ListUtil;

/**
Features:
<ul>
<li>Maintains information on all NotificationListeners so that queries can
be made on the number of listeners, and the number of listeners of each type</li>
<li>optionally sends all Notifications asynchronously via a separate Thread</li>
</ul>
<p>
For async use, a shared sender thread is used for all Notifications.
 */
public final class NotificationEmitterSupport
        extends NotificationBroadcasterSupport
{
    private final boolean mAsyncDelivery;

    private static volatile SenderThread sSenderThread = null;

    private final Map<String, Integer> mListenerTypeCounts;

    private final NotificationListenerTracking mListeners;

    public NotificationEmitterSupport(
            final boolean asyncDelivery)
    {
        mAsyncDelivery = asyncDelivery;

        mListenerTypeCounts = Collections.synchronizedMap(new HashMap<String, Integer>());

        mListeners = new NotificationListenerTracking(true);
    }

    public void cleanup()
    {
        // NO-OP with the shared SenderThread
        /*
        if ( mSenderThread != null )
        {
        mSenderThread.quit();
        mSenderThread        = null;
        }
         */
    }

    /**
    Synchronously (on current thread), ensure that all Notifications
    have been delivered.
     */
    public void sendAll()
    {
        // they will all be sent as fast as the sender thread can go, but the caller wants
        // them all sent before return

        if (sSenderThread != null)
        {
            sSenderThread.waitSentAll(this);
        }
    }

    public int getListenerCount()
    {
        return (mListeners.getListenerCount());
    }

    public int getNotificationTypeListenerCount(final String type)
    {
        final Integer count = mListenerTypeCounts.get(type);

        int resultCount = 0;

        if (count == null)
        {
            final Integer allCount = mListenerTypeCounts.get(WILDCARD_TYPE);
            if (allCount != null)
            {
                resultCount = allCount;
            }
            else
            {
                // no wildcards are in use
            }
        }

        return (resultCount);
    }

    private static final String WILDCARD_TYPE = "***";

    private static final String[] ALL_TYPES = new String[]
    {
        WILDCARD_TYPE
    };

    private static final String[] ATTRIBUTE_CHANGE_TYPES = new String[]
    {
        AttributeChangeNotification.ATTRIBUTE_CHANGE
    };

    private static final String[] MBEAN_SERVER_NOTIFICATION_TYPES = new String[]
    {
        MBeanServerNotification.REGISTRATION_NOTIFICATION,
        MBeanServerNotification.UNREGISTRATION_NOTIFICATION,
    };

    private final Integer COUNT_1 = Integer.valueOf(1);

    private void incrementListenerCountForType(final String type)
    {
        synchronized (mListenerTypeCounts)
        {
            final Integer count = mListenerTypeCounts.get(type);

            final Integer newCount = (count == null) ? COUNT_1 : Integer.valueOf(count.intValue() + 1);

            mListenerTypeCounts.put(type, newCount);
        }
    }

    private void decrementListenerCountForType(final String type)
    {
        synchronized (mListenerTypeCounts)
        {
            final Integer count = mListenerTypeCounts.get(type);
            if (count == null)
            {
                throw new IllegalArgumentException(type);
            }

            final int oldValue = count.intValue();
            if (oldValue == 1)
            {
                mListenerTypeCounts.remove(type);
            }
            else
            {
                mListenerTypeCounts.put(type, Integer.valueOf(oldValue - 1));
            }
        }
    }

    private String[] getTypes(
            final NotificationFilter filter)
    {
        String[] types;

        if (filter instanceof NotificationFilterSupport)
        {
            final NotificationFilterSupport fs = (NotificationFilterSupport) filter;

            types = ListUtil.toStringArray(fs.getEnabledTypes());
        }
        else if (filter instanceof AttributeChangeNotificationFilter)
        {
            types = ATTRIBUTE_CHANGE_TYPES;
        }
        else if (filter instanceof MBeanServerNotificationFilter)
        {
            types = MBEAN_SERVER_NOTIFICATION_TYPES;
        }
        else
        {
            // no filter, or non-standard one, have to assume all types
            types = ALL_TYPES;
        }

        return types;
    }

    private void addFilterTypeCounts(final NotificationFilter filter)
    {
        String[] types = getTypes(filter);

        for (String type : types)
        {
            incrementListenerCountForType(type);
        }
    }

    private void removeFilterTypeCounts(final NotificationFilter filter)
    {
        final String[] types = getTypes(filter);

        for (String type : types)
        {
            decrementListenerCountForType(type);
        }
    }

    private void removeFilterTypeCounts(final List<NotificationListenerInfo> infos)
    {
        for (NotificationListenerInfo info : infos)
        {
            removeFilterTypeCounts(info.getFilter());
        }
    }

    @Override
    public void addNotificationListener(
            final NotificationListener listener,
            final NotificationFilter filter,
            final Object handback)
    {
        super.addNotificationListener(listener, filter, handback);

        mListeners.addNotificationListener(listener, filter, handback);
        addFilterTypeCounts(filter);
    }

    @Override
    public void removeNotificationListener(final NotificationListener listener)
            throws ListenerNotFoundException
    {
        super.removeNotificationListener(listener);

        final List<NotificationListenerInfo> infos =
                mListeners.removeNotificationListener(listener);
        removeFilterTypeCounts(infos);
    }

    @Override
    public void removeNotificationListener(
            final NotificationListener listener,
            final NotificationFilter filter,
            final Object handback)
            throws ListenerNotFoundException
    {
        super.removeNotificationListener(listener, filter, handback);

        mListeners.removeNotificationListener(listener);
        if (filter != null)
        {
            removeFilterTypeCounts(filter);
        }

    }

    protected void internalSendNotification(final Notification notif)
    {
        super.sendNotification(notif);
    }

    /**
    Send the Notification.  If created with async=true,
    then this routine returns immediately and the Notification is sent
    on a separate Thread.
     */
    @Override
    public synchronized void sendNotification(final Notification notif)
    {
        if (getListenerCount() != 0)
        {
            final SenderThread senderThread = getSenderThread(mAsyncDelivery);
            if (senderThread != null)
            {
                senderThread.enqueue(notif, this);
            }
            else
            {
                internalSendNotification(notif);
            }
        }
    }

    private static final Object senderThreadLock = new Object();

    private static SenderThread getSenderThread(final boolean asyncDelivery)
    {
        // sSenderThread MUST be 'volatile' for this to be thread-safe
        if (sSenderThread != null)
        {
            return sSenderThread;
        }

        synchronized (senderThreadLock)
        {
            if (sSenderThread == null)
            {
                sSenderThread = asyncDelivery ? new SenderThread() : null;
                if (sSenderThread != null)
                {
                    sSenderThread.start();
                }
            }
        }

        return (sSenderThread);
    }

    private static final class SenderThread extends Thread
    {
        private volatile boolean mQuit;

        private final LinkedBlockingQueue<QueueItem> mPendingNotifications;

        private static final class QueueItem
        {
            private final Notification mNotif;

            private final NotificationEmitterSupport mSender;

            public QueueItem(final Notification notif, final NotificationEmitterSupport sender)
            {
                mNotif = notif;
                mSender = sender;
            }

        }

        public SenderThread()
        {
            setDaemon(true);
            mQuit = false;
            mPendingNotifications = new LinkedBlockingQueue<QueueItem>();
        }

        public void quit()
        {
            mQuit = true;
            this.interrupt();
        }

        private void enqueue(
                final Notification notif,
                final NotificationEmitterSupport sender)
        {
            mPendingNotifications.add(new QueueItem(notif, sender));
        }

        /**
        A fake Notification to used for waitSentAll()
         */
        static final class CountDownLatchNofication extends Notification
        {
            private static final long serialVersionUID = 0xDEADBEEF; // never serialized

            final transient CountDownLatch mLatch = new CountDownLatch(1);

            CountDownLatchNofication(final Object source)
            {
                super("CountDownLatchNofication", source, 0);
            }

        }

        /**
        Ensure that all existing items have been sent.
        Check the queue for empty does not work; we need to see that thread has
        not only emptied the queue, but grabbed the next item.
         */
        public void waitSentAll(final NotificationEmitterSupport sender)
        {
            final CountDownLatchNofication notif = new CountDownLatchNofication(this);
            enqueue(notif, sender);
            try
            {
                notif.mLatch.await();
            }
            catch (InterruptedException e)
            {
                Logger.getAnonymousLogger().log(Level.WARNING,  "Interrupted: ", e);
            }
        }

        @Override
        public void run()
        {
            while (!mQuit)
            {
                try
                {
                    final QueueItem item = mPendingNotifications.take();
                    final Notification notif = item.mNotif;

                    if (notif instanceof CountDownLatchNofication)
                    {
                        // internal mechanism, see waitEmpty()
                        ((CountDownLatchNofication) notif).mLatch.countDown();
                    }
                    else
                    {
                        item.mSender.internalSendNotification(notif);
                    }
                }
                catch (InterruptedException e)
                {
                    // what does this mean if interrupted other than by quit() ?
                    // Presumably we're supposed to stop working
                    mQuit = true;
                }
            }
        }

    }
}









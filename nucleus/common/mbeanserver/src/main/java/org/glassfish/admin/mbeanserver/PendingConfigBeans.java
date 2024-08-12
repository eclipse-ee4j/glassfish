/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.glassfish.config.support.ConfigBeanListener;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
Called when ConfigBeans come into the habitat (see GlassfishConfigBean); a job queue
is maintained for processing by the AMXConfigLoader, which is lazily loaded.
 * @author llc
 */
@Service(name = "PendingConfigBeans")
public class PendingConfigBeans implements ConfigBeanListener, PostConstruct, TransactionListener
{
    @Inject
    Transactions transactions;

    private final LinkedBlockingQueue<PendingConfigBeanJob> mJobs = new LinkedBlockingQueue<PendingConfigBeanJob>();

    public int size()
    {
        return mJobs.size();
    }

    /**
    /**
    Singleton: there should be only one instance and hence a private constructor.
    But the framework using this wants to instantiate things with a public constructor.
     */
    public PendingConfigBeans()
    {
    }

    @Override
    public void postConstruct()
    {
        transactions.addTransactionsListener(this);
    }


    public PendingConfigBeanJob take() throws InterruptedException
    {
        return mJobs.take();
    }

    public PendingConfigBeanJob peek() throws InterruptedException
    {
        return mJobs.peek();
    }

    /**
    @return a ConfigBean, or null if it's not a ConfigBean
     */
    final ConfigBean asConfigBean(final Object o)
    {
        return (o instanceof ConfigBean) ? ConfigBean.class.cast(o) : null;
    }

    @Override
    public void onEntered(final ActiveDescriptor<ConfigBean> provider)
    {
        //debug( "PendingConfigBeansNew.onEntered(): " + inhabitant);

        final ConfigBean cb = asConfigBean(provider);
        if (cb != null)
        {
            add(cb);
        }
    }

    private PendingConfigBeanJob addJob(final PendingConfigBeanJob job)
    {
        if (job == null)
        {
            throw new IllegalArgumentException();
        }

        mJobs.add(job);
        return job;
    }

    private PendingConfigBeanJob add(final ConfigBean cb)
    {
        return add(cb, null);
    }

    public PendingConfigBeanJob add(final ConfigBean cb, final boolean useLatch)
    {
        final PendingConfigBeanJob job = add(cb, useLatch ? new CountDownLatch(1) : null);

        return job;
    }

    private PendingConfigBeanJob add(final ConfigBean cb, final CountDownLatch latch)
    {
        //debug( "PendingConfigBeans.add():  " + cb.getProxyType().getName() );

        // determine if the ConfigBean is a child of Domain by getting its most distant ancestor
        ConfigBean ancestor = cb;
        ConfigBean parent;
        while ( (parent = asConfigBean(ancestor.parent())) != null )
        {
            ancestor = parent;
        }
        //debug( "PendingConfigBeansNew.onEntered: " + cb.getProxyType().getName() + " with parent " + (parent == null ? "null" : parent.getProxyType().getName()) );

        PendingConfigBeanJob job = null;

        // ignore bogus ConfigBeans that are not part of the Domain
        if (ancestor.getProxyType().getName().endsWith(".Domain"))
        {
            job = addJob(new PendingConfigBeanJob(cb, latch));
        }
        else
        {
            Util.getLogger().log(Level.FINEST, "PendingConfigBeans.onEntered: ignoring ConfigBean that does not have Domain as ancestor: {0}",
                    cb.getProxyType().getName());
            if ( latch != null )
            {
                latch.countDown();
                // job remains null
            }
        }

        return job;
    }

    /**
    Removing a ConfigBean must ensure that all its children are also removed.  This will normally
    happen if AMX is loaded as a side effect of unregistering MBeans, but if AMX has not loaded
    we must ensure it directly.
    This is all caused by an HK2 asymmetry that does not issue REMOVE events for children of removed
    elements.
    <p>
    TODO: remove all children of the ConfigBean.
     */
    public boolean remove(final ConfigBean cb)
    {
        //debug( "PendingConfigBeans.remove(): REMOVE: " + cb.getProxyType().getName() );
        boolean found = false;

        for (final PendingConfigBeanJob job : mJobs)
        {
            if (job.getConfigBean() == cb)
            {
                found = true;
                job.releaseLatch();
                mJobs.remove(job);
                break;
            }
        }

        if (found)
        {
            removeAllDescendants(cb);
        }
        return found;
    }

    /**
    Remove all jobs that have this ConfigBean as an ancestor.
     */
    private void removeAllDescendants(final ConfigBean cb)
    {
        final List<PendingConfigBeanJob> jobs = new ArrayList<PendingConfigBeanJob>(mJobs);

        for (final PendingConfigBeanJob job : jobs)
        {
            if (isDescendent(job.getConfigBean(), cb))
            {
                //debug( "removed descendent: " + job.getConfigBean().getProxyType().getName() );
                mJobs.remove(job);
            }
        }
    }

    /** return true if the candidate is a descendent of the parent */
    private boolean isDescendent(final ConfigBean candidate, final ConfigBean parent)
    {
        boolean isParent = false;
        Dom temp = candidate.parent();
        while (temp != null)
        {
            if (temp == parent)
            {
                isParent = true;
                break;
            }
            temp = temp.parent();
        }

        return isParent;
    }

    /**
    amx-impl has its own TransactionListener which takes over once AMX is loaded.
    Note that it is synchronized with transactionCommited() [sic] to avoid a race condition.
     */
    public synchronized void swapTransactionListener(final TransactionListener newListener)
    {
        //debug( "PendingConfigBeans.swapTransactionListener()" );
        transactions.addTransactionsListener(newListener);
        transactions.removeTransactionsListener(this);
    }

    /**
    This is a workaround for the fact that the onEntered() is not being called in all cases,
    namely during deployment before AMX has loaded.  See disableTransactionListener() above.
     */
    @Override
    public synchronized void transactionCommited(final List<PropertyChangeEvent> events)
    {
        // could there be an add/remove/add/remove of the same thing?  Maintain the order just in case
        for (final PropertyChangeEvent event : events)
        {
            final Object oldValue = event.getOldValue();
            final Object newValue = event.getNewValue();

            if (oldValue == null && newValue instanceof ConfigBeanProxy)
            {
                // ADD: a new ConfigBean was added
                final ConfigBean cb = asConfigBean(ConfigBean.unwrap((ConfigBeanProxy) newValue));
                add(cb);
            }
            else if (newValue == null && (oldValue instanceof ConfigBeanProxy))
            {
                // REMOVE
                final ConfigBean cb = asConfigBean(ConfigBean.unwrap((ConfigBeanProxy) oldValue));
                remove(cb);
            }
        }
    }

    @Override
    public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes)
    {
        // ignore
    }

}























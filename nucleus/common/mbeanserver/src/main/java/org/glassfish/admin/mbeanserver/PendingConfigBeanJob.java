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

import java.util.concurrent.CountDownLatch;

import org.jvnet.hk2.config.ConfigBean;

/** used internally for the queue {@link PendindingConfigBeans} */
public final class PendingConfigBeanJob
{
    private final ConfigBean mConfigBean;

    private final CountDownLatch mLatch;

    public PendingConfigBeanJob(final ConfigBean cb, final CountDownLatch latch)
    {
        if (cb == null)
        {
            throw new IllegalArgumentException();
        }

        mConfigBean = cb;
        mLatch = latch;
    }

    public PendingConfigBeanJob(final ConfigBean cb, final boolean latch)
    {
        this(cb, latch ? new CountDownLatch(1) : null);
    }

    public PendingConfigBeanJob(final ConfigBean cb)
    {
        this(cb, null);
    }

    public ConfigBean getConfigBean()
    {
        return mConfigBean;
    }

    public CountDownLatch getCountDownLatch()
    {
        return mLatch;
    }

    public void releaseLatch()
    {
        if (mLatch != null)
        {
            mLatch.countDown();
        }
    }

    public void await() throws InterruptedException
    {
        if (mLatch != null)
        {
            mLatch.await();
        }
    }

}



















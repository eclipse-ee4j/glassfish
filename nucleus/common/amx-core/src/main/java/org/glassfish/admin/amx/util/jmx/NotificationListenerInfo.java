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

import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
A immutable 3-tuple for tracking this stuff
 */
public class NotificationListenerInfo
{
    private final NotificationListener mListener;

    private final NotificationFilter mFilter;

    private final Object mHandback;

    public NotificationListenerInfo(
            NotificationListener listener,
            NotificationFilter filter,
            Object handback)
    {
        mListener = listener;
        mFilter = filter;
        mHandback = handback;
    }

    public NotificationListener getListener()
    {
        return mListener;
    }

    public NotificationFilter getFilter()
    {
        return mFilter;
    }

    public Object getHandback()
    {
        return mHandback;
    }

}

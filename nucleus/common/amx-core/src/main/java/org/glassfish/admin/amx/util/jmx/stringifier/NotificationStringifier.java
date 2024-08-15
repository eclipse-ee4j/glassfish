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

package org.glassfish.admin.amx.util.jmx.stringifier;

import java.util.Date;

import javax.management.MBeanServerNotification;
import javax.management.Notification;

import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class NotificationStringifier implements Stringifier
{
    public static final NotificationStringifier DEFAULT = new NotificationStringifier();

    protected Options mOptions;

    public final static class Options
    {
        // don't make 'final' fields; allow changes after instantiation
        public boolean mIncludeObjectName;

        public boolean mIncludeTimeStamp;

        public boolean mIncludeType;

        public boolean mIncludeSequenceNumber;

        public boolean mIncludeUserData;

        public String mDelim;

        public Options()
        {
            mIncludeObjectName = true;
            mIncludeTimeStamp = true;
            mIncludeType = true;
            mIncludeSequenceNumber = true;
            mIncludeUserData = false;
            mDelim = ", ";
        }

    }

    public NotificationStringifier()
    {
        mOptions = new Options();
    }

    public NotificationStringifier(Options options)
    {
        mOptions = options;
    }

    protected void append(StringBuffer b, Object o)
    {
        if (b.length() != 0)
        {
            b.append(mOptions.mDelim);
        }

        b.append(SmartStringifier.toString(o));
    }

    public String stringify(Object o)
    {
        final Notification notif = (Notification) o;

        return (_stringify(notif).toString());
    }

    public static String toString(Object o)
    {
        return (DEFAULT.stringify(o));
    }

    protected StringBuffer _stringify(Notification notif)
    {
        final StringBuffer b = new StringBuffer();

        if (mOptions.mIncludeSequenceNumber)
        {
            append(b, "#" + notif.getSequenceNumber());
        }

        if (mOptions.mIncludeTimeStamp)
        {
            append(b, new Date(notif.getTimeStamp()));
        }

        if (mOptions.mIncludeObjectName)
        {
            append(b, StringUtil.quote(notif.getSource()));
        }

        if (mOptions.mIncludeType)
        {
            append(b, notif.getType());
        }

        if (mOptions.mIncludeUserData)
        {
            append(b, StringUtil.quote(notif.getUserData()));
        }

        if (notif instanceof MBeanServerNotification)
        {
            // this should really be done in a MBeanServerNotificationStringifier!
            final MBeanServerNotification n = (MBeanServerNotification) notif;

            append(b, StringUtil.quote(n.getMBeanName()));
        }

        return (b);
    }

}




















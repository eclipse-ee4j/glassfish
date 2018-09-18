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

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.ObjectName;

/**
Base class for building AMX Notifications.
 */
public class AttributeChangeNotificationBuilder extends NotificationBuilder
{
    public AttributeChangeNotificationBuilder(
            final ObjectName source)
    {
        super(AttributeChangeNotification.ATTRIBUTE_CHANGE, source);
    }

    public final Notification buildNew()
    {
        throw new IllegalArgumentException();
    }

    public final Notification buildNew(
            final String key,
            final Object value)
    {
        throw new IllegalArgumentException();
    }

    public final AttributeChangeNotification buildAttributeChange(
            final String msg,
            final String attributeName,
            final String attributeType,
            final Object oldValue,
            final Object newValue)
    {
        return buildAttributeChange(msg, attributeName, attributeType, now(), oldValue, newValue);
    }

    public final AttributeChangeNotification buildAttributeChange(
            final String msg,
            final String attributeName,
            final String attributeType,
            final long when,
            final Object oldValue,
            final Object newValue)
    {
        final AttributeChangeNotification notif = new AttributeChangeNotification(
                getSource(),
                nextSequenceNumber(),
                when,
                msg,
                attributeName,
                attributeType,
                oldValue,
                newValue);

        return (notif);
    }

}






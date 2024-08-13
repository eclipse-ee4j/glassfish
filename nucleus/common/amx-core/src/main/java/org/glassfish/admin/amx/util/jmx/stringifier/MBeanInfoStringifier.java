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

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;

public class MBeanInfoStringifier extends MBeanFeatureInfoStringifier implements Stringifier
{
    public static final MBeanInfoStringifier DEFAULT = new MBeanInfoStringifier();

    public MBeanInfoStringifier()
    {
        super();
    }

    public MBeanInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        super(options);
    }

    private String stringifyArray(Object[] a, Stringifier stringifier)
    {
        String temp = "";

        if (a.length != 0)
        {
            temp = "\n" + ArrayStringifier.stringify(a, "\n", stringifier);
        }
        return (temp);
    }

    // subclass may override
    MBeanAttributeInfoStringifier getMBeanAttributeInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        return (new MBeanAttributeInfoStringifier(options));
    }

    // subclass may override
    MBeanOperationInfoStringifier getMBeanOperationInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        return (new MBeanOperationInfoStringifier(options));
    }

    // subclass may override
    MBeanConstructorInfoStringifier getMBeanConstructorInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        return (new MBeanConstructorInfoStringifier(options));
    }

    // subclass may override
    MBeanNotificationInfoStringifier getMBeanNotificationInfoStringifier(MBeanFeatureInfoStringifierOptions options)
    {
        return (new MBeanNotificationInfoStringifier(options));
    }

    public String stringify(Object o)
    {
        String result = "";
        final MBeanInfo info = (MBeanInfo) o;

        final MBeanOperationInfo[] operations = info.getOperations();
        final MBeanAttributeInfo[] attributes = info.getAttributes();
        final MBeanConstructorInfo[] constructors = info.getConstructors();
        final MBeanNotificationInfo[] notifications = info.getNotifications();
        final String description = info.getDescription();

        result = "Summary: " +
                 operations.length + " operations, " +
                 attributes.length + " attributes, " +
                 constructors.length + " constructors, " +
                 notifications.length + " notifications" +
                 (description == null ? "" : ", \"" + description + "\"");

        final MBeanFeatureInfoStringifierOptions options =
                new MBeanFeatureInfoStringifierOptions(true, ",");

        // Do formal terms like "Attributes" need to be I18n?
        // Probabably not as they are part of a specification.
        result = result + "\n\n- Attributes -" +
                 stringifyArray(attributes, getMBeanAttributeInfoStringifier(options));

        result = result + "\n\n- Operations -" +
                 stringifyArray(operations, getMBeanOperationInfoStringifier(options));

        result = result + "\n\n- Constructors -" +
                 stringifyArray(constructors, getMBeanConstructorInfoStringifier(options));

        result = result + "\n\n- Notifications -" +
                 stringifyArray(notifications, getMBeanNotificationInfoStringifier(options));

        return (result);

    }

}






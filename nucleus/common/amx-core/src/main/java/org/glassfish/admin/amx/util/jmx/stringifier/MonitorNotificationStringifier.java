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

import javax.management.monitor.MonitorNotification;

public class MonitorNotificationStringifier extends NotificationStringifier
{
    public static final MonitorNotificationStringifier DEFAULT =
            new MonitorNotificationStringifier();

    public MonitorNotificationStringifier()
    {
        super();
    }

    public MonitorNotificationStringifier(Options options)
    {
        super(options);
    }

    @Override
    public String stringify(Object o)
    {
        final MonitorNotification notif = (MonitorNotification) o;

        final StringBuffer b = super._stringify(notif);
        append(b, "");

        append(b, "Observed object: " + notif.getObservedObject());
        append(b, "Observed attribute: " + notif.getObservedAttribute());
        append(b, "Trigger: " + notif.getTrigger());
        append(b, "Gauge: " + notif.getDerivedGauge());

        return (b.toString());
    }

}




















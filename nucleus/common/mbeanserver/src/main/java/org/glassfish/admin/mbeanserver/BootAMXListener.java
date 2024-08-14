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

package org.glassfish.admin.mbeanserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnectorServer;

import org.glassfish.external.amx.BootAMXMBean;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Listens for a connection on the connector server, and when made,
 * ensures that AMX has been started.
 */
class BootAMXListener implements NotificationListener
{
    private JMXConnectorServer mServer = null;
    private final BootAMXMBean mBooter;

    private static final Logger LOGGER = Util.JMX_LOGGER;

    @LogMessageInfo(message = "Booting AMX Listener, connection made for {0}, now booting AMX MBeans",
            level="INFO")
    private static final String JMX_BOOTING_AMX_LISTENER="NCLS-JMX-00008";

    public BootAMXListener(final BootAMXMBean booter)
    {
        mBooter = booter;
    }

    void setServer(JMXConnectorServer server) {
        mServer = server;
    }

    @Override
    public void handleNotification(final Notification notif, final Object handback)
    {
        if (notif instanceof JMXConnectionNotification)
        {
            final JMXConnectionNotification n = (JMXConnectionNotification) notif;
            if (n.getType().equals(JMXConnectionNotification.OPENED))
            {
                LOGGER.log(Level.INFO,JMX_BOOTING_AMX_LISTENER,handback);
                mBooter.bootAMX();

                // job is done, stop listening
                if (mServer != null) {
                    try
                    {
                        mServer.removeNotificationListener(this);
                        LOGGER.fine("ConnectorStartupService.BootAMXListener: AMX is booted, stopped listening");
                    }
                    catch (final ListenerNotFoundException e)
                    {
                        // should be impossible.
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}










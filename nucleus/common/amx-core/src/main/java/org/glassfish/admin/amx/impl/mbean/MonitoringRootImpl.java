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

package org.glassfish.admin.amx.impl.mbean;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.monitoring.MonitoringRoot;
import org.glassfish.admin.amx.monitoring.ServerMon;
import org.glassfish.admin.amx.util.AMXLoggerInfo;

public class MonitoringRootImpl extends AMXImplBase // implements MonitoringRoot
{
    private final Logger mLogger = AMXLoggerInfo.getLogger();

    public MonitoringRootImpl(final ObjectName parent) {
        super(parent, MonitoringRoot.class);
    }

    @Override
    protected final void registerChildren() {
        super.registerChildren();

        final ObjectName self = getObjectName();
        final MBeanServer server = getMBeanServer();
        final ObjectNameBuilder objectNames = new ObjectNameBuilder(server, self);

        ObjectName childObjectName = null;
        Object mbean = null;
        mLogger.log(Level.INFO, AMXLoggerInfo.registerChild, System.getProperty("com.sun.aas.instanceName"));
        // when clustering comes along, some other party will need to register MBeans
        // for each non-DAS instance
        // childObjectName = objectNames.buildChildObjectName(ServerMon.class, AMXGlassfish.DEFAULT.dasName());
        childObjectName = objectNames.buildChildObjectName(ServerMon.class, System.getProperty("com.sun.aas.instanceName"));
        mbean = new ServerMonitoringImpl(self);
        registerChild(mbean, childObjectName);
    }

    public ObjectName[] getServerMon() {
        return getChildren(ServerMon.class);
    }
}












































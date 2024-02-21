/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.amx.impl.util;

import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;

public final class ImplUtil {

    /**
    Unload this AMX MBean and all its children.
    MBean should be unloaded at the leafs first, working back to DomainRoot so as to
    not violate the rule that a Container must always be present for a Containee.
     */
    public static void unregisterAMXMBeans(final AMXProxy top) {
        if (top == null) {
            throw new IllegalArgumentException();
        }

        //debug( "ImplUtil.unregisterOneMBean: unregistering hierarchy under: " + top.objectName() );

        final MBeanServer mbeanServer = (MBeanServer) top.extra().mbeanServerConnection();

        final Set<AMXProxy> children = top.extra().childrenSet();
        if (children != null) {
            // unregister all Containees first
            for (final AMXProxy amx : children) {
                unregisterAMXMBeans(amx);
            }
        }

        unregisterOneMBean(mbeanServer, top.objectName());
    }

    /** see javadoc for unregisterAMXMBeans(AMX) */
    public static void unregisterAMXMBeans(final MBeanServer mbs, final ObjectName objectName) {
        unregisterAMXMBeans(ProxyFactory.getInstance(mbs).getProxy(objectName, AMXProxy.class));
    }

    /**
    Unregister a single MBean, returning true if it was unregistered, false otherwise.
     */
    public static boolean unregisterOneMBean(final MBeanServer mbeanServer, final ObjectName objectName) {
        boolean success = false;
        //getLogger().fine( "UNREGISTER MBEAN: " + objectName );
        //debug( "ImplUtil.unregisterOneMBean: unregistering: " + objectName );
        try {
            mbeanServer.unregisterMBean(objectName);
        } catch (final Exception e) {
            // ignore
            success = false;
        }
        return success;
    }
}
































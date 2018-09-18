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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/client/ProxyFactoryTest.java,v 1.5 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.client;

import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.base.NotificationServiceMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ProxyFactory;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;


/**
 */
public final class ProxyFactoryTest
        extends AMXTestBase {
    public ProxyFactoryTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }


    /**
     Verify that when an MBean is removed, the ProxyFactory
     detects this, and removes any proxy from its cache.
     */
    public void
    testProxyFactoryDetectsMBeanRemoved()
            throws InstanceNotFoundException {
        // use the NotificationServiceMgr as a convenient way of making
        // an MBean (a NotificationService) come and go.
        final NotificationServiceMgr mgr = getDomainRoot().getNotificationServiceMgr();
        final NotificationService ns = mgr.createNotificationService("UserData", 10);
        final ObjectName nsObjectName = Util.getObjectName(ns);
        assert (ns.getUserData().equals("UserData"));

        final ProxyFactory factory = getProxyFactory();
        final NotificationService proxy =
                factory.getProxy(nsObjectName, NotificationService.class, false);
        assert (proxy == ns) : "proxies differ: " + ns + "\n" + proxy;

        mgr.removeNotificationService(ns.getName());

        int iterations = 0;
        long sleepMillis = 10;
        while (factory.getProxy(nsObjectName, NotificationService.class, false) != null) {
            mySleep(sleepMillis);
            if (sleepMillis >= 400) {
                trace("testProxyFactoryDetectsMBeanRemoved: waiting for proxy to be removed");
            }
            sleepMillis *= 2;
        }
    }
}










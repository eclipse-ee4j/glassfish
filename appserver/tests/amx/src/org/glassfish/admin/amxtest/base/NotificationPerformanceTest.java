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

package org.glassfish.admin.amxtest.base;

import com.sun.appserv.management.base.Util;
import org.glassfish.admin.amx.mbean.TestDummy;
import org.glassfish.admin.amx.mbean.TestDummyMBean;
import org.glassfish.admin.amxtest.JMXTestBase;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;

import org.glassfish.admin.amxtest.JMXTestBase;

/**
 */
public final class NotificationPerformanceTest
        extends JMXTestBase {
    // built-into server already
    private static final String IMPL_CLASSNAME = TestDummy.class.getName();

    public NotificationPerformanceTest() {
    }

    private ObjectName
    createTestDummy(final String name)
            throws JMException, IOException {
        ObjectName objectName =
                Util.newObjectName("NotificationPerformanceTest:name=" + name);

        final MBeanServerConnection conn = getMBeanServerConnection();

        if (!conn.isRegistered(objectName)) {
            objectName =
                    conn.createMBean(IMPL_CLASSNAME, objectName).getObjectName();
        }

        return objectName;
    }

    public void
    testNotificationPerformance()
            throws JMException, IOException {
        final ObjectName objectName = createTestDummy("testNotificationPerformance");

        final TestDummyMBean test = newProxy(objectName, TestDummyMBean.class);

        final int ITER = 10;
        final int COUNT = 1024 * 1024;

        for (int iter = 0; iter < ITER; ++iter) {
            final long elapsed =
                    test.emitNotifications("NotificationPerformanceTest.test", COUNT);

            final float rate = (elapsed == 0) ? (float) 0.0 : (1000 * ((float) COUNT / (float) elapsed));
            final String rateString = (elapsed == 0) ? "N/A" : "" + (int) rate;

            System.out.println("Millis to emit " + COUNT + " Notifications: " + elapsed +
                    " = " + rateString + " notifications/sec");
        }
    }
}



























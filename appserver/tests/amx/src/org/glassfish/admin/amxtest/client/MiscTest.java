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

package org.glassfish.admin.amxtest.client;

import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.base.NotificationServiceMgr;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import java.io.IOException;


/**
 */
public final class MiscTest
        extends AMXTestBase {
    public MiscTest()
            throws IOException {
    }

    public void
    testMBeanInfo() {
        final MBeanInfo info = new MBeanInfo(
                "foo.bar",
                null,
                null,
                null,
                null,
                null);

        assert (info.getNotifications() != null);
        assert (info.getOperations() != null);
        assert (info.getAttributes() != null);
        assert (info.getConstructors() != null);
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(true);
    }

    /**
     Hangs were occuring in getPropertyNames().  Repeatedly invoke it to see if the hang
     can be reproduced.
     public void
     testGetPropertyNames()
     throws ClassNotFoundException
     {
     final Set    s    = getQueryMgr().queryInterfaceSet( PropertiesAccess.class.getName(), null );

     for( int i = 0; i < 5000; ++i )
     {
     final Iterator iter    = s.iterator();
     while ( iter.hasNext() )
     {
     final PropertiesAccess    pa    = (PropertiesAccess)iter.next();

     pa.getPropertyNames();
     }
     }
     }
     */


    /**
     Verify that when an MBean is removed, the proxy
     throws an InstanceNotFoundException.  This test is included here because
     it otherwise causes problems when running other unit tests that want to operate
     on all MBeans--this test creates and removes one, which causes the other
     tests to fail.
     */
    public void
    testProxyDetectsMBeanRemoved()
            throws InstanceNotFoundException {
        // use the NotificationServiceMgr as a convenient way of making
        // an MBean (a NotificationService) come and go.
        final NotificationServiceMgr mgr = getDomainRoot().getNotificationServiceMgr();

        final NotificationService ns = mgr.createNotificationService("UserData", 10);
        assert (ns.getUserData().equals("UserData"));
        final ObjectName nsObjectName = Util.getObjectName(ns);

        mgr.removeNotificationService(ns.getName());
        try {
            // all calls should fail
            Util.getObjectName(ns);
            ns.getName();
            ns.getUserData();
            failure("expecting exception due to missing MBean");
        }
        catch (Exception e) {
            // root cause should be an InstanceNotFoundException containing the ObjectName
            final Throwable t = ExceptionUtil.getRootCause(e);
            assert (t instanceof InstanceNotFoundException);
            final InstanceNotFoundException inf = (InstanceNotFoundException) t;

            final String msg = inf.getMessage();
            final int objectNameStart = msg.indexOf("amx:");
            final String objectNameString = msg.substring(objectNameStart, msg.length());

            final ObjectName on = Util.newObjectName(objectNameString);

            assert (on.equals(nsObjectName));
        }
    }



}



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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/base/NotificationServiceMgrTest.java,v 1.4 2007/05/05 05:23:53 tcfujii Exp $
* $Revision: 1.4 $
* $Date: 2007/05/05 05:23:53 $
*/
package org.glassfish.admin.amxtest.base;

import com.sun.appserv.management.base.NotificationService;
import com.sun.appserv.management.base.NotificationServiceMgr;
import com.sun.appserv.management.base.Util;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import javax.management.ObjectName;

/**
 */
public final class NotificationServiceMgrTest
        extends AMXTestBase {
    public NotificationServiceMgrTest() {
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(true);
    }

    public void
    testGet() {
        assert (getNotificationServiceMgr() != null);
    }

    public void
    testCreateRemove()
            throws Exception {
        final NotificationServiceMgr proxy = getNotificationServiceMgr();

        final NotificationService service = proxy.createNotificationService("test1", 512);

        final ObjectName objectName = Util.getObjectName(service);

        proxy.removeNotificationService(service.getName());
        assert (!getConnection().isRegistered(objectName));
    }


}



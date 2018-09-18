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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/monitor/JMXMonitorMgrTest.java,v 1.5 2007/05/05 05:24:05 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:24:05 $
*/
package org.glassfish.admin.amxtest.monitor;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.monitor.AMXStringMonitor;
import com.sun.appserv.management.monitor.JMXMonitorMgr;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.ObjectName;
import java.io.IOException;

/**
 */
public final class JMXMonitorMgrTest
        extends AMXTestBase {
    public JMXMonitorMgrTest() {
    }

    public JMXMonitorMgr
    getMgr() {
        return (getDomainRoot().getJMXMonitorMgr());
    }

    public void
    testGetMgr() {
        final JMXMonitorMgr mgr = getMgr();
        assert (mgr != null);
    }

    public void
    testCreateRemoveStringMonitor()
            throws IOException {
        final JMXMonitorMgr mgr = getMgr();

        final AMXStringMonitor mon = mgr.createStringMonitor(getClass().getName() + "Test");
        final ObjectName objectName = Util.getObjectName(mon);

        mon.setObservedAttribute("Group");
        final ObjectName observee = Util.getObjectName(mgr);
        assert (observee != null);
        mon.addObservedObject(observee);
        assert mon.containsObservedObject(observee);
        mon.removeObservedObject(observee);

        assert (getConnection().isRegistered(objectName));
        mgr.remove(mon.getName());
        assert (!getConnection().isRegistered(objectName));
    }
}



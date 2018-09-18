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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/monitor/AMXMonitorTestBase.java,v 1.6 2007/05/05 05:24:05 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:24:05 $
*/
package org.glassfish.admin.amxtest.monitor;

import com.sun.appserv.management.monitor.MonitoringRoot;
import com.sun.appserv.management.monitor.ServerRootMonitor;
import org.glassfish.admin.amxtest.AMXTestBase;

/**
 */
public abstract class AMXMonitorTestBase
        extends AMXTestBase {
    public AMXMonitorTestBase() {
    }

    protected final MonitoringRoot
    getMonitoringRoot() {
        return getDomainRoot().getMonitoringRoot();
    }

    protected final ServerRootMonitor
    getServerRootMonitor(final String name) {
        return
                getMonitoringRoot().getServerRootMonitorMap().get(name);
    }
}



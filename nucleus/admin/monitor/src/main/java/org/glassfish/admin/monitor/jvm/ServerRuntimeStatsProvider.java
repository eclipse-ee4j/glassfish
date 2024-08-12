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

package org.glassfish.admin.monitor.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/* server.runtime */
@AMXMetadata(type = "server-runtime-mon", group = "monitoring", isSingleton = true)
@ManagedObject
@Description("Server Runtime Statistics")
public class ServerRuntimeStatsProvider {

    private final RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();

    public static final int STARTING_STATE = 0;
    public static final int RUNNING_STATE = 1;
    public static final int STOPPING_STATE = 2;
    public static final int STOPPED_STATE = 3;
    public static final int FAILED_STATE = 4;
    private int state = STOPPED_STATE;

    @ManagedAttribute(id = "uptime")
    @Description("uptime of the Java virtual machine in milliseconds")
    public long getUptime() {
        return rtBean.getUptime();
    }

    @ManagedAttribute(id = "starttime")
    @Description("start time of the Java virtual machine")
    public long getStartTime() {
        return rtBean.getStartTime();
    }

    @ManagedAttribute(id = "state")
    @Description("state of the server such as Running, Stopped, Failed")
    public synchronized long getState() {
        if (rtBean != null) {
            return RUNNING_STATE;
        }
        return state;
    }

    //TODO: set state based on server events
    public synchronized void setState(int state) {
        this.state = state;
    }

}

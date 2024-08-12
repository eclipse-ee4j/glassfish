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

package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.VirtualServer;

import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

@AMXMetadata(type="virtualserverinfo-mon", group="monitoring")
@ManagedObject
@Description("Virtual Server Statistics")
public class VirtualServerInfoStatsProvider {

    private static final String STATE_DESCRIPTION =
        "The state of the virtual server";

    private static final String HOSTS_DESCRIPTION =
        "The host (alias) names of the virtual server";

    private static final String ID_DESCRIPTION =
        "The id of the virtual server";

    private static final String MODE_DESCRIPTION =
        "The mode of the virtual server";

    private VirtualServer host;

    private StringStatisticImpl state = new StringStatisticImpl(
        "State", "String", STATE_DESCRIPTION);

    private StringStatisticImpl hosts = new StringStatisticImpl(
        "Hosts", "String", HOSTS_DESCRIPTION);

    private StringStatisticImpl id = new StringStatisticImpl(
        "Id", "String", ID_DESCRIPTION);

    private StringStatisticImpl mode = new StringStatisticImpl(
        "Mode", "String", MODE_DESCRIPTION);

    public VirtualServerInfoStatsProvider(VirtualServer host) {
        this.host = host;
    }

    @ManagedAttribute(id="state")
    @Description(STATE_DESCRIPTION)
    public StringStatistic getState() {
        state.setCurrent(host.getState());
        return state;
    }

    @ManagedAttribute(id="hosts")
    @Description(HOSTS_DESCRIPTION)
    public StringStatistic getHosts() {
        hosts.setCurrent(host.getHosts());
        return hosts;
    }

    @ManagedAttribute(id="id")
    @Description(ID_DESCRIPTION)
    public StringStatistic getId() {
        id.setCurrent(host.getId());
        return id;
    }

    @ManagedAttribute(id="mode")
    @Description(MODE_DESCRIPTION)
    public StringStatistic getMode() {
        mode.setCurrent(host.getState().equals("on") ? "active" : "unknown");
        return mode;
    }
}

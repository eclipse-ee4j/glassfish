/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.gjc.monitoring;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * Provider interface for JDBCRA sql tracing related probes.
 *
 * @author Shalini M
 */
@ProbeProvider(moduleProviderName=JdbcRAConstants.GLASSFISH,
moduleName=JdbcRAConstants.JDBCRA, probeProviderName=JdbcRAConstants.SQL_TRACING_PROBE)
public class SQLTraceProbeProvider {

    /**
     * Emits probe event/notification that the given connection pool
     * <code>poolName</code>has got an event to cache a sql query
     *
     * @param poolName for which sql query should be cached
     * @param sql sql query that should be cached
     */
    @Probe(name=JdbcRAConstants.TRACE_SQL)
    public void traceSQLEvent(@ProbeParam("poolName") String poolName,
                                   @ProbeParam("appName") String appName,
                                   @ProbeParam("moduleName") String moduleName,
            @ProbeParam("sql") String sql) {

    }
}

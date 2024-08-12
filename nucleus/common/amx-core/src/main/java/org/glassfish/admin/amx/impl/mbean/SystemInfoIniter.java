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

package org.glassfish.admin.amx.impl.mbean;

import javax.management.MBeanServer;

import org.glassfish.admin.amx.impl.util.Issues;

import static org.glassfish.admin.amx.base.SystemInfo.CLUSTERS_FEATURE;
import static org.glassfish.admin.amx.base.SystemInfo.MULTIPLE_SERVERS_FEATURE;
import static org.glassfish.admin.amx.base.SystemInfo.RUNNING_IN_DAS_FEATURE;

/**
Single-use utility class to contain the  details of initializing various
SystemInfo data.  Appropriate code can be added here, but can be implemented just as
well elsewhere, most properly within the module that wishes to advertised presence of
a feature.
 */
final class SystemInfoIniter {

    private final SystemInfoImpl mSystemInfo;

    SystemInfoIniter(final MBeanServer mbeanServer, final SystemInfoImpl systemInfo) {
        mSystemInfo = systemInfo;
        Issues.getAMXIssues().notDone("How to implement supportsClusters()");
        Issues.getAMXIssues().notDone("How to implement isRunningInDomainAdminServer()");
    }

    public void init() {
        final boolean supportsClusters = supportsClusters();
        mSystemInfo.addFeature(CLUSTERS_FEATURE, supportsClusters);
        mSystemInfo.addFeature(MULTIPLE_SERVERS_FEATURE, supportsClusters);
        mSystemInfo.addFeature(RUNNING_IN_DAS_FEATURE, isRunningInDomainAdminServer());
    }

    private boolean supportsClusters() {
        return false;
    }

    private boolean isRunningInDomainAdminServer() {
        return true;
    }
}









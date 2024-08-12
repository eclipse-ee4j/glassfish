/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.monitor;

import java.util.Collection;

import org.glassfish.flashlight.client.ProbeClientMethodHandle;

public class StatsProviderUtil {

    private static StatsProviderManagerDelegateImpl spmd; // populate this during our initilaization process

    public static void setStatsProviderManagerDelegate(StatsProviderManagerDelegateImpl lspmd) {
        spmd = lspmd;
    }

    public static void setHandlesForStatsProviders(Object statsProvider, Collection<ProbeClientMethodHandle> handles) {
        if (spmd == null) {
            return;
        }
        // save the handles also against statsProvider so you can unregister when statsProvider is unregistered
        if (handles != null) {
            spmd.setHandlesForStatsProviders(statsProvider, handles);
        }
    }

    public static Boolean isMonitoringEnabled(String configElement) {
        return spmd == null ? Boolean.FALSE : spmd.getEnabledValue(configElement);
    }
}

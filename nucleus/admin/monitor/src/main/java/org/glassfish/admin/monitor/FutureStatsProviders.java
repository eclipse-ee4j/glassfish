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

package org.glassfish.admin.monitor;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.glassfish.external.probe.provider.StatsProviderInfo;

/**
 * Note that this class is not public... THREAD SAFE CLASS!
 *
 * @author bnevins
 */
class FutureStatsProviders {
    static void add(StatsProviderInfo spInfo) {
        if (spInfo != null)
            data.add(spInfo);
    }

    static void remove(StatsProviderInfo spInfo) {
        if (spInfo != null)
            data.remove(spInfo);
    }

    // does NOT support the remove operation
    static Iterator<StatsProviderInfo> iterator() {
        return data.iterator();
    }

    static boolean isEmpty() {
        return data.isEmpty();
    }

    private FutureStatsProviders() {
    }

    private final static Set data = new CopyOnWriteArraySet<StatsProviderInfo>();
}

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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ThreadPoolConfig;
import com.sun.appserv.management.config.ThreadPoolConfigKeys;
import com.sun.appserv.management.util.misc.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 */
public final class ThreadPoolConfigTest
        extends ConfigMgrTestBase {
    static final Map<String, String> OPTIONAL = new HashMap<String, String>();

    static {
        OPTIONAL.put(ThreadPoolConfigKeys.MIN_THREAD_POOL_SIZE_KEY, "10");
        OPTIONAL.put(ThreadPoolConfigKeys.MAX_THREAD_POOL_SIZE_KEY, "100");
        OPTIONAL.put(ThreadPoolConfigKeys.IDLE_THREAD_TIMEOUT_IN_SECONDS_KEY, "120");
        OPTIONAL.put(ThreadPoolConfigKeys.NUM_WORK_QUEUES_KEY, "10");
    }

    public ThreadPoolConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getConfigConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("ThreadPoolConfig");
    }

    public static ThreadPoolConfig
    ensureDefaultInstance(final ConfigConfig cc) {
        ThreadPoolConfig result = cc.getThreadPoolsConfig().getThreadPoolConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(cc, getDefaultInstanceName(), OPTIONAL);
        }

        return result;
    }

    public static ThreadPoolConfig
    createInstance(
            final ConfigConfig cc,
            final String name,
            Map<String, String> optional) {
        return cc.getThreadPoolsConfig().createThreadPoolConfig(name, optional);
    }

    protected Container
    getProgenyContainer() {
        return getConfigConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.THREAD_POOL_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getConfigConfig().getThreadPoolsConfig().removeThreadPoolConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final Map<String, String> allOptions = MapUtil.newMap(options, OPTIONAL);

        return getConfigConfig().getThreadPoolsConfig().createThreadPoolConfig(name, allOptions);
    }
}



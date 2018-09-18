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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.base.AMXDebugSupportMBean;
import org.glassfish.admin.amx.util.AMXDebugStuff;

import java.util.Map;
import java.util.Set;


/**
 This test run prior to testing any AMX MBeans.
 */
public final class RunMeFirstTest
        extends AMXTestBase {
    public RunMeFirstTest() {
        initCoverageInfos();
    }

    private void
    initCoverageInfos() {
        final Set<AMX> all = getAllAMX();

        // set the AMX-DEBUG flags on
        final String AMX_DEBUG = "-DAMX-DEBUG.enabled=true";
        final String AMX_DEBUG2 = "-DAMX-DEBUG=true";

        // set AMX-DEBUG.enabled=true in all ConfigConfig JVM options
        final Map<String, ConfigConfig> configs = getDomainConfig().getConfigsConfig().getConfigConfigMap();
        for (final ConfigConfig config : configs.values()) {
            final JavaConfig jc = config.getJavaConfig();
            final String[] opt = jc.getJVMOptions();
            final Set<String> jvmOptions = GSetUtil.newStringSet(opt == null ? new String[0] : opt );

            if (!(jvmOptions.contains(AMX_DEBUG) || jvmOptions.contains(AMX_DEBUG2))) {
                jvmOptions.add(AMX_DEBUG);
                jc.setJVMOptions(GSetUtil.toStringArray(jvmOptions));

                // don't warn for default-config; it's not used by a running server
                if (!config.getName().equals("default-config")) {
                    warning("Enabled AMX-DEBUG for config " + config.getName() +
                            " (restart required)");
                }
            }
        }

        // setup default stuff
        final AMXDebugSupportMBean debug = getAMXDebugSupportMBean();
        debug.setAll(true);
        debug.setDefaultDebug(true);
        debug.getOutputIDs();

        for (final AMX amx : all) {
            final AMXDebugStuff debugStuff = getTestUtil().asAMXDebugStuff(amx);

            if (debugStuff == null) {
                continue;
            }

            try {
                debugStuff.enableAMXDebug(true);
            }
            catch (Throwable t) {
                warning("Couldn't enableAMXDebug() for " + amx.getJ2EEType());
            }

            try {
                debugStuff.enableCoverageInfo(true);
                debugStuff.clearCoverageInfo();
            }
            catch (Throwable t) {
                warning("Couldn't enableCoverageInfo for " + amx.getJ2EEType());
            }
        }
    }
}















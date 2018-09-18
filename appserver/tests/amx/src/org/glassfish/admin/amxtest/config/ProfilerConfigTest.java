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

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.JavaConfig;
import com.sun.appserv.management.config.ProfilerConfig;
import com.sun.appserv.management.config.ProfilerConfigKeys;
import com.sun.appserv.management.util.jmx.JMXUtil;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.HashMap;
import java.util.Map;

/**
 */
public final class ProfilerConfigTest
        extends AMXTestBase {
    private static final String NATIVE_LIBRARY_PATH = "a/b/c";
    private static final String CLASSPATH = "/foo/bar";

    private static Map<String, String>
    getOptional() {
        final Map<String, String> optional = new HashMap<String, String>();
        optional.put(ProfilerConfigKeys.NATIVE_LIBRARY_PATH_KEY, NATIVE_LIBRARY_PATH);
        optional.put(ProfilerConfigKeys.CLASSPATH_KEY, CLASSPATH);
        optional.put(ProfilerConfigKeys.ENABLED_KEY, "false");
        return optional;
    }

    public ProfilerConfigTest() {
        if (checkNotOffline("testIllegalCreate")) {
            ensureDefaultInstance(getConfigConfig().getJavaConfig());
        }
    }

    public static ProfilerConfig
    ensureDefaultInstance(final JavaConfig javaConfig) {
        ProfilerConfig prof = javaConfig.getProfilerConfig();
        if (prof == null) {
            final String NAME = "profiler";

            prof = javaConfig.createProfilerConfig(NAME, getOptional());
            assert prof != null;
        }

        return prof;
    }

    private void
    testGetters(final ProfilerConfig prof) {
        assert (prof.getClasspath() != null);
        prof.setClasspath(prof.getClasspath());

        assert (prof.getNativeLibraryPath() != null);
        prof.setNativeLibraryPath(prof.getNativeLibraryPath());

        assert (prof.getJVMOptions() != null);
        prof.setJVMOptions(prof.getJVMOptions());

        prof.setEnabled(prof.getEnabled());
    }

    public synchronized void
    testCreateRemoveProfiler()
            throws Exception {
        if (checkNotOffline("testIllegalCreate")) {
            ensureDefaultInstance(getConfigConfig().getJavaConfig());

            final JavaConfig javaConfig = getConfigConfig().getJavaConfig();

            javaConfig.removeProfilerConfig();
            assert javaConfig.getProfilerConfig() == null :
                    "Can't remove ProfilerConfig from " +
                            JMXUtil.toString(Util.getObjectName(javaConfig));

            ensureDefaultInstance(javaConfig);
            assert javaConfig.getProfilerConfig() != null;
            Util.getExtra(javaConfig.getProfilerConfig()).getMBeanInfo();

            testGetters(javaConfig.getProfilerConfig());

            javaConfig.removeProfilerConfig();
            ensureDefaultInstance(javaConfig);
            assert javaConfig.getProfilerConfig() != null;
            Util.getExtra(javaConfig.getProfilerConfig()).getMBeanInfo();
            testGetters(javaConfig.getProfilerConfig());
        }
    }
}



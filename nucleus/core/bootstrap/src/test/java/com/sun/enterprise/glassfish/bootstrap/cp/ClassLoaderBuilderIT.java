/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap.cp;

import com.sun.enterprise.glassfish.bootstrap.StartupContextUtil;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;

import org.junit.jupiter.api.Test;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ASADMIN_ARGS;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES;

/**
 * Created by kokil on 5/18/17.
 */
class ClassLoaderBuilderIT {

    @Test
    void createLauncher_Felix() throws Exception {
        StartupContextCfg cfg = StartupContextUtil.createStartupContextCfg();
        assertEquals("-something", cfg.getProperty(ASADMIN_ARGS),
            ASADMIN_ARGS + " wasn't written through to startup context cfg.");
        ClassLoader loader = ClassLoaderBuilder.createOSGiFrameworkLauncherCL(cfg, ClassLoader.getPlatformClassLoader());
        assertNotNull(loader);
        Class<?> osgiClass = loader.loadClass("org.osgi.framework.BundleReference");
        assertNotNull(osgiClass);
        Class<?> clazz = loader.loadClass("org.apache.felix.framework.Felix");
        assertNotNull(clazz);
        String osgiPackages = cfg.getProperty(FRAMEWORK_SYSTEMPACKAGES);
        assertAll(
            () -> assertThat(osgiPackages,
                stringContainsInOrder("org.osgi.framework;version=\"1.10\"", "java.lang,",
                    "java.util.concurrent.locks,", "javax.xml.crypto.dom,", "org.w3c.dom.traversal,")),
            () -> assertThat(osgiPackages, not(stringContainsInOrder(".hk2."))),
            () -> assertThat(osgiPackages, anyOf(stringContainsInOrder("com.sun.jarsigner"),
                stringContainsInOrder("jdk.security.jarsigner")))
        );
    }

}

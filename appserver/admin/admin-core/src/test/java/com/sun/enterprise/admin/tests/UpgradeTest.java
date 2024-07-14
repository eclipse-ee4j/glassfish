/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.tests;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.junit.DomainXml;
import org.glassfish.tests.utils.junit.hk2.HK2JUnit5Extension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test for the domain.xml upgrade scenario
 *
 * @author Jerome Dochez
 */
@ExtendWith(HK2JUnit5Extension.class)
@DomainXml(value = "UpgradeTest.xml")
public class UpgradeTest {

    @Inject
    private ServiceLocator locator;
    @Inject
    private StartupContext startupContext;

    // FIXME: Workaround, because ServerEnvironmentImpl changes global System.properties, but other services
    //        are depending on it. The for cycle in setup() in the test will start initializations
    //        of objects, but the order is not well defined.
    //        But if the test instance injects the environment instance, it is initialized before the cycle.
    @Inject
    private ServerEnvironment environment;

    /**
     * Does the upgrade. Results will be verified in tests methods.
     */
    @BeforeEach
    public void setup() {
        System.clearProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        startupContext.getArguments().clear();

        Domain domain = locator.getService(Domain.class);
        assertNotNull(domain);

        final List<ConfigurationUpgrade> allServices = locator
            .<ConfigurationUpgrade> getAllServices(ConfigurationUpgrade.class);
        for (ConfigurationUpgrade upgrade : allServices) {
            Logger.getAnonymousLogger().info("Running upgrade " + upgrade.getClass());
        }
    }

    @AfterEach
    public void checkStateOfEnvironment() {
        // DefaultConfigUpgrade uses this property.
        assertNull(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY), "Install root in system props");
        assertNotNull(environment.getInstanceRoot(), "Instance root in ServerEnvironment");
        assertNull(startupContext.getArguments().get(SystemPropertyConstants.INSTALL_ROOT_PROPERTY),
            "Install root in startup context");
    }

    @Test
    public void threadPools() {
        List<ThreadPool> threadPools = locator.<Config>getService(Config.class).getThreadPools().getThreadPool();
        assertThat(threadPools, hasSize(3));
        String[] threadPoolNames = threadPools.stream().map(ThreadPool::getName).toArray(String[]::new);
        assertThat(threadPoolNames, arrayContaining("thread-pool-1", "http-thread-pool", "admin-thread-pool"));
    }


    @Test
    public void applicationUpgrade() {
        Applications apps = locator.getService(Applications.class);
        assertNotNull(apps);
        for (Application app : apps.getApplications()) {
            assertTrue(app.getEngine().isEmpty());
            assertThat(app.getModule(), hasSize(1));
            for (Module module : app.getModule()) {
                assertEquals(app.getName(), module.getName());
                assertFalse(module.getEngines().isEmpty());
            }
        }
    }
}

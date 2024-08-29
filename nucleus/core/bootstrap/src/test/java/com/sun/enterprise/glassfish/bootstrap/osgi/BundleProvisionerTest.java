/*
 * Copyright (c) 2023, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap.osgi;

import com.sun.enterprise.glassfish.bootstrap.Util;
import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

class BundleProvisionerTest {
    private static final Logger LOG = System.getLogger(BundleProvisionerTest.class.getName());


    @Test
    void noBundles() throws Exception {
        LOG.log(Level.INFO, LogFacade.STARTING_BUNDLEPROVISIONER);
        // TODO: add some properties, create more tests
        Properties props = new Properties();
        Path cacheDir = Files.createTempDirectory("FelixCache");
        props.setProperty(FRAMEWORK_STORAGE, cacheDir.toFile().getAbsolutePath());
        Util.substVars(props);
        long t0 = System.currentTimeMillis();
        Map<String, String> mm = props.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        Framework osgiFramework = null;
        for (FrameworkFactory ff : loader) {
            assertNull(osgiFramework, "There is more than one FrameworkFactory service available!");
            osgiFramework = ff.newFramework(mm);
        }
        assertNotNull(osgiFramework, "No OSGi framwework found!");
        long t1 = System.currentTimeMillis();
        LOG.log(Level.INFO, LogFacade.OSGI_LOCATE_TIME, (t1-t0));
        osgiFramework.init();
        long t2 = System.currentTimeMillis();
        LOG.log(Level.INFO, LogFacade.OSGI_INIT_TIME, (t2-t1));
        BundleProvisioner bundleProvisioner = BundleProvisioner
            .createBundleProvisioner(osgiFramework.getBundleContext(), props);
        bundleProvisioner.installBundles();
        long t3 = System.currentTimeMillis();
        LOG.log(Level.INFO, LogFacade.BUNDLE_INSTALLATION_TIME, (t3-t2));
        assertEquals(0, bundleProvisioner.getNoOfInstalledBundles());
        assertEquals(0, bundleProvisioner.getNoOfUpdatedBundles());
        assertEquals(0, bundleProvisioner.getNoOfUninstalledBundles());
        assertFalse(bundleProvisioner.hasAnyThingChanged());
        bundleProvisioner.startBundles();
        osgiFramework.start();

        assertEquals(Bundle.ACTIVE, osgiFramework.getState());
        long t4 = System.currentTimeMillis();
        LOG.log(Level.INFO, LogFacade.BUNDLE_STARTING_TIME, (t4-t3));
        LOG.log(Level.INFO, LogFacade.TOTAL_START_TIME, (t4-t0));
        osgiFramework.stop();
        osgiFramework.waitForStop(0);
        long t5 = System.currentTimeMillis();
        LOG.log(Level.INFO, LogFacade.BUNDLE_STOP_TIME, (t5 - t4));
        LOG.log(Level.INFO, LogFacade.TOTAL_TIME, (t5-t0));
        assertEquals(Bundle.RESOLVED, osgiFramework.getState());
    }
}

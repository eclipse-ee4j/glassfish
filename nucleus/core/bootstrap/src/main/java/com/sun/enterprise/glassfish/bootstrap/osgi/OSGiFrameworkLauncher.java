/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.glassfish.bootstrap.osgi;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Utility class which takes care of launching OSGi framework.
 * It lauches the framework in a daemon thread, because typically framework spawned threads inherit
 * parent thread's daemon status.
 *
 * It also provides a utility method to get hold of OSGi services registered in the system.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiFrameworkLauncher {

    private final Properties properties;
    private Framework framework;

    public OSGiFrameworkLauncher(Properties properties) {
        this.properties = properties;
    }


    public Framework launchOSGiFrameWork() throws Exception {
        if (isOSGiEnv()) {
            throw new IllegalStateException("An OSGi framework is already running...");
        }
        // Locate an OSGi framework and initialize it
        ServiceLoader<FrameworkFactory> frameworkFactories = ServiceLoader.load(FrameworkFactory.class,
            getClass().getClassLoader());
        Map<String, String> mm = new HashMap<>();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            mm.put((String) e.getKey(), (String) e.getValue());
        }
        for (FrameworkFactory ff : frameworkFactories) {
            framework = ff.newFramework(mm);
            break;
        }
        if (framework == null) {
            throw new RuntimeException("No OSGi framework in classpath");
        }
        // init framework in a daemon thread so that the framework spwaned internal threads will be
        // daemons
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    framework.init();
                } catch (BundleException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.setDaemon(true);
        t.start();
        t.join();
        return framework;
    }


    public <T> T getService(Class<T> type) throws Exception {
        if (framework == null) {
            throw new IllegalStateException("OSGi framework has not yet been launched.");
        }
        final BundleContext context = framework.getBundleContext();
        ServiceTracker tracker = new ServiceTracker(context, type.getName(), null);
        try {
            tracker.open(true);
            return type.cast(tracker.waitForService(0));
        } finally {
            tracker.close();
        }
    }


    /**
     * Determine if we we are operating in OSGi env. We do this by checking what class loader is
     * used to this class.
     *
     * @return false if we are already called in the context of OSGi framework, else true.
     */
    private boolean isOSGiEnv() {
        return getClass().getClassLoader() instanceof BundleReference;
    }
}

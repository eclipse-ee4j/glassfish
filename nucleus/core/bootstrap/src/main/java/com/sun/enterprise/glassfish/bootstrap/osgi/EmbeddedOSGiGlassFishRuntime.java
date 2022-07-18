/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.glassfish.bootstrap.GlassFishImpl;
import com.sun.enterprise.glassfish.bootstrap.GlassfishUrlClassLoader;
import com.sun.enterprise.glassfish.bootstrap.MainHelper;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.FelixPrettyPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Implementation of GlassFishRuntime in an OSGi environment.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EmbeddedOSGiGlassFishRuntime extends GlassFishRuntime {

    /** Millis to initiate HK2 */
    private static final int TIMEOUT_FOR_HK2 = 10000;

    private static final Logger LOG = Logger.getLogger(EmbeddedOSGiGlassFishRuntime.class.getName());

    // TODO(Sahoo): Merge with StaticGlassFishRuntime and elevate to higher package level.
    // This can be achieved by modelling this as a GlassFishRuntimeDecorator taking StaticGlassFishRuntime
    // as the decorated object.

    private final List<GlassFish> gfs = new ArrayList<>();
    private final BundleContext context;

    public EmbeddedOSGiGlassFishRuntime(BundleContext context) {
        this.context = context;
    }

    @Override
    public synchronized GlassFish newGlassFish(GlassFishProperties gfProps) throws GlassFishException {
        try {
            // set env props before updating config, because configuration update may actually trigger
            // some code to be executed which may be depending on the environment variable values.
            setEnv(gfProps.getProperties());
            final StartupContext startupContext = new StartupContext(gfProps.getProperties());

            final ServiceTracker<Main, ?> hk2Tracker = new ServiceTracker<>(context, Main.class, null);
            hk2Tracker.open();
            final Main hk2Main = (Main) hk2Tracker.waitForService(TIMEOUT_FOR_HK2);
            hk2Tracker.close();
            if (hk2Main == null) {
                throw new IllegalStateException("HK2 Main not found, check GlassFish dependencies!");
            }

            final ServiceReference<ModulesRegistry> mrServiceRef = context.getServiceReference(ModulesRegistry.class);
            final ModulesRegistry mr = context.getService(mrServiceRef);

            logClassLoaders(mr);

            final ServiceLocator serviceLocator = hk2Main.createServiceLocator(mr, startupContext, null, null);
            final ModuleStartup gfKernel = hk2Main.findStartupService(mr, serviceLocator, null, startupContext);
            final GlassFish glassFish = createGlassFish(gfKernel, serviceLocator, gfProps.getProperties());
            gfs.add(glassFish);

            return glassFish;
        } catch (BootException | InterruptedException ex) {
            throw new GlassFishException(ex);
        } catch (MultiException ex) {
            final String bundleMessage = findBundleMessage(ex);
            if (bundleMessage == null) {
                throw new GlassFishException("GlassFish failed to start.", ex);
            }
            try {
                final String prettyMessage = FelixPrettyPrinter.prettyPrintExceptionMessage(bundleMessage);
                List<Integer> bundleIDs = FelixPrettyPrinter.findBundleIds(prettyMessage);
                if (bundleIDs.isEmpty()) {
                    throw new GlassFishException(prettyMessage, ex);
                }
                final StringBuilder bundleBuilder = new StringBuilder(1024);
                bundleBuilder.append(prettyMessage);
                for (Integer bundleId : bundleIDs) {
                    Bundle bundle = context.getBundle(bundleId);
                    if (bundle != null) {
                        bundleBuilder.append('[').append(bundleId).append("] \n");
                        bundleBuilder.append("jar = ").append(bundle.getLocation());
                        tryAddPomProperties(bundle, bundleBuilder);
                        bundleBuilder.append('\n');
                    }
                }
                throw new GlassFishException(bundleBuilder.toString(), ex);
            } catch (GlassFishException ee) {
                throw ee;
            } catch (Throwable ee) {
                GlassFishException e = new GlassFishException(bundleMessage, ex);
                e.addSuppressed(ee);
                throw e;
            }
        }
    }


    @Override
    public synchronized void shutdown() throws GlassFishException {
        // make a copy to avoid ConcurrentModificationException
        for (GlassFish gf : new ArrayList<>(gfs)) {
            if (gf.getStatus() != GlassFish.Status.DISPOSED) {
                try {
                    gf.dispose();
                } catch (GlassFishException e) {
                    e.printStackTrace();
                }
            }
        }
        gfs.clear();
        shutdownInternal();
    }

    private String findBundleMessage(MultiException ex) {
        for (Throwable error : ex.getErrors()) {

            Throwable currentThrowable = error;
            while (currentThrowable != null) {
                if (currentThrowable instanceof BundleException) {
                    return currentThrowable.getMessage();
                }
                currentThrowable = currentThrowable.getCause();
            }
        }
        return null;
    }


    private void tryAddPomProperties(Bundle bundle, StringBuilder bundleBuilder) throws IOException {
        Enumeration<URL> entries = bundle.findEntries("META-INF/maven/", "pom.properties", true);
        while (entries.hasMoreElements()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entries.nextElement().openStream(), UTF_8))) {
                reader.lines()
                      .filter(e -> !e.startsWith("#"))
                      .forEach(e -> bundleBuilder.append('\n').append(e.replace("=", " = ")));
            }
            bundleBuilder.append('\n');
        }
    }


    private void setEnv(Properties properties) {
        final String installRootValue = properties.getProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME);
        if (installRootValue != null && !installRootValue.isEmpty()) {
            File installRoot = new File(installRootValue);
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
            final Properties asenv = MainHelper.parseAsEnv(installRoot);
            for (String s : asenv.stringPropertyNames()) {
                System.setProperty(s, asenv.getProperty(s));
            }
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
        }
        final String instanceRootValue = properties.getProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME);
        if (instanceRootValue != null && !instanceRootValue.isEmpty()) {
            File instanceRoot = new File(instanceRootValue);
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
            System.setProperty(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());
        }
    }

    private GlassFish createGlassFish(ModuleStartup gfKernel, ServiceLocator habitat, Properties gfProps) throws GlassFishException {
        GlassFish gf = new GlassFishImpl(gfKernel, habitat, gfProps);
        return new EmbeddedOSGiGlassFishImpl(gf, context);
    }


    private void logClassLoaders(final ModulesRegistry moduleRegistry) {
        if (!LOG.isLoggable(Level.FINEST)) {
            return;
        }
        logCL(LOG, "currentThread.contextClassLoader:       ", Thread.currentThread().getContextClassLoader());
        logCL(LOG, "this.class.classLoader:                 ", getClass().getClassLoader());
        logCL(LOG, "this.class.classLoader.parent:          ", getClass().getClassLoader().getParent());
        logCL(LOG, "moduleRegistry.parentClassLoader:       ", moduleRegistry.getParentClassLoader());
        logCL(LOG, "moduleRegistry.parentClassLoader.parent ", moduleRegistry.getParentClassLoader().getParent());
    }


    private void logCL(final Logger logger, final String label, final ClassLoader classLoader) {
        // don't use supplier here, the message must be resolved in current state, not later.
        logger.finest(label + toString(classLoader));
    }


    private String toString(final ClassLoader cl) {
        if (cl instanceof GlassfishUrlClassLoader) {
            return cl.toString();
        }
        if (cl instanceof URLClassLoader) {
            URLClassLoader ucl = URLClassLoader.class.cast(cl);
            return ucl + ": " + Arrays.stream(ucl.getURLs()).collect(Collectors.toList());
        }
        return cl.toString();
    }
}

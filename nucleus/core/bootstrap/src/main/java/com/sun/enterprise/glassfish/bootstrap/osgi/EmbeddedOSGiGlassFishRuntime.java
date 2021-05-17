/*
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

import static com.sun.enterprise.util.FelixPrettyPrinter.findBundleIds;
import static com.sun.enterprise.util.FelixPrettyPrinter.prettyPrintExceptionMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

import com.sun.enterprise.glassfish.bootstrap.GlassFishImpl;
import com.sun.enterprise.glassfish.bootstrap.MainHelper;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.FelixPrettyPrinter;

/**
 * Implementation of GlassFishRuntime in an OSGi environment.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EmbeddedOSGiGlassFishRuntime extends GlassFishRuntime {

    // TODO(Sahoo): Merge with StaticGlassFishRuntime and elevate to higher package level.
    // This can be achieved by modelling this as a GlassFishRuntimeDecorator taking StaticGlassFishRuntime
    // as the decorated object.

    List<GlassFish> gfs = new ArrayList<GlassFish>();

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
            final ServiceTracker hk2Tracker = new ServiceTracker(getBundleContext(), Main.class.getName(), null);

            hk2Tracker.open();
            final Main main = (Main) hk2Tracker.waitForService(0);
            hk2Tracker.close();

            final ModulesRegistry mr = ModulesRegistry.class.cast(getBundleContext().getService(getBundleContext().getServiceReference(ModulesRegistry.class.getName())));
            ServiceLocator serviceLocator = main.createServiceLocator(mr, startupContext, null, null);
            final ModuleStartup gfKernel = main.findStartupService(mr, serviceLocator, null, startupContext);
            GlassFish glassFish = createGlassFish(gfKernel, serviceLocator, gfProps.getProperties());
            gfs.add(glassFish);

            return glassFish;
        } catch (BootException | InterruptedException ex) {
            throw new GlassFishException(ex);
        } catch (MultiException ex) {
            GlassFishException e = null;

            String bundleMessage = "";
            try {
                bundleMessage = findBundleMessage(ex);
                if (bundleMessage != null) {

                    bundleMessage = prettyPrintExceptionMessage(bundleMessage);

                    List<Integer> bundleIDs = findBundleIds(bundleMessage);
                    if (!bundleIDs.isEmpty()) {
                        StringBuilder bundleBuilder = new StringBuilder(bundleMessage);
                        for (Integer bundleId : bundleIDs) {
                            Bundle bundle = context.getBundle(bundleId);
                            if (bundle != null) {
                                bundleBuilder.append("[" + bundleId + "] \n").append("jar = " + bundle.getLocation());
                                tryAddPomProperties(bundle, bundleBuilder);
                                bundleBuilder.append("\n");
                            }
                        }
                        bundleMessage = bundleBuilder.toString();
                    }

                    e = new GlassFishException("\n\n" + bundleMessage, ex);
                }
            } catch (Exception ee) {
                e = new GlassFishException("\n\n" + bundleMessage, ex);
                e.addSuppressed(ee);
            }

            if (e == null) {
                throw ex;
            }

            throw e;
        }
    }

    private void tryAddPomProperties(Bundle bundle, StringBuilder bundleBuilder) throws IOException {
        Enumeration<URL> entries = bundle.findEntries("META-INF/maven/", "pom.properties", true);
        while (entries.hasMoreElements()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entries.nextElement().openStream(), UTF_8))) {
                reader.lines()
                      .filter(e -> !e.startsWith("#"))
                      .forEach(e -> bundleBuilder.append("\n" + e.replace("=", " = ")));
            }
            bundleBuilder.append("\n");
        }
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

    public synchronized void shutdown() throws GlassFishException {
        // make a copy to avoid ConcurrentModificationException
        for (GlassFish gf : new ArrayList<GlassFish>(gfs)) {
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
        System.out.println("Completed shutdown of GlassFish runtime");
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

    protected GlassFish createGlassFish(ModuleStartup gfKernel, ServiceLocator habitat, Properties gfProps) throws GlassFishException {
        GlassFish gf = new GlassFishImpl(gfKernel, habitat, gfProps);
        return new EmbeddedOSGiGlassFishImpl(gf, getBundleContext());
    }

    private BundleContext getBundleContext() {
        return context;
    }
}

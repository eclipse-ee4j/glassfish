/*
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

package org.glassfish.uberjar.osgimain;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author bhavanishankar@dev.java.net
 */

public class Main implements BundleActivator {

    private static final String UBER_JAR_URI = "org.glassfish.embedded.osgimain.jarURI";

    public static final String AUTO_START_BUNDLES_PROP =
            "org.glassfish.embedded.osgimain.autostartBundles";

    private List<String> excludedModules = new ArrayList();

    private List<Bundle> autoStartBundles = new ArrayList();

    HashMap<String, Bundle> autostartBundles = new HashMap();

    private static final Logger logger = Logger.getLogger("embedded-glassfish");

    public void start(final BundleContext context) throws Exception {
        logger.logp(Level.FINER, "Main", "start", "Start has been called. BundleContext = {0}", context);


        URI embeddedJarURI = new URI(context.getProperty(UBER_JAR_URI));

        final String autoStartBundleLocation = context.getProperty(AUTO_START_BUNDLES_PROP);

//        String autoStartBundleLocation = context.getProperty(AUTO_START_BUNDLES_PROP); // TODO :: parse multiple values.

        logger.info("Please wait while the GlassFish is being initialized...");
        logger.finer("embeddedJar = " + embeddedJarURI + ", autoStartBundles = " + autoStartBundleLocation);
        logger.finer("Installing GlassFish bundles. Please wait.....");
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Bundle[] bundles = context.getBundles();
        final Map<String, Bundle> installedBundles = new HashMap();
        if (bundles != null && bundles.length > 0) {
            for (Bundle b : bundles) {
                installedBundles.put(b.getSymbolicName(), b);
            }
        }
        final File embeddedJar = new File(embeddedJarURI);

        for (OSGIModule module : ModuleExtractor.extractModules(embeddedJar)) {
            final OSGIModule m = module;
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        String bundleLocation = m.getLocation();
                        if (!excludedModules.contains(bundleLocation)) {
                            Bundle installed = m.getBundleSymbolicName() != null ?
                                    installedBundles.get(m.getBundleSymbolicName()) : null;
                            if(installed == null) {
                                installed = context.installBundle(bundleLocation, m.getContentStream());
                            } else if(installed.getLastModified() < embeddedJar.lastModified()) {
                                // update the bundle if it is already installed and is older than uber jar.
                                installed.update(m.getContentStream());
                            }
                            if (autoStartBundleLocation.indexOf(bundleLocation) != -1) {
                                autostartBundles.put(bundleLocation, installed);
                            }
                        }
                        m.close();
                    } catch (Exception ex) {
                        logger.finer(ex.getMessage());
                    }
                }
            });
        }

        logger.finer("Waiting to complete installation of all bundles. Please wait.....");
        executor.shutdown();
        boolean completed = executor.awaitTermination(120, TimeUnit.SECONDS);
        logger.finer("Completed successfully ? " + completed);

        // Autostart the bundles in the order in which they are specified.
        if (autoStartBundleLocation != null) {
            StringTokenizer st = new StringTokenizer(autoStartBundleLocation, ",");
            while (st.hasMoreTokens()) {
                String bundleLocation = st.nextToken().trim();
                if (bundleLocation.isEmpty()) break;
                Bundle b = autostartBundles.get(bundleLocation);
                if (b != null) {
                    logger.finer("Starting bundle " + b);
                    try {
                        b.start();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    logger.finer("Started bundle " + b);
                } else {
                    logger.warning("Unable to find bundle with location " + bundleLocation);
                }
            }
        }

        logger.finer("Autostart bundles = " + autoStartBundles);
        for (Bundle bundle : autoStartBundles) {
            bundle.start();
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        logger.logp(Level.FINER, "Main", "stop", "Stop has been called. BundleContext = {0}", bundleContext);
    }


}

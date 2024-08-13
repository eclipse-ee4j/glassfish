/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.enterprise.glassfish.bootstrap.Constants.HK2_CACHE_DIR;
import static com.sun.enterprise.glassfish.bootstrap.Constants.INHABITANTS_CACHE;
import static com.sun.enterprise.glassfish.bootstrap.osgi.Constants.BUNDLEIDS_FILENAME;
import static com.sun.enterprise.glassfish.bootstrap.osgi.Constants.PROVISIONING_OPTIONS_FILENAME;
import static com.sun.enterprise.glassfish.bootstrap.osgi.Constants.PROVISIONING_OPTIONS_PREFIX;
import static com.sun.enterprise.util.FelixPrettyPrinter.prettyPrintFelixMessage;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import com.sun.enterprise.glassfish.bootstrap.LogFacade;
import com.sun.enterprise.glassfish.bootstrap.MainHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.spi.RuntimeBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

/**
 * This RuntimeBuilder can only handle GlassFish_Platform of following types:
 * <p/>
 * {@link com.sun.enterprise.glassfish.bootstrap.Constants.Platform#Felix},
 * {@link com.sun.enterprise.glassfish.bootstrap.Constants.Platform#Equinox},
 * and {@link com.sun.enterprise.glassfish.bootstrap.Constants.Platform#Knopflerfish}.
 * <p/>
 * <p/>It can't handle GenericOSGi platform,
 * because it reads framework configuration from a framework specific file when it calls
 * {@link MainHelper#buildStartupContext(java.util.Properties)}.
 * <p/>
 * This class is responsible for
 * a) setting up OSGi framework,
 * b) installing glassfish bundles,
 * c) starting a configured list of bundles
 * d) obtaining a reference to GlassFishRuntime OSGi service.
 * <p/>
 * Steps #b & #c are handled via {@link BundleProvisioner}.
 * We specify our provisioning bundle details in the properties object that's used to boostrap
 * the system. BundleProvisioner installs and starts such bundles,
 * <p/>
 * If caller does not pass in a properly populated properties object, we assume that we are
 * running against an existing installation of glassfish and set appropriate default values.
 * <p/>
 * <p/>
 * This class is registered as a provider of RuntimeBuilder using META-INF/services file.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class OSGiGlassFishRuntimeBuilder implements RuntimeBuilder {

    /*
     * Not a thread safe class.
     */

    private Framework framework;

    private final Logger logger = LogFacade.BOOTSTRAP_LOGGER;

    private Properties oldProvisioningOptions;
    private Properties newProvisioningOptions;

    // These two should be a part of an external interface of HK2, but they are not, so we have to duplicate them here.
    private OSGiFrameworkLauncher fwLauncher;

    /**
     * Default constructor needed for meta-inf/service lookup to work
     */
    public OSGiGlassFishRuntimeBuilder() {}

    @Override
    public GlassFishRuntime build(BootstrapProperties bsProps) throws GlassFishException {
        try {
            MainHelper.buildStartupContext(bsProps.getProperties());
            Properties properties = bsProps.getProperties();

            // Set the builder name so that when we check for nonEmbedded() inside GlassFishMainActivator,
            // we can identify the environment.
            properties.setProperty(Constants.BUILDER_NAME_PROPERTY, getClass().getName());
            // Step 0: Locate and launch a framework
            long t0 = System.currentTimeMillis();
            fwLauncher = new OSGiFrameworkLauncher(properties);
            framework = fwLauncher.launchOSGiFrameWork();
            long t1 = System.currentTimeMillis();
            logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "build", "Launched {0}", new Object[]{framework});

            // Step 1: install/update/delete bundles
            if (newFramework()) {
                storeProvisioningOptions(properties);
            } else {
                reconfigure(properties); // this will reconfigure if any provisioning options have changed.
            }
            BundleProvisioner bundleProvisioner = BundleProvisioner.createBundleProvisioner(
                    framework.getBundleContext(), properties);
            List<Long> bundleIds = bundleProvisioner.installBundles();

            if (bundleProvisioner.hasAnyThingChanged()) {
                bundleProvisioner.refresh();
                deleteHK2Cache(properties); // clean hk2 cache so that updated bundle details will go in there.
                // Save the bundle ids for use during restart.
                storeBundleIds(bundleIds.toArray(new Long[bundleIds.size()]));
            }
            if (bundleProvisioner.isSystemBundleUpdationRequired()) {
                logger.log(Level.INFO, LogFacade.UPDATING_SYSTEM_BUNDLE);
                framework.update();
                framework.waitForStop(0);
                framework.init();
                bundleProvisioner.setBundleContext(framework.getBundleContext());
            }

            // Step 2: Start bundles
            bundleProvisioner.startBundles();
            long t2 = System.currentTimeMillis();

            // Step 3: Start the framework, so bundles will get activated as per their start levels
            framework.getBundleContext().addFrameworkListener(new ErrorLogger(framework));
            framework.start();
            long t3 = System.currentTimeMillis();
            printStats(bundleProvisioner, t0, t1, t2, t3);

            // Step 4: Obtain reference to GlassFishRuntime and return the same
            return getGlassFishRuntime();
        } catch (Exception e) {
            throw new GlassFishException(e);
        }
    }

    private static class ErrorLogger implements FrameworkListener {

        final org.apache.felix.framework.Logger felixLogger = new org.apache.felix.framework.Logger();
        final Framework framework;

        public ErrorLogger(Framework framework) {
            this.framework = framework;
        }

        @Override
        public void frameworkEvent(FrameworkEvent event) {
            if (event.getThrowable() != null) {
                felixLogger.log(
                    framework,
                    org.apache.felix.framework.Logger.LOG_ERROR,
                    prettyPrintFelixMessage(
                        framework.getBundleContext(),
                        event.getThrowable().getMessage()) ,
                    event.getThrowable());
            }
        }
    }

    @Override
    public boolean handles(BootstrapProperties bsProps) {
        // See GLASSFISH-16743 for the reason behind additional check
        final String builderName = bsProps.getProperty(Constants.BUILDER_NAME_PROPERTY);
        if (builderName != null && !builderName.equals(getClass().getName())) {
            return false;
        }
        /*
         * This builder can't handle GOSGi platform, because we read framework configuration from a framework
         * specific file in MainHelper.buildStartupContext(properties);
         */
        String platformStr = bsProps.getProperty(Constants.PLATFORM_PROPERTY_KEY);
        if (platformStr != null && platformStr.trim().length() != 0) {
            try {
                Constants.Platform platform = Constants.Platform.valueOf(platformStr);
                switch (platform) {
                    case Felix:
                    case Equinox:
                    case Knopflerfish:
                        return true;
                }
            } catch (IllegalArgumentException ex) {
                // might be a plugged-in custom platform.
            }
        }
        return false;
    }

    private GlassFishRuntime getGlassFishRuntime() throws GlassFishException {
        final ServiceReference<GlassFishRuntime> reference = framework.getBundleContext()
            .getServiceReference(GlassFishRuntime.class);
        if (reference != null) {
            GlassFishRuntime embeddedGfr = framework.getBundleContext().getService(reference);
            return new OSGiGlassFishRuntime(embeddedGfr, framework);
        }
        throw new GlassFishException("No GlassFishRuntime available");
    }

    private void deleteHK2Cache(Properties properties) throws GlassFishException {
        // This is a HACK - thanks to some weired optimization trick
        // done for GlassFish. HK2 maintains a cache of inhabitants and
        // that needs to be recreated when there is a change in modules dir.
        final String cacheDir = properties.getProperty(HK2_CACHE_DIR);
        if (cacheDir != null) {
            File inhabitantsCache = new File(cacheDir, INHABITANTS_CACHE);
            if (inhabitantsCache.exists()) {
                logger.logp(Level.CONFIG, "OSGiGlassFishRuntimeBuilder", "deleteHK2Cache",
                    "Deleting OSGI inhabitants cache {0} ...", inhabitantsCache);
                if (!inhabitantsCache.delete()) {
                    throw new GlassFishException("Cannot delete cache: " + inhabitantsCache.getAbsolutePath());
                }
            }
        }
    }

    private void printStats(BundleProvisioner bundleProvisioner, long t0, long t1, long t2, long t3) {
        logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "build",
                "installed = {0}, updated = {1}, uninstalled = {2}",
                new Object[]{bundleProvisioner.getNoOfInstalledBundles(),
                        bundleProvisioner.getNoOfUpdatedBundles(),
                        bundleProvisioner.getNoOfUninstalledBundles()});
        logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "build",
                "Total time taken (in ms) to initialize framework = {0}, " +
                        "to install/update/delete/start bundles = {1}, " +
                        "to start framework= {2}",
                new Object[]{t1 - t0, t2 - t1, t3 - t2});
    }

    private boolean newFramework() {
        BundleContext context = framework.getBundleContext();
        return context == null || context.getBundles().length == 1;
    }

    /**
     * This method helps in situations where glassfish installation directory has been moved or
     * certain initial provisoning options have changed, etc. If such thing has happened, it uninstalls
     * all the bundles that were installed from GlassFish installation location.
     */
    private void reconfigure(Properties properties) throws Exception {
        if (hasBeenReconfigured(properties)) {
            logger.log(Level.INFO, LogFacade.PROVISIONING_OPTIONS_CHANGED);
            framework.stop();
            framework.waitForStop(0);
            properties.setProperty(FRAMEWORK_STORAGE_CLEAN,
                    FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            fwLauncher = new OSGiFrameworkLauncher(properties);
            framework = fwLauncher.launchOSGiFrameWork();
            logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "reconfigure", "Launched {0}",
                    new Object[]{framework});
            storeProvisioningOptions(properties);
        }
    }

    private boolean hasBeenReconfigured(Properties properties) {
        logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "hasBeenReconfigured", "oldProvisioningOptions = {0}",
                new Object[]{getOldProvisioningOptions()});
        logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "hasBeenReconfigured", "newProvisioningOptions = {0}",
                new Object[]{getNewProvisioningOptions(properties)});
        return !getNewProvisioningOptions(properties).equals(getOldProvisioningOptions());
    }

    /**
     * @return properties used by BundleProvisioner
     */
    private Properties getNewProvisioningOptions(Properties properties) {
        if (newProvisioningOptions == null) {
            Properties props = new Properties();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(PROVISIONING_OPTIONS_PREFIX)) {
                    props.setProperty(key, properties.getProperty(key));
                }
            }
            newProvisioningOptions = props;
        }
        return newProvisioningOptions;
    }

    private void storeBundleIds(Long[] bundleIds) {
        File f = framework.getBundleContext().getDataFile(BUNDLEIDS_FILENAME);
        // GLASSFISH-19623: f can be null
        if (f == null) {
            logger.log(Level.WARNING, LogFacade.CANT_STORE_BUNDLEIDS);
            return;
        }
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f))) {
            os.writeObject(bundleIds);
            logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "storeBundleIds", "Stored bundle ids in {0}", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeProvisioningOptions(Properties properties) {
        BundleContext context = framework.getBundleContext();
        if (context == null) {
            throw new IllegalStateException("No bundle context available!");
        }
        File f = context.getDataFile(PROVISIONING_OPTIONS_FILENAME);
        // GLASSFISH-19623: f can be null
        if (f == null) {
            logger.log(Level.WARNING, LogFacade.CANT_STORE_PROVISIONING_OPTIONS);
            return;
        }
        try (FileOutputStream os = new FileOutputStream(f)) {
            getNewProvisioningOptions(properties).store(os, "");
            os.flush();
            logger.logp(Level.CONFIG, "OSGiGlassFishRuntimeBuilder", "storeProvisioningOptions",
                "Stored provisioning options in {0}", new Object[] {f.getAbsolutePath()});
        } catch (IOException e) {
            logger.log(Level.WARNING, "Storing provisioning options failed.", e);
        }
    }

    private Properties getOldProvisioningOptions() {
        if (oldProvisioningOptions == null) {
            Properties options = new Properties();
            try {
                File f = framework.getBundleContext().getDataFile(PROVISIONING_OPTIONS_FILENAME);
                if (f != null && f.exists()) {
                    options.load(new FileInputStream(f));
                    logger.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "getOldProvisioningOptions",
                            "Read provisioning options from {0}", new Object[]{f.getAbsolutePath()});
                    oldProvisioningOptions = options;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Loading provisioning options failed.", e);
            }
        }
        return oldProvisioningOptions;
    }

}

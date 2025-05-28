/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.BUNDLEIDS_FILENAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.HK2_CACHE_DIR;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.INHABITANTS_CACHE;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.ONDEMAND_BUNDLE_PROVISIONING;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PROVISIONING_OPTIONS_FILENAME;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PROVISIONING_OPTIONS_PREFIX;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;

/**
 * This RuntimeBuilder can only handle GlassFish_Platform of following types:
 * <p/>
 * {@link com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform#Felix},
 * {@link com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform#Equinox},
 * and {@link com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform#Knopflerfish}.
 * <p>
 * It can't handle Generic OSGi platform,
 * because it reads framework configuration from a framework specific file, see {@link OsgiPlatform}
 * <p>
 * This class is responsible for
 * <ol>
 * <li>setting up OSGi framework,
 * <li>installing glassfish bundles,
 * <li>starting a configured list of bundles
 * <li>obtaining a reference to GlassFishRuntime OSGi service.
 * </ol>
 * Steps #b & #c are handled via {@link BundleProvisioner}.
 * We specify our provisioning bundle details in the properties object that's used to boostrap
 * the system. BundleProvisioner installs and starts such bundles,
 * <p>
 * If caller does not pass in a properly populated properties object, we assume that we are
 * running against an existing installation of glassfish and set appropriate default values.
 * <p>
 * This class is registered as a provider of RuntimeBuilder using META-INF/services file.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
// 1. Used in a service file!
// 2. Not a thread safe class.
public final class OSGiGlassFishRuntimeBuilder implements RuntimeBuilder {

    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;

    private Properties oldProvisioningOptions;
    private Properties newProvisioningOptions;

    // These two should be a part of an external interface of HK2, but they are not, so we have to duplicate them here.
    private OSGiFrameworkLauncher fwLauncher;
    private Framework framework;

    @Override
    public GlassFishRuntime build(BootstrapProperties bsProps, ClassLoader classloader) throws GlassFishException {
        try {
            Properties properties = bsProps.getProperties();

            // Set the builder name so that when we check for nonEmbedded() inside GlassFishMainActivator,
            // we can identify the environment.
            properties.setProperty(BootstrapKeys.BUILDER_NAME_PROPERTY, getClass().getName());
            // Step 0: Locate and launch a framework
            long t0 = System.currentTimeMillis();
            fwLauncher = new OSGiFrameworkLauncher(properties, classloader);
            framework = fwLauncher.launchOSGiFrameWork();
            long t1 = System.currentTimeMillis();
            LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "build", "Launched {0}", framework);

            // Step 1: install/update/delete bundles
            if (newFramework()) {
                storeProvisioningOptions(properties);
            } else {
                // this will reconfigure if any provisioning options have changed.
                reconfigure(properties, classloader);
            }
            BundleProvisioner bundleProvisioner = createBundleProvisioner(framework.getBundleContext(), properties);
            LOG.log(Level.CONFIG, LogFacade.CREATE_BUNDLE_PROVISIONER, bundleProvisioner);
            List<Long> bundleIds = bundleProvisioner.installBundles();

            if (bundleProvisioner.hasAnyThingChanged()) {
                bundleProvisioner.refresh();
                // clean hk2 cache so that updated bundle details will go in there.
                deleteHK2Cache(properties);
                // Save the bundle ids for use during restart.
                storeBundleIds(bundleIds.toArray(new Long[bundleIds.size()]));
            }
            if (bundleProvisioner.isSystemBundleUpdationRequired()) {
                LOG.log(Level.INFO, LogFacade.UPDATING_SYSTEM_BUNDLE);
                framework.update();
                framework.waitForStop(0);
                framework.init();
                bundleProvisioner.setBundleContext(framework.getBundleContext());
            }

            // Step 2: Start bundles
            bundleProvisioner.startBundles();
            long t2 = System.currentTimeMillis();

            // Step 3: Start the framework, so bundles will get activated as per their start levels
            framework.start();
            long t3 = System.currentTimeMillis();
            printStats(bundleProvisioner, t0, t1, t2, t3);

            // Step 4: Obtain reference to GlassFishRuntime and return the same
            return getGlassFishRuntime();
        } catch (Exception e) {
            throw new GlassFishException(e);
        }
    }

    @Override
    public boolean handles(BootstrapProperties bsProps) {
        // See GLASSFISH-16743 for the reason behind additional check
        final String builderName = bsProps.getProperty(BootstrapKeys.BUILDER_NAME_PROPERTY);
        if (builderName != null && !builderName.equals(getClass().getName())) {
            return false;
        }
        // This builder can't handle Generic OSGi platform, because we read framework configuration
        // from a framework specific file in MainHelper.buildStartupContext(properties);
        String platformStr = bsProps.getProperty(BootstrapKeys.PLATFORM_PROPERTY_KEY);
        if (platformStr != null && !platformStr.isBlank()) {
            try {
                OsgiPlatform osgiPlatform = OsgiPlatform.valueOf(platformStr);
                switch (osgiPlatform) {
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
                LOG.logp(Level.CONFIG, "OSGiGlassFishRuntimeBuilder", "deleteHK2Cache",
                    "Deleting OSGI inhabitants cache {0} ...", inhabitantsCache);
                if (!inhabitantsCache.delete()) {
                    throw new GlassFishException("Cannot delete cache: " + inhabitantsCache.getAbsolutePath());
                }
            }
        }
    }

    private void printStats(BundleProvisioner bundleProvisioner, long t0, long t1, long t2, long t3) {
        LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "build",
                "installed = {0}, updated = {1}, uninstalled = {2}",
                new Object[]{bundleProvisioner.getNoOfInstalledBundles(),
                        bundleProvisioner.getNoOfUpdatedBundles(),
                        bundleProvisioner.getNoOfUninstalledBundles()});
        LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "build",
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
     * @param classloader
     */
    private void reconfigure(Properties properties, ClassLoader classloader) throws Exception {
        if (hasBeenReconfigured(properties)) {
            LOG.log(Level.INFO, LogFacade.PROVISIONING_OPTIONS_CHANGED);
            framework.stop();
            framework.waitForStop(0);
            properties.setProperty(FRAMEWORK_STORAGE_CLEAN, FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            fwLauncher = new OSGiFrameworkLauncher(properties, classloader);
            framework = fwLauncher.launchOSGiFrameWork();
            LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "reconfigure", "Launched {0}", framework);
            storeProvisioningOptions(properties);
        }
    }

    private boolean hasBeenReconfigured(Properties properties) {
        LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "hasBeenReconfigured", "oldProvisioningOptions = {0}",
                getOldProvisioningOptions());
        Properties newOptions = getNewProvisioningOptions(properties);
        LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "hasBeenReconfigured", "newProvisioningOptions = {0}",
            newOptions);
        return !newOptions.equals(getOldProvisioningOptions());
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
            LOG.log(Level.WARNING, LogFacade.CANT_STORE_BUNDLEIDS);
            return;
        }
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f))) {
            os.writeObject(bundleIds);
            LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "storeBundleIds", "Stored bundle ids in {0}", f);
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
            LOG.log(Level.WARNING, LogFacade.CANT_STORE_PROVISIONING_OPTIONS);
            return;
        }
        try (FileOutputStream os = new FileOutputStream(f)) {
            getNewProvisioningOptions(properties).store(os, "");
            os.flush();
            LOG.logp(Level.CONFIG, "OSGiGlassFishRuntimeBuilder", "storeProvisioningOptions",
                "Stored provisioning options in {0}", f);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Storing provisioning options failed.", e);
        }
    }

    private Properties getOldProvisioningOptions() {
        if (oldProvisioningOptions == null) {
            Properties options = new Properties();
            try {
                File f = framework.getBundleContext().getDataFile(PROVISIONING_OPTIONS_FILENAME);
                if (f != null && f.exists()) {
                    options.load(new FileInputStream(f));
                    LOG.logp(Level.FINE, "OSGiGlassFishRuntimeBuilder", "getOldProvisioningOptions",
                            "Read provisioning options from {0}", f);
                    oldProvisioningOptions = options;
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Loading provisioning options failed.", e);
            }
        }
        return oldProvisioningOptions;
    }


    private static BundleProvisioner createBundleProvisioner(BundleContext bctx, Properties props) {
        final boolean ondemandProvisioning = Boolean.parseBoolean(props.getProperty(ONDEMAND_BUNDLE_PROVISIONING));
        if (ondemandProvisioning) {
            return new MinimalBundleProvisioner(bctx, props);
        }
        return new BundleProvisioner(bctx, props);
    }
}

/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * Goes through a list of URIs and installs bundles from those locations.
 * It installs the bundles in the same order as they appear in the configuration.
 * <p>
 * This class is also responsible for updating or uninstalling bundles during
 * subsequent restart if jars have been updated or deleted.
 * <p>
 * It can also be passed a list of URIs of bundles to be started automatically.
 * The list of bundles to be started must be a subset of list of bundles to be
 * installed. The autostart bundles can also be configured with start level.
 * It can be configured to start bundles persistently or transiently.
 * <p>
 * This being a provisioning service itself can't expect too many other services to be available.
 * So, it relies on core framework services only.
 * <p>
 * Several operations of this class can be customized via a {@link BundleProvisionerCustomizer}
 * object. Please refer to {@link DefaultBundleProvisionerCustomizer} for the default policy.
 * <p>
 * Implementation Note: Since bundle installation order can affect OSGi packager resolver, this class
 * honors the order specified by user.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
public class BundleProvisioner {

    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;

    private GlassFishBundleContext bundleContext;
    private boolean systemBundleUpdationRequired;
    private final Map<URI, Jar> currentManagedBundles = new HashMap<>();
    private int noOfUninstalledBundles;
    private int noOfUpdatedBundles;
    private int noOfInstalledBundles;
    private final BundleProvisionerCustomizer customizer;

    public BundleProvisioner(BundleContext bundleContext, Properties config) {
        this(bundleContext, new DefaultBundleProvisionerCustomizer(config));
    }

    public BundleProvisioner(BundleContext bundleContext, BundleProvisionerCustomizer customizer) {
        this.bundleContext = new GlassFishBundleContext(bundleContext);
        this.customizer = customizer;
    }


    public BundleContext getBundleContext() {
        return bundleContext.unwrap();
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = new GlassFishBundleContext(bundleContext);
    }


    /**
     * This method goes collects list of bundles that have been installed
     * from the watched directory in previous run of the program,
     * compares them with the current set of jar files,
     * uninstalls old bundles, updates modified bundles, installs new bundles.
     *
     * @return list of bundle ids provisioned by this provisoner.
     */
    public List<Long> installBundles() {
        initCurrentManagedBundles();
        final Collection<Jar> current = getBundleJars();
        List<Jar> discovered = discoverJars();

        // Find out all the new, deleted and common bundles.
        // new = discovered - current
        List<Jar> newBundles = new ArrayList<>(discovered);
        newBundles.removeAll(current);

        // deleted = current - discovered
        List<Jar> deletedBundles = new ArrayList<>(current);
        deletedBundles.removeAll(discovered);

        // existing = intersection of current & discovered
        List<Jar> existingBundles = new ArrayList<>(discovered);
        // We remove discovered ones from current, so that we are left
        // with a collection of Jars made from files so that we can compare
        // them with bundles.
        existingBundles.retainAll(current);

        // We do the operations in the following order:
        // uninstall, update, install, refresh & start.
        uninstall(deletedBundles);
        update(existingBundles);
        install(newBundles);
        List<Long> ids = new ArrayList<>();
        for (Jar jar : currentManagedBundles.values()) {
            ids.add(jar.getBundleId());
        }
        return ids;
    }

    /**
     * Go through the list of auto start bundles and start them.
     */
    public void startBundles() {
        for (URI uri : getAutoStartLocations()) {
            Bundle bundle = getBundle(new Jar(uri));
            if (bundle == null) {
                LOG.log(Level.WARNING, LogFacade.CANT_START_BUNDLE, uri);
                continue;
            }
            startBundle(bundle);
        }
    }

    /**
     * Start a bundle using given policy
     *
     * @param bundle
     */
    private void startBundle(Bundle bundle) {
        if (!isFragment(bundle)) {
            try {
                bundle.start(customizer.getStartOptions());
                LOG.logp(Level.FINE, "BundleProvisioner", "startBundle", "Started bundle = {0}", bundle);
            } catch (BundleException e) {
                LogFacade.log(LOG,
                        Level.WARNING,
                        LogFacade.BUNDLE_START_FAILED,
                        e,
                        bundle);
            }
        }
    }

    /**
     * This method goes through all the currently installed bundles
     * and returns information about those bundles whose location
     * refers to locations as we have been configured to manage.
     */
    private void initCurrentManagedBundles() {
        Bundle[] bundles = getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            try {
                final long id = bundle.getBundleId();
                if (id == 0) {
                    // We can't manage system bundle
                    continue;
                }
                Jar jar = new Jar(bundle);
                if (customizer.isManaged(jar)) {
                    addBundle(jar);
                }
            }
            catch (URISyntaxException e) {
                // Ignore and continue.
                // This can never happen for bundles that have been installed
                // by FileInstall, as we always use proper filepath as location.
            }
        }
    }

    /**
     * @return list of URIs configured to be installed everytime
     */
    private List<URI> getAutoInstallLocations() {
        return customizer.getAutoInstallLocations();
    }

    /**
     * @return list of URIs configured to be started everytime
     */
    private List<URI> getAutoStartLocations() {
        return customizer.getAutoStartLocations();
    }

    private Integer getStartLevel(Jar jar) {
        return customizer.getStartLevel(jar);
    }

    /**
     * Goes through the list of URIs configured via the config properties and converst them into
     * bundle Jar objects. It delegates to the customizer to discover the bundle jars.
     *
     * @return
     */
    private List<Jar> discoverJars() {
        List<Jar> jars = new ArrayList<>();
        for (URI uri : getAutoInstallLocations()) {
            jars.add(new Jar(uri));
        }
        return jars;
    }

    private int uninstall(List<Jar> bundles) {
        for (Jar jar : bundles) {
            Bundle bundle = getBundle(jar);
            if (bundle == null) {
                // this is highly unlikely, but can't be ruled out.
                LOG.log(Level.WARNING, LogFacade.BUNDLE_ALREADY_UNINSTALED, new Object[]{jar.getPath()});
                continue;
            }
            try {
                if (isFrameworkExtensionBundle(bundle)) {
                    setSystemBundleUpdationRequired(true);
                }
                bundle.uninstall();
                noOfUninstalledBundles++;
                removeBundle(jar);
                LOG.log(Level.INFO, LogFacade.UNINSTALLED_BUNDLE, new Object[]{bundle.getBundleId(), jar.getPath()});
            } catch (Exception e) {
                LogFacade.log(LOG,
                        Level.WARNING,
                        LogFacade.BUNDLE_UNINSTALL_FAILED,
                        e,
                        jar.getPath());
            }
        }
        return noOfUninstalledBundles;
    }

    private int update(Collection<Jar> jars) {
        for (Jar jar : jars) {
            final Jar existingJar = getBundleJar(jar);
            if (jar.isNewer(existingJar)) {
                Bundle bundle = getBundle(existingJar);
                if (bundle == null) {
                    // this is highly unlikely, but can't be ruled out.
                    LOG.log(Level.WARNING, LogFacade.CANT_UPDATE_ALREADY_INSTALLED, new Object[]{existingJar.getPath()});
                    continue;
                }
                try {
                    if (isFrameworkExtensionBundle(bundle)) {
                        setSystemBundleUpdationRequired(true);
                    }
                    bundle.update();
                    noOfUpdatedBundles++;
                    LOG.log(Level.INFO, LogFacade.BUNDLE_UPDATED, new Object[]{bundle.getBundleId(), jar.getPath()});
                } catch (Exception e) {
                    LogFacade.log(LOG,
                            Level.WARNING,
                            LogFacade.UPDATE_FAILED,
                            e,
                            jar.getPath());
                }
            }
        }
        return noOfUpdatedBundles;
    }

    private Collection<Jar> getBundleJars() {
        return currentManagedBundles.values();
    }

    private Jar getBundleJar(Jar jar) {
        return currentManagedBundles.get(jar.getURI());
    }

    private void addBundle(Jar jar) throws URISyntaxException {
        currentManagedBundles.put(jar.getURI(), jar);
    }

    private void removeBundle(Jar jar) throws URISyntaxException {
        currentManagedBundles.remove(jar.getURI());
    }

    /**
     * Return a bundle corresponding to this jar object.
     * It first searches using BundleContext as opposed to {@link #currentManagedBundles} so that it can give
     * more accurate results if bundles have been uninstalled without our knowledge.
     *
     * @param jar
     * @return
     */
    private Bundle getBundle(Jar jar) {
        long bundleId = jar.getBundleId();
        if (bundleId < 0) {
            final Jar jar1 = currentManagedBundles.get(jar.getURI());
            if (jar1 != null) {
                bundleId = jar1.getBundleId();
            }
        }
        return getBundleContext().getBundle(bundleId);
    }

    /**
     * Is the supplied bundle a framework extension bundle?
     *
     * @param bundle
     * @return
     */
    private boolean isFrameworkExtensionBundle(Bundle bundle) {
        if (isFragment(bundle)) {
            // Since Fragment-Host can use a framework specific symbolic name of the system bundle, we can't
            // assume that user has used "system.bundle." So, we check for the directive "extension:=framework"
            final String fragmentHost = bundle.getHeaders().get(org.osgi.framework.Constants.FRAGMENT_HOST);
            final String separator = ";";
            for (String s : fragmentHost.split(separator)) {
                int idx = s.indexOf(":=");
                if (idx != -1) {
                    String directiveName = s.substring(0, idx).trim();
                    if (directiveName.equals("extension") && s.substring(idx + 2).trim().equals("framework")) {
                        LOG.logp(Level.FINE, "BundleProvisioner", "isSystemBundleFragment",
                            "{0} is a framework extension bundle", bundle);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Is this a fragment bundle?
     *
     * @param bundle
     * @return
     */
    private boolean isFragment(Bundle bundle) {
        return bundleContext.isFragment(bundle);
    }

    private int install(Collection<Jar> jars) {
        for (Jar jar : jars) {
            try (InputStream is = jar.getURI().toURL().openStream()) {
                Bundle bundle = getBundleContext().installBundle(makeLocation(jar), is);
                Integer jarStartLevel = getStartLevel(jar);
                // if specified, set it
                if (jarStartLevel != null) {
                    bundleContext.setInitialBundleStartLevel(jarStartLevel);
                }
                noOfInstalledBundles++;
                addBundle(new Jar(bundle));
                LOG.logp(Level.FINE, "BundleProvisioner", "install", "Installed bundle {0} from {1} ",
                    new Object[] {bundle.getBundleId(), jar.getURI()});
            } catch (Exception e) {
                LogFacade.log(LOG, Level.WARNING, LogFacade.INSTALL_FAILED, e, jar.getURI());
            }
        }
        return noOfInstalledBundles;
    }

    private String makeLocation(Jar jar) {
        return customizer.makeLocation(jar);
    }

    /**
     * Refresh packages
     */
    public void refresh() {
        bundleContext.refresh();
    }

    /**
     * @return true if anything changed since last time framework was initialized
     */
    public boolean hasAnyThingChanged() {
        return getNoOfInstalledBundles() + getNoOfUninstalledBundles() + getNoOfUpdatedBundles() > 0;
    }

    /**
     * @return true if system bundle needs to be updated because of bundles getting updated or deleted or installed.
     */
    public boolean isSystemBundleUpdationRequired() {
        return systemBundleUpdationRequired;
    }

    protected void setSystemBundleUpdationRequired(boolean systemBundleUpdationRequired) {
        this.systemBundleUpdationRequired = systemBundleUpdationRequired;
    }

    /**
     * @return no of bundles uninstalled
     */
    public int getNoOfUninstalledBundles() {
        return noOfUninstalledBundles;
    }

    /**
     * @return no of bundles updated
     */
    public int getNoOfUpdatedBundles() {
        return noOfUpdatedBundles;
    }

    /**
     * @return no of bundles installed
     */
    public int getNoOfInstalledBundles() {
        return noOfInstalledBundles;
    }

    public BundleProvisionerCustomizer getCustomizer() {
        return customizer;
    }
}

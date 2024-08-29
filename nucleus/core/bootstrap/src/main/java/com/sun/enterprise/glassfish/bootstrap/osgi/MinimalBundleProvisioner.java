/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * This is a specialized {@link BundleProvisioner} that installs only a minimum set of of bundles.
 * It derives the set of bundles to be included from the list of bundles to be started and all fragment bundles
 * available in the installation locations.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
class MinimalBundleProvisioner extends BundleProvisioner {
    private final Logger logger = LogFacade.BOOTSTRAP_LOGGER;
    private List<Long> installedBundleIds;

    MinimalBundleProvisioner(BundleContext bundleContext, Properties config) {
        super(bundleContext, new MinimalBundleProvisionerCustomizer(config));
    }

    @Override
    public List<Long> installBundles() {
        BundleContext bctx = getBundleContext();
        final int n = bctx.getBundles().length;
        List<Long> bundleIds;
        if (n > 1) {
            // This is not the first run of the program, so don't do anything
            logger.logp(Level.FINE, "MinimalBundleProvisioner", "installBundles",
                    "Skipping installation of bundles as there are already {0} no. of bundles.", new Object[]{n});
            bundleIds = Collections.emptyList();
        } else {
            bundleIds = super.installBundles();
        }
        return installedBundleIds = bundleIds;
    }

    @Override
    public void startBundles() {
        if (installedBundleIds.isEmpty()) {
            logger.log(Level.INFO, LogFacade.SKIP_STARTING_ALREADY_PROVISIONED_BUNDLES);
        } else {
            super.startBundles();
        }
    }

    @Override
    public boolean hasAnyThingChanged() {
        long latestBundleTimestamp = -1;
        Bundle latestBundle = null;
        for (Bundle bundle : getBundleContext().getBundles()) {
            if (bundle.getLastModified() > latestBundleTimestamp) {
                latestBundleTimestamp = bundle.getLastModified();
                latestBundle = bundle;
            }
        }
        Jar latestJar = getCustomizer().getLatestJar();
        final boolean chnaged = latestJar.getLastModified() > latestBundle.getLastModified();
        logger.log(Level.INFO, LogFacade.LATEST_FILE_IN_INSTALL_LOCATION,
                new Object[]{chnaged, latestJar.getURI(), latestBundle.getLocation()});
        return chnaged;
    }

    @Override
    public void refresh() {
        // uninstall everything and start afresh
        for (Bundle b : getBundleContext().getBundles()) {
            // TODO(Sahoo): We should call getCustomizer().isManaged(new Jar(b)),
            // but obr gives us the ability to encode information in url
            if (b.getBundleId() != 0) {
                try {
                    b.uninstall();
                } catch (BundleException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        installBundles();
        super.refresh();
        setSystemBundleUpdationRequired(true);
    }

    @Override
    public MinimalBundleProvisionerCustomizer getCustomizer() {
        return (MinimalBundleProvisionerCustomizer) super.getCustomizer();
    }
}

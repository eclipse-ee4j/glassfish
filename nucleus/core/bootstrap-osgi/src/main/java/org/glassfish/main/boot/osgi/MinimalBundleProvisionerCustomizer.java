/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.boot.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.boot.log.LogFacade;
import org.osgi.framework.Constants;

class MinimalBundleProvisionerCustomizer extends DefaultBundleProvisionerCustomizer {
    private final Logger logger = LogFacade.BOOTSTRAP_LOGGER;

    MinimalBundleProvisionerCustomizer(Properties config) {
        super(config);
    }

    public Jar getLatestJar() {
        File latestFile = null;
        for (URI uri : getConfiguredAutoInstallLocations()) {
            File file = null;
            try {
                file = new File(uri);
            } catch (Exception e) {
                continue; // not a file, skip to next one
            }
            if (latestFile == null) {
                latestFile = file;
            }
            if (file.lastModified() > latestFile.lastModified()) {
                latestFile = file;
            }
            if (file.isDirectory()) {
                // do only one-level search as configured auto install locations are not recursive.
                for (File child : file.listFiles()) {
                    if (child.lastModified() > latestFile.lastModified()) {
                        latestFile = child;
                    }
                }
            }
        }
        return latestFile != null ? new Jar(latestFile) : null;
    }

    @Override
    public List<URI> getAutoInstallLocations() {
        // We only install those bundles that are required to be started  or those bundles that are fragments
        List<URI> installLocations = getAutoStartLocations();
        List<URI> fragments = selectFragmentJars(super.getAutoInstallLocations());
        installLocations.addAll(fragments);
        logger.log(Level.INFO, LogFacade.SHOW_INSTALL_LOCATIONS, new Object[]{installLocations});
        return installLocations;
    }

    private List<URI> selectFragmentJars(List<URI> installLocations) {
        List<URI> fragments = new ArrayList<>();
        for (URI uri : installLocations) {
            InputStream is = null;
            JarInputStream jis = null;
            try {
                is = uri.toURL().openStream();
                jis = new JarInputStream(is);
                Manifest m = jis.getManifest();
                if (m != null && m.getMainAttributes().getValue(Constants.FRAGMENT_HOST) != null) {
                    logger.logp(Level.FINE, "MinimalBundleProvisioner$MinimalCustomizer", "selectFragmentJars",
                            "{0} is a fragment", new Object[]{uri});
                    fragments.add(uri);
                }
            } catch (IOException e) {
                LogFacade.log(logger, Level.INFO, LogFacade.CANT_TELL_IF_FRAGMENT, e, uri);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (jis != null) {
                        jis.close();
                    }
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
        return fragments;
    }
}
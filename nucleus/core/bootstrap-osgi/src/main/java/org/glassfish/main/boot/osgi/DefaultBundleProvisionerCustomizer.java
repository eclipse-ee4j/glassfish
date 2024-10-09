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

import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_INSTALL_PROP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_START_LEVEL_PROP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_START_OPTIONS_PROP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_START_PROP;
import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.FILE_SCHEME;

/**
 * This is default implementation of {@link BundleProvisionerCustomizer} which uses the URI of the
 * Jar as location. The default customizer uses following configuration properties:
 * <p>
 * glassfish.osgi.auto.install:
 * <p>
 * This contains white space delimited list of absolute URIs to be installed.
 * If a URI represents a directory location, then it will be treated as if all the jar files from
 * that directory location (non-recursive) were specified instead.
 * Bundles will be installed in the same order they are specified.
 * <p>
 * glassfish.osgi.auto.start:
 * This contains white space delimited list of absolute URIs to be started.
 * If a URI represents a directory location, then it will be treated as if all the jar files from
 * that directory location (non-recursive) were specified instead. Although bundles will be started
 * in the order they are specified in this list, their eventual activation order depends on start
 * level and/or activation policy.
 * <p>
 * glassfish.osgi.auto.start.level.N, where N is > 0
 * This contains list of bundle URIs to be started at start level N.
 * <p>
 * glassfish.osgi.auto.start.option:
 * This specifies the options used to start bundles. It is an integer and must confirm to the format
 * accepted by {@link Bundle#start(int)} method. The default is to use
 * {@link Bundle#START_ACTIVATION_POLICY}. It also means bundles will be started persistently
 * by default. To start transiently, one has to specify explicitly.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
class DefaultBundleProvisionerCustomizer implements BundleProvisionerCustomizer {

    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;
    private final Properties config;

    /** Maps URI to start level */
    private final Map<URI, Integer> startLevels = new HashMap<>();
    private List<URI> autoInstallLocations;
    private List<URI> autoStartLocations;
    private List<URI> configuredAutoInstallLocations;

    DefaultBundleProvisionerCustomizer(Properties config) {
        this.config = config;
        // for optimization reasons, process these properties once and store their values
        processAutoInstallLocations();
        LOG.log(Level.CONFIG, () -> "autoInstallLocations:\n" + autoInstallLocations);
        processAutoStartLocations();
        LOG.log(Level.CONFIG, () -> "autoStartLocations:\n" + autoStartLocations);
        processStartLevels();
        LOG.log(Level.CONFIG, () -> "configuredAutoInstallLocations:\n" + configuredAutoInstallLocations);
    }


    private void processAutoInstallLocations() {
        String list = config.getProperty(AUTO_INSTALL_PROP);
        configuredAutoInstallLocations = getLocations(list, false);
        autoInstallLocations = getLocations(list);
    }


    private void processAutoStartLocations() {
        String list = config.getProperty(AUTO_START_PROP);
        autoStartLocations = getLocations(list);
    }


    private void processStartLevels() {
        for (String key : config.stringPropertyNames()) {
            if (key.startsWith(AUTO_START_LEVEL_PROP)) {
                try {
                    Integer startLevel = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
                    String list = config.getProperty(key);
                    for (URI uri : getLocations(list)) {
                        if (startLevels.containsKey(uri)) {
                            LOG.log(Level.WARNING, LogFacade.CANT_SET_START_LEVEL,
                                new Object[] {uri, startLevels.get(uri), startLevel});

                        } else {
                            startLevels.put(uri, startLevel);
                        }
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid property: " + key);
                }

            }
        }
    }


    @Override
    public List<URI> getAutoInstallLocations() {
        return autoInstallLocations;
    }


    @Override
    public List<URI> getAutoStartLocations() {
        return autoStartLocations;
    }


    private List<URI> getLocations(String list) {
        return getLocations(list, true);
    }


    private List<URI> getLocations(String list, boolean expand) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<URI> uris = new ArrayList<>();
        for (String uriString : list.split("\\s")) {
            try {
                URI uri = new URI(uriString);
                if (!uri.isAbsolute()) {
                    LOG.log(Level.WARNING, LogFacade.ENTRY_SKIPPED, uri);
                    continue;
                }
                if (expand && isDirectory(uri)) {
                    uris.addAll(listJarFiles(uri));
                } else {
                    uris.add(uri);
                }
            } catch (URISyntaxException e) {
                LogFacade.log(LOG, Level.WARNING, LogFacade.ENTRY_SKIPPED_DUE_TO, e, uriString);
            }
        }
        return uris;
    }


    @Override
    public int getStartOptions() {
        String autostart = config.getProperty(AUTO_START_OPTIONS_PROP);
        return autostart == null ? Bundle.START_ACTIVATION_POLICY : Integer.parseInt(autostart);
    }


    @Override
    public String makeLocation(Jar jar) {
        return jar.getURI().toString();
    }


    /**
     * Is this URI a directory?
     *
     * @param uri URI to be checked
     * @return true if this URI represents a directory, else false.
     */
    protected boolean isDirectory(URI uri) {
        try {
            return new File(uri).isDirectory();
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * @param aDirectoryURI uri corresponding to a directory location which will be searched.
     * @return URIs corresponding to jar files in a given directory location.
     */
    protected List<? extends URI> listJarFiles(URI aDirectoryURI) {
        // currently we only support file type directory URI.
        // In future, we should be able to handle directories inside jar files as well.
        if (!FILE_SCHEME.equalsIgnoreCase(aDirectoryURI.getScheme())) {
            throw new IllegalStateException("Currently we only support file URI scheme.");
        }
        final FileFilter filter = path -> (path.getName().endsWith(".jar") && !path.isDirectory());
        return Stream.of(new File(aDirectoryURI).listFiles(filter)).map(File::toURI).collect(Collectors.toList());
    }


    @Override
    public boolean isManaged(Jar jar) {
        URI uri = jar.getURI();
        if (uri == null) {
            // jar.getURI is null means we could not parse the location
            // as a meaningful URI. We can't do any meaningful processing for this bundle.
            return false;
        }
        for (URI configuredLocation : getConfiguredAutoInstallLocations()) {
            final String otherLocationAsString = configuredLocation.toString();
            if (uri.toString().regionMatches(0, otherLocationAsString, 0, otherLocationAsString.length())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public Integer getStartLevel(Jar jar) {
        return startLevels.get(jar.getURI());
    }


    protected List<URI> getConfiguredAutoInstallLocations() {
        return configuredAutoInstallLocations;
    }
}

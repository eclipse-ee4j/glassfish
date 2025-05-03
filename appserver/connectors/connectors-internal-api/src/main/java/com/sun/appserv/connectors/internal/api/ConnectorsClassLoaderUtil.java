/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.Events;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Service;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.belongsToSystemRA;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static java.util.logging.Level.WARNING;
import static org.glassfish.api.event.EventTypes.PREPARE_SHUTDOWN;


/**
 * Classloader util to create a new classloader for the provided .rar deploy directory.
 *
 * @author Jagadish Ramu
 */
@Service
@Singleton
public class ConnectorsClassLoaderUtil {

    private static final Logger LOG = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);

    @Inject
    private ClassLoaderHierarchy classLoaderHierarchy;

    @Inject
    private ServerEnvironment serverEnvironment;

    @Inject
    private ProcessEnvironment processEnvironment;

    @Inject
    private Events events;


    private volatile boolean rarsInitializedInEmbeddedServerMode;

    public ConnectorClassFinder createRARClassLoader(String moduleDir, ClassLoader deploymentParent, String moduleName, List<URI> appLibs) throws ConnectorRuntimeException {

        ClassLoader parent = null;

        // For standalone rar :
        // this is not a normal application and hence cannot use the provided parent during deployment.
        // setting the parent to connector-class-loader's parent (common class-loader) as this is a .rar
        //
        // For embedded rar :
        // use the deploymentParent as the class-finder created won't be part of connector class loader
        // service hierarchy
        if (deploymentParent == null) {
            parent = classLoaderHierarchy.getCommonClassLoader();
        } else {
            parent = deploymentParent;
        }

        return createRARClassLoader(parent, moduleDir, moduleName, appLibs);
    }

    private DelegatingClassLoader.ClassFinder getLibrariesClassLoader(final List<URI> appLibs) throws ConnectorRuntimeException {
        try {
            return classLoaderHierarchy.getAppLibClassFinder(appLibs);
        } catch (MalformedURLException e) {
            throw new ConnectorRuntimeException("Failed to create libraries classloader", e);
        }
    }

    private ConnectorClassFinder createRARClassLoader(final ClassLoader parent, String moduleDir,
        final String moduleName, List<URI> appLibs) throws ConnectorRuntimeException {
        ConnectorClassFinder connectorClassFinder = null;

        try {
            final DelegatingClassLoader.ClassFinder librariesCL = getLibrariesClassLoader(appLibs);
            final ConnectorClassFinder ccf = new ConnectorClassFinder(parent, moduleName, librariesCL);

            if (processEnvironment.getProcessType().isEmbedded()) {
                events.register(event -> {
                    if (event.is(PREPARE_SHUTDOWN)) {
                        try {
                            ccf.close();
                        } catch (IOException ioe) {
                            LOG.log(WARNING, "Could not close the " + ccf, ioe);
                        }
                    }
                });
            }

            connectorClassFinder = ccf;
        } catch (Exception ex) {
            throw new ConnectorRuntimeException("Failed to create connector classloader", ex);
        }

        File file = new File(moduleDir);
        try {
            connectorClassFinder.appendURL(file.toURI().toURL());
            appendJars(file, connectorClassFinder);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return connectorClassFinder;
    }

    private boolean extractRar(String rarName, String destination) {
        String rarFileName = rarName + ConnectorConstants.RAR_EXTENSION;
        return ConnectorsUtil.extractRar(destination + rarFileName, rarFileName, destination);
    }

    public Collection<ConnectorClassFinder> getSystemRARClassLoaders() throws ConnectorRuntimeException {
        if (processEnvironment.getProcessType().isEmbedded() && !rarsInitializedInEmbeddedServerMode) {
            synchronized (ConnectorsClassLoaderUtil.class) {
                if (!rarsInitializedInEmbeddedServerMode) {
                    String installDir = System.getProperty(INSTALL_ROOT_PROPERTY) + File.separator;
                    for (String jdbcRarName : ConnectorConstants.jdbcSystemRarNames) {
                        String rarPath = ConnectorsUtil.getSystemModuleLocation(jdbcRarName);
                        File rarDir = new File(rarPath);
                        if (!rarDir.exists()) {
                            extractRar(jdbcRarName, installDir);
                        }
                    }
                    rarsInitializedInEmbeddedServerMode = true;
                }
            }
        }

        List<ConnectorClassFinder> classLoaders = new ArrayList<>();
        for (String rarName : ConnectorsUtil.getSystemRARs()) {
            String location = ConnectorsUtil.getSystemModuleLocation(rarName);
            List<URI> libraries;
            if (processEnvironment.getProcessType().isEmbedded()) {
                libraries = new ArrayList<>();
            } else {
                libraries = ConnectorsUtil.getInstalledLibrariesFromManifest(location, serverEnvironment);
            }
            ConnectorClassFinder ccf = createRARClassLoader(location, null, rarName, libraries);
            classLoaders.add(ccf);
        }

        return classLoaders;
    }


    public ConnectorClassFinder getSystemRARClassLoader(String rarName) throws ConnectorRuntimeException {
        if (belongsToSystemRA(rarName)) {
            DelegatingClassLoader connectorClassLoader = classLoaderHierarchy.getConnectorClassLoader(null);

            for (DelegatingClassLoader.ClassFinder classFinder : connectorClassLoader.getDelegates()) {
                if (classFinder instanceof ConnectorClassFinder connectorClassFinder) {
                    if (rarName.equals(connectorClassFinder.getResourceAdapterName())) {
                        return (ConnectorClassFinder) classFinder;
                    }
                }
            }
        }

        throw new ConnectorRuntimeException("No Classloader found for RA [ " + rarName + " ]");
    }

    private void appendJars(File moduleDir, ASURLClassLoader cl) throws MalformedURLException {
        // TODO for embedded rars -consider MANIFEST.MF's classpath attribute
        if (moduleDir.isDirectory()) {
            File[] list = moduleDir.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (file.getName().toUpperCase(Locale.getDefault()).endsWith(".JAR")) {
                        cl.appendURL(file.toURI().toURL());
                    } else if (file.isDirectory()) {
                        appendJars(file, cl); // recursive add
                    }
                }
            }
        }
    }
}

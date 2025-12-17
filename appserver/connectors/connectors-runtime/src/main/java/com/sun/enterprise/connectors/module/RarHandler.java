/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsClassLoaderUtil;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveDetector;
import org.glassfish.api.deployment.archive.RarArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINEST;
import static org.glassfish.loader.util.ASClassLoaderUtil.getLibDirectoryJarURIs;

/**
 * Archive handler for resource-adapters
 *
 * @author Jagadish Ramu
 */
@Service(name = RarArchiveType.ARCHIVE_TYPE)
public class RarHandler extends AbstractArchiveHandler {
    // This class should be moved to connector runtime along with ConnectorClassLoaderUtil.
    // We should also consider merging connectors-connector with connectors-internal-api

    private final Logger _logger = LogDomains.getLogger(RarHandler.class, LogDomains.RSR_LOGGER);

    @Inject
    private ConnectorsClassLoaderUtil loader;

    @Inject
    @Named(RarArchiveType.ARCHIVE_TYPE)
    private ArchiveDetector detector;

    @Override
    public String getArchiveType() {
        return RarArchiveType.ARCHIVE_TYPE;
    }

    @Override
    public boolean handles(ReadableArchive archive) throws IOException {
        return detector.handles(archive);
    }

    @Override
    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        try {
            String moduleDir = context.getSource().getURI().getPath();
            String moduleName = context.getSource().getName();

            List<URI> appLibs = null;
            try {
                appLibs = context.getAppLibs();
                if(_logger.isLoggable(FINEST)){
                    _logger.log(FINEST, "installed libraries (--applibs and EXTENSTION_LIST) for rar " +
                        "[ "+moduleName+" ] :  " + appLibs);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            ClassLoader carCL;
            if (isEmbedded(context)) {
                String applicationName = ConnectorsUtil.getApplicationName(context);
                String embeddedRarName = ConnectorsUtil.getEmbeddedRarModuleName(applicationName, moduleName);
                // ear's classloader hierarchy is : module-CL -> ear-CL (contains all ejb module classpath)
                // -> embedded-RAR-CL -> ear-lib-CL.
                // parent provided here is ear-CL, we need to use
                // ear-lib-CL as parent for embedded-RAR module-CL
                carCL = loader.createRARClassLoader(moduleDir, parent.getParent().getParent(), embeddedRarName, appLibs);
            } else {
                carCL = loader.createRARClassLoader(moduleDir, null, moduleName, appLibs);
            }

            return carCL;

        } catch (ConnectorRuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * indicates whether the .rar being deployed is standalone or embedded
     * @param context deployment context
     * @return boolean indicating whether its embedded .rar
     */
    private boolean isEmbedded(DeploymentContext context) {
        ReadableArchive archive = context.getSource();
        return archive != null && archive.getParentArchive() != null;
    }

    /**
     * Returns the classpath URIs for this archive.
     *
     * @param archive file
     * @return classpath URIs for this archive
     */
    @Override
    public List<URI> getClassPathURIs(ReadableArchive archive) {
        List<URI> uris = super.getClassPathURIs(archive);
        try {
            File archiveFile = new File(archive.getURI().getSchemeSpecificPart());
            if (archiveFile.exists() && archiveFile.isDirectory()) {
                // add top level jars
                uris.addAll(getLibDirectoryJarURIs(archiveFile));
            }
        } catch (Exception e) {
            _logger.log(Level.WARNING, e.getMessage(), e);
        }

        return uris;
    }
}

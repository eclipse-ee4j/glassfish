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

package org.glassfish.internal.deployment;

import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Pretty generic implementation of some ArchiveHandler methods
 *
 * @author Jerome Dochez
 */
public abstract class GenericHandler implements ArchiveHandler {

    @Inject
    protected ServiceLocator habitat;

    /**
     * Prepares the jar file to a format the ApplicationContainer is expecting.
     * This could be just a pure unzipping of the jar or nothing at all.
     *
     * @param source of the expanding
     * @param target of the expanding
     * @param context deployment context
     * @throws IOException when the archive is corrupted
     */
    @Override
    public void expand(ReadableArchive source, WritableArchive target, DeploymentContext context) throws IOException {
        Enumeration<String> e = source.entries();
        while (e.hasMoreElements()) {
            String entryName = e.nextElement();
            try (InputStream is = source.getEntry(entryName)) {
                if (is == null) {
                    continue;
                }
                try (WritableArchiveEntry output = target.putNextEntry(entryName)) {
                    FileUtils.copy(is, output);
                }
            }
        }

        // last is manifest if exists.
        Manifest manifest = source.getManifest();
        if (manifest != null) {
            try (WritableArchiveEntry output = target.putNextEntry(JarFile.MANIFEST_NAME)) {
                manifest.write(output);
            }
        }
    }

    /**
     * Returns the default application name usable for identifying the archive.
     * <p>
     * This default implementation returns the name portion of
     * the archive's URI.  The archive's name depends on the type of archive
     * (FileArchive vs. JarArchive vs. MemoryMappedArchive, for example).
     * <p>
     * A concrete subclass can override this method to provide an alternative
     * way of deriving the default application name.
     *
     * @param archive the archive for which the default name is needed
     * @param context deployment context
     * @return the default application name for the specified archive
     */
    @Override
    public String getDefaultApplicationName(ReadableArchive archive,
        DeploymentContext context) {
        // first try to get the name from ApplicationInfoProvider if
        // we can find an implementation of this service
        ApplicationInfoProvider nameProvider = habitat.getService(ApplicationInfoProvider.class);

        DeploymentTracing tracing = null;

        if (context != null) {
            tracing = context.getModuleMetaData(DeploymentTracing.class);
        }

        if (tracing != null) {
            tracing.addMark(DeploymentTracing.Mark.APPINFO_PROVIDED);
        }


        String appName = null;
        if (nameProvider != null) {
            appName = nameProvider.getNameFor(archive, context);
            if (appName != null) {
                return appName;
            }
        }

        // now try to get the default
        return getDefaultApplicationNameFromArchiveName(archive);
    }

    public String getDefaultApplicationNameFromArchiveName(ReadableArchive archive) {
        String appName = archive.getName();
        int lastDot = appName.lastIndexOf('.');
        if (lastDot != -1) {
            if (appName.substring(lastDot).equalsIgnoreCase("." + getArchiveType())) {
                appName = appName.substring(0, lastDot);
            }
        }
        return appName;
    }

    @Override
    public String getDefaultApplicationName(ReadableArchive archive) {
        return getDefaultApplicationName(archive, null);
    }

    /**
     * Returns the default value for versionIdentifier. This allows us to
     * override the method only where thhe version-identifier element is
     * supported.
     *
     * @return null
     */
    @Override
    public String getVersionIdentifier(ReadableArchive archive){
        return null;
    }

    /**
     * Returns the manifest file for this archive, this file is usually located at
     * the META-INF/MANIFEST location, however, certain archive type can change this
     * default location or use another mean of expressing manifest information.
     *
     * @param archive file
     * @return manifest instance or null if this archive has no manifest
     */
    @Override
    public Manifest getManifest(ReadableArchive archive) throws IOException {
        return archive.getManifest();
    }

    /**
     * Returns the classpath URIs for this archive.
     *
     * @param archive file
     * @return classpath URIs for this archive
     */
    @Override
    public List<URI> getClassPathURIs(ReadableArchive archive) {
        List<URI> uris = new ArrayList<>();
        // add the archive itself
        uris.add(archive.getURI());
        return uris;
    }

    /**
     * Returns whether this archive requires annotation scanning.
     *
     * @param archive file
     * @return whether this archive requires annotation scanning
     */
    @Override
    public boolean requiresAnnotationScanning(ReadableArchive archive) {
        return true;
    }
}

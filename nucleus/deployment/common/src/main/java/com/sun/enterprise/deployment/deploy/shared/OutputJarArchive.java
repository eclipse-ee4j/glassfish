/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.deploy.shared;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Provides an implementation of the Archive that maps to
 * a Jar file @see java.util.jar.JarFile
 *
 * @author Jerome Dochez
 */
@Service(name="jar")
@PerLookup
public class OutputJarArchive extends JarArchive implements WritableArchive {

    // the path
    private URI uri;

    // the file we are currently mapped to (if open for writing)
    private ZipOutputStream jos;

    private Manifest manifest;

    // list of entries already written to this ouput
    private final Vector<String> entries = new Vector<>();

    /**
     * Get the size of the archive
     *
     * @return -1 because this is getting created
     */
    @Override
    public long getArchiveSize() throws NullPointerException, SecurityException {
        return -1;
    }

    /**
     * close the abstract archive
     */
    @Override
    public void close() throws IOException {
        if (jos != null) {
            jos.close();
            jos = null;
        }
    }

    @Override
    protected JarEntry getJarEntry(String entryName) {
        return null;
    }

    /**
     * creates a new abstract archive with the given path
     *
     * @param path the path to create the archive
     */
    @Override
    public void create(URI path) throws IOException {
        this.uri = path;
        File file = new File(uri.getSchemeSpecificPart());
        // if teh file exists, we delete it first
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Error in deleting file " + file.getAbsolutePath());
            }
        }
        jos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    }

    /**
     * @return an @see java.util.Enumeration of entries in this abstract
     *         archive
     */
    @Override
    public Enumeration<String> entries() {
        return entries.elements();
    }

    @Override
    public Collection<String> getDirectories() throws IOException {
        return new Vector<>();
    }


    /**
     * @return the manifest information for this abstract archive
     */
    @Override
    public Manifest getManifest() throws IOException {
        if (manifest == null) {
            manifest = new Manifest();
        }
        return manifest;
    }

    /**
     * Returns the path used to create or open the underlyong archive
     *
     * @return the path for this archive.
     */
    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public WritableArchive createSubArchive(String name) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(putNextEntry(name));
        OutputJarArchive jar = new OutputJarArchive();
        try {
            jar.uri = new URI("jar", name, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not create a subarchive for name " + name, e);
        }
        jar.jos = zip;
        return jar;
    }

    /**
     * Close a previously returned sub archive
     *
     * @param subArchive output stream to close
     * @see WritableArchive#createSubArchive(String)
     */
    @Override
    public void closeEntry(WritableArchive subArchive) throws IOException {
        if (subArchive instanceof OutputJarArchive) {
            ((OutputJarArchive) subArchive).jos.flush();
            ((OutputJarArchive) subArchive).jos.finish();
        }
        jos.closeEntry();
    }


    /**
     * @param name the entry name
     * @returns an @see java.io.OutputStream for a new entry in this
     * current abstract archive.
     */
    @Override
    public WritableArchiveEntry putNextEntry(String name) throws java.io.IOException {
        if (jos != null) {
            ZipEntry ze = new ZipEntry(name);
            jos.putNextEntry(ze);
            entries.add(name);
        }
        return new WritableArchiveEntry(jos, jos);
    }
}

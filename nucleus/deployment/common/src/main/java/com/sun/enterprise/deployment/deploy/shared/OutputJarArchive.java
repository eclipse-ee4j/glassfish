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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
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
    private static final Logger LOG = System.getLogger(OutputJarArchive.class.getName());

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
            LOG.log(Level.DEBUG, "close()");
            jos.close();
            jos = null;
        }
    }

    @Override
    protected JarEntry getJarEntry(String entryName) {
        return null;
    }

    @Override
    public void create(URI path) throws IOException {
        LOG.log(Level.DEBUG, "create(path={0})", path);
        this.uri = path;
        File file = new File(uri.getSchemeSpecificPart());
        if (file.exists() && !file.delete()) {
            LOG.log(Level.WARNING, "Could not delete the file {0}", file);
        }
        jos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
    }

    @Override
    public Enumeration<String> entries() {
        return entries.elements();
    }

    @Override
    public Collection<String> getDirectories() throws IOException {
        return new Vector<>();
    }


    @Override
    public Manifest getManifest() throws IOException {
        if (manifest == null) {
            manifest = new Manifest();
        }
        return manifest;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public WritableArchive createSubArchive(String name) throws IOException {
        LOG.log(Level.DEBUG, "createSubArchive(name={0})", name);
        checkOpen("Could not create subarchive {0}, because the output archive {1} is already closed.", name, getName());
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


    @Override
    public WritableArchiveEntry putNextEntry(String name) throws java.io.IOException {
        LOG.log(Level.DEBUG, "putNextEntry(name={0})", name);
        checkOpen("Could not put next entry {0}, because the output archive {1} is already closed.", name, getName());
        ZipEntry ze = new ZipEntry(name);
        jos.putNextEntry(ze);
        entries.add(name);
        return new WritableArchiveEntry(this::getZipOutputStream, this::closeEntry);
    }


    private void closeEntry() throws IOException {
        LOG.log(Level.DEBUG, "closeEntry({0})", this.entries.get(this.entries.size() - 1));
        checkOpen("The zip output stream to {0} is already closed.", getName());
        jos.flush();
        jos.closeEntry();
    }


    private ZipOutputStream getZipOutputStream() {
        checkOpen("The zip output stream to {0} is already closed.", getName());
        return jos;
    }


    private void checkOpen(String message, Object... args) {
        if (jos == null) {
            throw new IllegalStateException(MessageFormat.format(message, args));
        }
    }
}

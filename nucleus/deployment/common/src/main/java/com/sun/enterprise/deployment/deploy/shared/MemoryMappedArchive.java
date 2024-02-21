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

import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author  Jerome Dochez 2002
 */
@Service
@PerLookup
public class MemoryMappedArchive extends JarArchive implements ReadableArchive {
    private static final Logger LOG = System.getLogger(MemoryMappedArchive.class.getName());

    private URI uri;
    private byte[] file;


    /** Creates a new instance of MemoryMappedArchive */
    protected MemoryMappedArchive() {
        // for use by subclasses
    }

    /** Creates a new instance of MemoryMappedArchive */
    public MemoryMappedArchive(InputStream is) throws IOException {
        read(is);
    }

    public MemoryMappedArchive(byte[] bits) {
        file = bits;
    }

    // copy constructor
    public MemoryMappedArchive(ReadableArchive source) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JarOutputStream jos = new JarOutputStream(baos)) {
            for (Enumeration<String> elements = source.entries(); elements.hasMoreElements();) {
                String elementName = elements.nextElement();
                try (InputStream is = source.getEntry(elementName)) {
                    jos.putNextEntry(new ZipEntry(elementName));
                    FileUtils.copy(is, jos);
                } finally {
                    jos.flush();
                    jos.closeEntry();
                }
            }
        }
        file = baos.toByteArray();
    }

    public byte[] getByteArray() {
        return file;
    }

    private void read(InputStream is) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtils.copy(is, baos);
        file = baos.toByteArray();
    }

    @Override
    public void open(URI uri) throws IOException {
        File in = new File(uri);
        if (!in.exists()) {
            throw new FileNotFoundException(uri.getSchemeSpecificPart());
        }
        try (FileInputStream is = new FileInputStream(in)) {
            read(is);
        }
    }

    /**
     * close the abstract archive
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * delete the archive
     */
    @Override
    public boolean delete() {
        return false;
    }

    /**
     * @return an @see java.util.Enumeration of entries in this abstract
     * archive
     */
    @Override
    public Enumeration entries() {
        return entries(false).elements();
    }


    @Override
    public Collection<String> getDirectories() throws IOException {
        return entries(true);
    }

    private Vector<String> entries(boolean directory) {
        Vector<String> entries = new Vector<>();
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(file))) {
            ZipEntry ze;
            while ((ze=jis.getNextEntry())!=null) {
                if (ze.isDirectory()==directory) {
                    entries.add(ze.getName());
                }
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, ioe.getMessage(), ioe);
        }
        return entries;
    }


    /**
     * @return an @see java.util.Enumeration of entries in this abstract
     *         archive, providing the list of embedded archive to not count their
     *         entries as part of this archive
     */
    public Enumeration entries(Enumeration embeddedArchives) {
        // jar file are not recursive
        return entries();
    }


    /**
     * @return true if this archive exists
     */
    @Override
    public boolean exists() {
        return false;
    }

    /**
     * @return the archive uri
     */
    public String getPath() {
        return null;
    }

    /**
     * Get the size of the archive
     * @return tje the size of this archive or -1 on error
     */
    @Override
    public long getArchiveSize() throws NullPointerException, SecurityException {
        return file.length;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    public void setURI(final URI uri) {
        this.uri = uri;
    }

    /**
     * create or obtain an embedded archive within this abstraction.
     *
     * @param name the name of the embedded archive.
     */
    @Override
    public ReadableArchive getSubArchive(String name) throws IOException {
        try (InputStream is = getEntry(name)) {
            if (is != null) {
                ReadableArchive archive = new MemoryMappedArchive(is);
                return archive;
            }
        }
        return null;
    }

    /**
     * Returns the existence of the given entry name
     * The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module.
     * @return the existence the given entry name.
     */
    @Override
    public boolean exists(String name) throws IOException {
        return (getEntry(name) != null);
    }


    /**
     * @param name the entry name
     * @return an {@link InputStream} for an existing entry in the current abstract archive.
     *         The caller is responsible for closing. Can be null.
     */
    @Override
    public InputStream getEntry(String name) throws IOException {
        JarInputStream jis = new JarInputStream(new ByteArrayInputStream(file));
        ZipEntry ze;
        while ((ze = jis.getNextEntry()) != null) {
            if (ze.getName().equals(name)) {
                return new BufferedInputStream(jis);
            }
        }
        return null;
    }

    @Override
    public JarEntry getJarEntry(String name) {
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(file))) {
            JarEntry ze;
            while ((ze = jis.getNextJarEntry()) != null) {
                if (ze.getName().equals(name)) {
                    return ze;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Returns the entry size for a given entry name or 0 if not known
     *
     * @param name the entry name
     * @return the entry size
     */
    @Override
    public long getEntrySize(String name) {
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(file))) {
            ZipEntry ze;
            while ((ze=jis.getNextEntry())!=null) {
                if (ze.getName().equals(name)) {
                    return ze.getSize();
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Couldn't get entry size for " + name + " in " + file, e);
        }
        return 0;
    }

    /**
     * @return the manifest information for this abstract archive
     */
    @Override
    public Manifest getManifest() throws IOException {
        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(file))) {
            return jis.getManifest();
        }
    }

    /**
     * rename the archive
     *
     * @param name the archive name
     */
    @Override
    public boolean renameTo(String name) {
        return false;
    }

    /**
     * Returns the name for the archive.
     * <p>
     * For a MemoryMappedArhive there is no name, so an empty string is returned.
     * @return the name of the archive
     *
     */
    @Override
    public String getName() {
        return "";
    }
}

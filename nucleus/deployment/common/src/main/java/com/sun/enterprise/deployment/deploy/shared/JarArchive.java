/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;

import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * This abstract class contains all common implementation of the Archive/WritableArchive interfaces for Jar files
 *
 * @author Jerome Dochez
 */
public abstract class JarArchive implements Archive {

    protected ReadableArchive parentArchive;

    protected Map<Class<?>, Object> extraData = new HashMap<>();

    protected Map<String, Object> archiveMetaData = new HashMap<>();

    /**
     * Returns an enumeration of the module file entries with the specified prefix. All elements in the enumeration are of
     * type String. Each String represents a file name relative to the root of the module.
     *
     * @param prefix the prefix of entries to be included
     * @return an enumeration of the archive file entries.
     */
    @Override
    public Enumeration<String> entries(String prefix) {
        Enumeration<String> allEntries = entries();
        Vector<String> entries = new Vector<>();
        while (allEntries.hasMoreElements()) {
            String name = allEntries.nextElement();
            if (name != null && name.startsWith(prefix)) {
                entries.add(name);
            }
        }
        return entries.elements();
    }

    /**
     * Returns the name portion of the archive's URI.
     * <p>
     * For JarArhive the name is all of the path that follows the last slash up to but not including the last dot.
     * <p>
     * Here are some example archive names for the specified JarArchive paths:
     * <ul>
     * <li>/a/b/c/d.jar -> d
     * <li>/a/b/c/d -> d
     * <li>/x/y/z.html -> z
     * </ul>
     *
     * @return the name of the archive
     *
     */
    @Override
    public String getName() {
        return JarArchive.getName(getURI());
    }

    abstract protected JarEntry getJarEntry(String entryName);

    /**
     * Returns the existence of the given entry name The file name must be relative to the root of the module.
     *
     * @param name the file name relative to the root of the module. * @return the existence the given entry name.
     */
    public boolean exists(String name) throws IOException {
        return getJarEntry(name) != null;
    }

    /**
     * Returns true if the entry is a directory or a plain file
     *
     * @param name name is one of the entries returned by {@link #entries()}
     * @return true if the entry denoted by the passed name is a directory
     */
    @Override
    public boolean isDirectory(String name) {
        JarEntry entry = getJarEntry(name);
        if (entry == null) {
            throw new IllegalArgumentException(name);
        }
        return entry.isDirectory();
    }

    static String getName(URI uri) {
        String path = Util.getURIName(uri);
        int lastDot = path.lastIndexOf('.');
        int endOfName = lastDot == -1 ? path.length() : lastDot;
        return path.substring(0, endOfName);
    }

    /**
     * set the parent archive for this archive
     *
     * @param parentArchive the parent archive
     */
    public void setParentArchive(ReadableArchive parentArchive) {
        this.parentArchive = parentArchive;
    }

    /**
     * get the parent archive of this archive
     *
     * @return the parent archive
     */
    public ReadableArchive getParentArchive() {
        return parentArchive;
    }

    /**
     * Returns any data that could have been calculated as part of the descriptor loading.
     *
     * @param dataType the type of the extra data
     * @return the extra data or null if there are not an instance of type dataType registered.
     */
    public synchronized <U> U getExtraData(Class<U> dataType) {
        return dataType.cast(extraData.get(dataType));
    }

    public synchronized <U> void setExtraData(Class<U> dataType, U instance) {
        extraData.put(dataType, instance);
    }

    public synchronized <U> void removeExtraData(Class<U> dataType) {
        extraData.remove(dataType);
    }

    public void addArchiveMetaData(String metaDataKey, Object metaData) {
        if (metaData != null) {
            archiveMetaData.put(metaDataKey, metaData);
        }
    }

    public <T> T getArchiveMetaData(String metaDataKey, Class<T> metadataType) {
        Object metaData = archiveMetaData.get(metaDataKey);
        if (metaData != null) {
            return metadataType.cast(metaData);
        }
        return null;
    }

    public void removeArchiveMetaData(String metaDataKey) {
        archiveMetaData.remove(metaDataKey);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + "]";
    }
}

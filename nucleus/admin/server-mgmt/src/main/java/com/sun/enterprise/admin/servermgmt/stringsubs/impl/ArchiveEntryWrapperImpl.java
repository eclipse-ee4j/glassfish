/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.Archive;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.MemberEntry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;


/**
 * Handles the operation related with an archive string substitution process. i.e extracting the substitutable entries
 * from jar and rebuilding the jar after substitution operation.
 */
public class ArchiveEntryWrapperImpl implements ArchiveEntryWrapper {
    private static final Logger LOG = System.getLogger(ArchiveEntryWrapperImpl.class.getName());

    private static final LocalStringsImpl _strings = new LocalStringsImpl(ArchiveEntryWrapperImpl.class);

    // Prefix for the extraction directory.
    private static final String EXTRACTION_DIR_PREFIX = "ext";
    // A buffer for better jar performance during extraction.
    private static final byte[] _buffer = new byte[10000];

    private Archive _archive;
    private ArchiveEntryWrapperImpl _parent = null;
    private File _extractDir = null;
    private JarFile _jar = null;
    //List of all substitutable archive entries including the entries from nested archive.
    private List<ArchiveMember> _allArchiveMembers;
    // Extracted entries from an archive, doesn't include entry from nested archive.
    private Map<String, File> _extractedEntries = new HashMap<String, File>();
    //Substitutable entries count for an archive, doesn't include entry from nested archive.
    private AtomicInteger _noOfExtractedEntries = new AtomicInteger();

    /**
     * Construct an {@link ArchiveEntryWrapperImpl} for a given archive entry.
     *
     * @throws IOException If any IO error occurs.
     */

    ArchiveEntryWrapperImpl(Archive archive) throws IOException {
        this(archive, null, null);
    }

    /**
     * Construct an {@link ArchiveEntryWrapperImpl} for a given archive entry.
     *
     * @param archive An {@link Archive} entry.
     * @param archivePath Path where the archive resides. <code>null</code> if the archive name contains the path.
     * @throws IOException If any IO error occurs.
     */
    private ArchiveEntryWrapperImpl(Archive archive, String archivePath, ArchiveEntryWrapperImpl parent) throws IOException {
        _archive = archive;
        _jar = archivePath == null || archivePath.isEmpty() ? new JarFile(_archive.getName())
                : new JarFile(archivePath + _archive.getName());
        _parent = parent;
        // Create a directory to extract archive members.
        _extractDir = SubstitutionFileUtil.setupDir(EXTRACTION_DIR_PREFIX);
        extract();
    }

    @Override
    public ArchiveEntryWrapper getParentArchive() {
        return _parent;
    }

    @Override
    public List<? extends ArchiveMember> getSubstitutables() {
        return _allArchiveMembers == null ? new ArrayList<>(1) : _allArchiveMembers;
    }

    @Override
    public void notifyCompletion() {
        if (_noOfExtractedEntries.decrementAndGet() <= 0) {
            try {
                updateArchive();
                SubstitutionFileUtil.removeDir(_extractDir);
                if (_parent != null) {
                    _parent.notifyCompletion();
                }
            } catch (IOException e) {
                SubstitutionFileUtil.removeDir(_extractDir);
                LOG.log(WARNING, () -> "Failed to update jar " + _archive.getName() + " with the substitutable files", e);
            }
        }
    }

    /**
     * Extract all the substitutable entries for an archive. It also takes care of extracting substitutable entries from
     * nested archives.
     *
     * @throws IOException If any IO error occurs during extraction.
     */
    private void extract() throws IOException {
        for (Object object : _archive.getArchiveOrMemberEntry()) {
            String extratFilePath = _extractDir.getAbsolutePath() + File.separator;
            if (object instanceof Archive) {
                Archive archive = (Archive) object;
                File file = new File(extratFilePath + archive.getName());
                try {
                    extractEntry(archive.getName(), file);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                _extractedEntries.put(archive.getName(), file);
                new ArchiveEntryWrapperImpl(archive, extratFilePath, this);
                _noOfExtractedEntries.incrementAndGet();
            } else if (object instanceof MemberEntry) {
                MemberEntry entry = (MemberEntry) object;
                File file = new File(extratFilePath + entry.getName());
                try {
                    extractEntry(entry.getName(), file);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                _extractedEntries.put(entry.getName(), file);
                getAllArchiveMemberList().add(new ArchiveMemberHandler(file, this));
                _noOfExtractedEntries.incrementAndGet();
            } else {
                LOG.log(WARNING, () -> "File " + object + " not present inside archive " + _archive.getName() );
            }
        }
    }

    /**
     * Gets the list which stores all the substitutable entries of an archive.
     *
     * @return List storing the substitutable member entries.
     */
    private List<ArchiveMember> getAllArchiveMemberList() {
        ArchiveEntryWrapperImpl current = this;
        while (current._parent != null) {
            current = current._parent;
        }
        if (current._allArchiveMembers == null) {
            current._allArchiveMembers = new ArrayList<ArchiveMember>();
        }
        return current._allArchiveMembers;
    }

    /**
     * Extracts entry from jar into the given file.
     *
     * @param jarEntry The jar entry to be extracted.
     * @param file The File to extract the jar entry into.
     * @throws IOException if problem occurred in accessing the jar.
     * @throws IllegalArgumentException If entry is not present in the jar.
     */
    private void extractEntry(String name, File file) throws IOException, IllegalArgumentException {
        if (_jar == null) {
            throw new JarException("Jar file is closed.");
        }

        JarEntry jarEntry = _jar.getJarEntry(name);
        if (jarEntry == null) {
            String msg = _strings.get("invalidArchiveEntry", name, _jar.getName());
            throw new IllegalArgumentException(msg);
        }

        if (jarEntry.isDirectory() && !file.exists()) {
            if (!file.mkdirs()) {
                LOG.log(INFO, () -> "Could not create directory " +  file.getAbsolutePath());
            }
            return;
        }

        InputStream in = null;
        BufferedOutputStream outputStream = null;
        try {
            in = _jar.getInputStream(jarEntry);
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            int i = 0;
            while ((i = in.read(_buffer)) != -1) {
                outputStream.write(_buffer, 0, i);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    if (LOG.isLoggable(DEBUG)) {
                        LOG.log(DEBUG, _strings.get("errorInClosingStream", _jar.getName()), e);
                    }
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    if (LOG.isLoggable(DEBUG)) {
                        LOG.log(DEBUG, _strings.get("errorInClosingStream", file.getPath()), e);
                    }
                }
            }
        }
    }

    /**
     * Updates the jar with the extracted entries.
     *
     * @throws IOException If any error occurs during update process.
     */
    void updateArchive() throws IOException {
        if (_extractedEntries.isEmpty()) {
            LOG.log(DEBUG, () -> _strings.get("noArchiveEntryToUpdate", _archive.getName()));
            return;
        }
        if (_jar == null) {
            throw new JarException("Jar file is not in open state, jar path.");
        }
        boolean success = false;
        final File jarFile = new File(_jar.getName());
        final File tempJarFile = File.createTempFile("helper", ".jar", jarFile.getParentFile());
        try (FileOutputStream fos = new FileOutputStream(tempJarFile); JarOutputStream jos = new JarOutputStream(fos)) {
            InputStream is = null;
            Set<String> extractedJarEntries = _extractedEntries.keySet();

            for (Enumeration<JarEntry> e = _jar.entries(); e.hasMoreElements();) {
                String jarEntryName = e.nextElement().getName();
                if (extractedJarEntries.contains(jarEntryName)) {
                    File file = _extractedEntries.get(jarEntryName);
                    if (file != null) {
                        JarEntry entry = new JarEntry(jarEntryName);
                        is = new FileInputStream(file);
                        appendEntry(jos, entry, is);
                    }
                } else {
                    JarEntry je = _jar.getJarEntry(jarEntryName);
                    is = _jar.getInputStream(je);
                    appendEntry(jos, je, is);
                }
            }
            success = true;
        } catch (Exception e) {
            LOG.log(ERROR, "Error occurred while closing the stream for file " + _archive.getName(), e);
        } finally {
            try {
                _jar.close();
            } catch (IOException e) {
                if (LOG.isLoggable(DEBUG)) {
                    LOG.log(DEBUG, "Problem occurred while closing the jar file : " + jarFile.getPath(), e);
                }
            }

            if (!jarFile.delete()) {
                if (!tempJarFile.delete()) {
                    LOG.log(ERROR, "Error occurred while closing the stream for file {0}", tempJarFile.getAbsolutePath());
                }
                throw new IOException(jarFile.getPath() + " did not get updated. Unable to delete.");
            } else if (!success) {
                if (!tempJarFile.delete()) {
                    LOG.log(INFO, () -> _strings.get("errorInClosingStream", tempJarFile.getAbsolutePath()));
                }
            } else if (!tempJarFile.renameTo(jarFile)) {
                LOG.log(ERROR, () -> "Could not rename temporary jar " + tempJarFile.getName() + " file to " + jarFile.getName() );
            }
        }
    }

    /**
     * Adds the given entry into the jar and writes the entry data from the InputStream.
     *
     * @param jarEntry The jar entry we are writing as a JarEntry.
     * @param is The InputStream to read the jar entry data from.
     * @throws IOException if there are problems accessing the jar.
     */
    private void appendEntry(JarOutputStream jos, JarEntry jarEntry, InputStream is) throws IOException {
        jos.putNextEntry(jarEntry);
        if (!jarEntry.isDirectory()) {
            int i = 0;
            while ((i = is.read(_buffer)) != -1) {
                jos.write(_buffer, 0, i);
            }
            is.close();
            jos.flush();
        }
        jos.closeEntry();
    }
}

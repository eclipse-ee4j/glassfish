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

import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Factory class to get the files that has to undergo string substitution.
 */
class FileEntryFactory {

    private static final Logger LOG = System.getLogger(FileEntryFactory.class.getName());
    private static final LocalStringsImpl _strings = new LocalStringsImpl(FileEntryFactory.class);

    /**
     * Create the {@link List} of {@link FileSubstitutionHandler} by processing the file path. The path can point to a
     * single file\directory or can contain pattern or wild card characters. A {@link FileSubstitutionHandler} point to a
     * individual file eligible for String substitution process.
     *
     * @param fileEntry
     * @return List of matching substitutable entries.
     */
    @SuppressWarnings("unchecked")
    List<Substitutable> getFileElements(FileEntry fileEntry) {
        // TODO: Name attribute of file entry can not contain comma separated files.
        String pathEntries[] = fileEntry.getName().split(",");
        List<Substitutable> substituables = null;
        List<File> retrievedFiles = null;
        for (String pathEntry : pathEntries) {
            String isRegex = fileEntry.getRegex();
            if (Boolean.getBoolean(isRegex) || "yes".equalsIgnoreCase(isRegex)) {
                File file = new File(pathEntry);
                File parentDir = file.getParentFile();
                if (parentDir == null || !parentDir.exists()) {
                    continue;
                }
                retrievedFiles = new ArrayList<File>();
                String expression = file.getName();
                String[] fileList = parentDir.list();
                Pattern pattern = Pattern.compile(expression);
                if (fileList != null) {
                    for (String fileName : fileList) {
                        Matcher matcher = pattern.matcher(fileName);
                        if (matcher.matches()) {
                            File matchingFile = new File(parentDir, fileName);
                            if (matchingFile.exists() && matchingFile.canRead() && matchingFile.canWrite()) {
                                retrievedFiles.add(matchingFile);
                            } else {
                                if (LOG.isLoggable(DEBUG)) {
                                    LOG.log(DEBUG, _strings.get("skipFileFromSubstitution", matchingFile.getAbsolutePath()));
                                }
                            }
                        }
                    }
                }
            } else {
                FileLister fileLocator = new FileLister();
                retrievedFiles = fileLocator.getFiles(fileEntry.getName());
            }
            if (retrievedFiles.isEmpty()) {
                if (LOG.isLoggable(DEBUG)) {
                    LOG.log(DEBUG, _strings.get("noMatchedFile", pathEntry));
                }
                continue;
            }
            if (substituables == null) {
                substituables = new ArrayList<Substitutable>(retrievedFiles.size());
            }
            for (File retrievedFile : retrievedFiles) {
                if (retrievedFile.exists()) {
                    try {
                        FileSubstitutionHandler substituable = retrievedFile.length() > SubstitutionFileUtil
                                .getInMemorySubstitutionFileSizeInBytes() ? new LargeFileSubstitutionHandler(retrievedFile)
                                        : new SmallFileSubstitutionHandler(retrievedFile);
                        substituables.add(substituable);
                    } catch (FileNotFoundException e) {
                        LOG.log(WARNING, () -> "Could not locate file or resource " + retrievedFile, e);
                    }
                }
            }
        }
        return substituables == null ? Collections.EMPTY_LIST : substituables;
    }
}

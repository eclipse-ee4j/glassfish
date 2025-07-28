/*
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

import com.sun.enterprise.admin.servermgmt.SLogger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Utility class for the substitutable files.
 */
public class SubstitutionFileUtil {
    private static final Logger _logger = SLogger.getLogger();

    private static final LocalStringsImpl _strings = new LocalStringsImpl(SubstitutionFileUtil.class);

    private static final String INMEMORY_SUBSTITUTION_FILE_SIZE_IN_KB = "inmemory.substitution.file.size.in.kb";
    private static final int DEFAULT_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_KB = 10240;
    private static int PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES = 0;

    /**
     * The maximum copy byte count.
     */
    public static final int MAX_COPY_BYTE_COUNT = (64 * 1024 * 1024) - (32 * 1024);

    /**
     * Gets the maximum file size for which in-memory substitution can be performed.
     *
     * @return Max file size in bytes to perform in-memory substitution.
     */
    public static int getInMemorySubstitutionFileSizeInBytes() {
        if (PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES > 0) {
            return PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES;
        }
        try {
            PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES = Integer
                    .parseInt(StringSubstitutionProperties.getProperty(INMEMORY_SUBSTITUTION_FILE_SIZE_IN_KB)) * 1024;
        } catch (Exception e) {
            _logger.log(Level.INFO, SLogger.MISSING_MEMORY_FILE_SIZE);
            PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES = DEFAULT_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_KB;
        }
        return PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES > 0 ? PROVIDED_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_BYTES
                : DEFAULT_INMEMORY_SUBSTITUTION_FILE_SIZE_IN_KB;
    }

    /**
     * Create a directory with the given prefix.
     *
     * @param prefix Prefix for the directory name.
     * @return An extraction directory.
     */
    public static File setupDir(String prefix) throws IOException {
        String extractBase = System.getProperty("user.dir");
        File extractDir = null;
        File extractBaseFile = new File(extractBase);
        if (!extractBaseFile.mkdirs()) {
            _logger.log(Level.WARNING, SLogger.DIR_CREATION_ERROR, extractBaseFile.getAbsolutePath());
        }
        extractDir = File.createTempFile(prefix, null, extractBaseFile);
        // ensure it's a directory
        if (extractDir.delete()) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, _strings.get("recreateDirectories", extractDir.getAbsolutePath()));
            }
        }
        if (!extractDir.mkdirs()) {
            _logger.log(Level.WARNING, SLogger.DIR_CREATION_ERROR, extractDir.getAbsolutePath());
        }
        return extractDir;
    }

    /**
     * Delete's the given file, if the file is a directory then method will recursively delete the content of it.
     *
     * @param file File to delete.
     */
    public static void removeDir(File file) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    removeDir(f);
                }
            }
        }
        if (!file.delete()) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, _strings.get("failureInFileDeletion", file.getAbsolutePath()));
            }
        }
    }
}

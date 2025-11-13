/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.System.Logger;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates {@link Reader} and {@link Writer} for the String substitution file. Implementation is useful for large files
 * which cann't be read entirely in a memory or need a substantial amount of memory.
 * <p>
 * To perform substitution it take helps of temporary file to write output, after substitution, temporary file renamed
 * to input file.
 * <p>
 */
public class LargeFileSubstitutionHandler extends FileSubstitutionHandler {
    private static final Logger LOG = System.getLogger(LargeFileSubstitutionHandler.class.getName());
    private static final String BACKUP_FILE_PREFIX = ".bkp";
    private static final String TEMP_FILE_PREFIX = ".tmp";
    private File _outputFile;

    public LargeFileSubstitutionHandler(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    public Reader getReader() {
        try {
            _reader = new BufferedReader(new InputStreamReader(new FileInputStream(_inputFile), UTF_8));
        } catch (FileNotFoundException e) {
            LOG.log(INFO, () -> _strings.get("invalidFileLocation", _inputFile.getAbsolutePath()), e);
        }
        return _reader;
    }

    @Override
    public Writer getWriter() {
        _outputFile = new File(_inputFile.getAbsolutePath() + TEMP_FILE_PREFIX);
        try {
            if (!_outputFile.exists()) {
                if (!_outputFile.createNewFile()) {
                    throw new IOException();
                }
            }
            _writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_outputFile), UTF_8));
        } catch (IOException e) {
            LOG.log(INFO, () -> _strings.get("failureTempFileCreation", _outputFile.getAbsolutePath(), e));
        }
        return _writer;
    }

    @Override
    public void finish() {
        super.finish();
        String inputFileName = _inputFile.getName();
        File inputBackUpfile = new File(_inputFile.getAbsolutePath() + BACKUP_FILE_PREFIX);
        if (_inputFile.renameTo(inputBackUpfile)) {
            if (_outputFile.renameTo(new File(_inputFile.getAbsolutePath()))) {
                if (!inputBackUpfile.delete()) {
                    LOG.log(INFO, () -> _strings.get("failureInBackUpFileDeletion", inputBackUpfile.getAbsolutePath()));
                }
            } else {
                LOG.log(INFO, _strings.get("failureInFileRename", _outputFile.getAbsolutePath(), inputFileName));
            }
        } else {
            LOG.log(WARNING, () -> _strings.get("failureInFileRename", _inputFile.getAbsolutePath(), inputBackUpfile.getName()));
        }
    }
}

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

import java.io.BufferedWriter;
import java.io.CharArrayReader;
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
import java.lang.System.Logger.Level;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates {@link Reader} and {@link Writer} for the String substitution file. Its handles the small files which can be
 * processed differently for faster and better performance comparative to larger files.
 */
public class SmallFileSubstitutionHandler extends FileSubstitutionHandler {
    private static final Logger LOG = System.getLogger(SmallFileSubstitutionHandler.class.getName());

    /**
     * Constructs the {@link SmallFileSubstitutionHandler} for the given input file.
     *
     * @param file Input file.
     * @throws FileNotFoundException If file is not found.
     */
    public SmallFileSubstitutionHandler(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    public Reader getReader() {
        try {
            if (_reader == null) {
                char[] buffer = new char[(int) _inputFile.length()];
                int count = 0;
                try {
                    _reader = new InputStreamReader(new FileInputStream(_inputFile), UTF_8);
                    count = _reader.read(buffer);
                } finally {
                    _reader.close();
                }
                _reader = new CharArrayReader(buffer, 0, count);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, _strings.get("invalidFileLocation", _inputFile.getAbsolutePath()), e);
        }
        return _reader;
    }

    @Override
    public Writer getWriter() {
        try {
            _writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_inputFile), UTF_8));
        } catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, _strings.get("invalidFileLocation", _inputFile.getAbsolutePath()), e);
        }
        return _writer;
    }
}

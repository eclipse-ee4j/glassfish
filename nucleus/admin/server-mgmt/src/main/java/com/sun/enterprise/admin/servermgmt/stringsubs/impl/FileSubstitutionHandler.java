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
import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Abstract class initialize the input file for the string substitution. The sub-classes provides the way to create the
 * {@link Reader} and {@link Writer} for the input & output file.
 */
public abstract class FileSubstitutionHandler implements Substitutable {

    protected static final Logger _logger = SLogger.getLogger();

    protected static final LocalStringsImpl _strings = new LocalStringsImpl(FileLister.class);

    /** A {@link Reader} to read the character stream from input file. */
    protected Reader _reader;

    /** A {@link Writer} to write the character stream to the output file. */
    protected Writer _writer;

    /** Input file. */
    protected File _inputFile;

    public FileSubstitutionHandler(File file) throws FileNotFoundException {
        if (file.exists()) {
            _inputFile = file;
        } else {
            throw new FileNotFoundException(_strings.get("invalidFileLocation", file.getAbsolutePath()));
        }
    }

    @Override
    public String getName() {
        return _inputFile.getAbsolutePath();
    }

    @Override
    public void finish() {
        if (_reader != null) {
            try {
                _reader.close();
            } catch (Exception e) {
                if (_logger.isLoggable(Level.FINER)) {
                    _logger.log(Level.FINER, _strings.get("errorInClosingStream", _inputFile.getAbsolutePath()), e);
                }
            }
        }
        if (_writer != null) {
            try {
                _writer.close();
            } catch (Exception e) {
                if (_logger.isLoggable(Level.FINER)) {
                    _logger.log(Level.FINER, _strings.get("errorInClosingStream"), e);
                }
            }
        }
    }
}

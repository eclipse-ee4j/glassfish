/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.server.logging.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.glassfish.main.jul.formatter.LogFormatDetector;

public class LogParserFactory {

    private static final Logger LOG = Logger.getLogger(LogParserFactory.class.getName());

    private enum LogFormat {
        UNIFORM_LOG_FORMAT,
        ODL_LOG_FORMAT,
        ONELINE_LOG_FORMAT,
        UNKNOWN_LOG_FORMAT
    }

    private static final LogParserFactory SINGLETON = new LogParserFactory();
    private final LogFormatDetector logFormatDetector;

    public static LogParserFactory getInstance() {
        return SINGLETON;
    }


    private LogParserFactory() {
        logFormatDetector = new LogFormatDetector();
    }


    public LogParser createLogParser(File logFile) throws IOException {
        final String firstLine;
        try (BufferedReader reader = createReader(logFile)) {
            firstLine = reader.readLine();
        }
        final LogFormat format = detectLogFormat(firstLine);
        LOG.fine(() -> "Detected log format=" + format + " for line: " + firstLine);
        switch (format) {
            case UNIFORM_LOG_FORMAT:
                return new UniformLogParser();
            case ODL_LOG_FORMAT:
                return new ODLLogParser();
            case ONELINE_LOG_FORMAT:
                return new OneLineLogParser();
            default:
                return new RawLogParser();
        }
    }


    private BufferedReader createReader(File logFile) throws IOException {
        if (logFormatDetector.isCompressedFile(logFile.getName())) {
            return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile))));
        }
        return new BufferedReader(new FileReader(logFile));
    }


    private LogFormat detectLogFormat(final String line) {
        if (line != null) {
            if (logFormatDetector.isODLFormatLogHeader(line)) {
                return LogFormat.ODL_LOG_FORMAT;
            } else if (logFormatDetector.isUniformFormatLogHeader(line)) {
                return LogFormat.UNIFORM_LOG_FORMAT;
            } else if (logFormatDetector.isOneLineLFormatLogHeader(line)) {
                return LogFormat.ONELINE_LOG_FORMAT;
            }
        }
        return LogFormat.UNKNOWN_LOG_FORMAT;
    }
}

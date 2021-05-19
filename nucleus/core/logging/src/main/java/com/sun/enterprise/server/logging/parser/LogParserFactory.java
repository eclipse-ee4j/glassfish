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

package com.sun.enterprise.server.logging.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.enterprise.server.logging.LogFormatHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;

public class LogParserFactory {

    final private static LocalStringManagerImpl LOCAL_STRINGS =
            new LocalStringManagerImpl(LogParserFactory.class);

    static final String NEWLINE = System.getProperty("line.separator");

    private static enum LogFormat {
        UNIFORM_LOG_FORMAT,
        ODL_LOG_FORMAT,
        UNKNOWN_LOG_FORMAT
    };

    private static final String ODL_LINE_HEADER_REGEX =
        "\\[(\\d){4}\\-(\\d){2}\\-(\\d){2}T(\\d){2}\\:(\\d){2}\\:(\\d){2}\\.(\\d){3}[\\+|\\-](\\d){4}\\].*";

    private static final boolean DEBUG = false;

    private static class SingletonHolder {
        private static final LogParserFactory SINGLETON = new LogParserFactory();
    }

    public static LogParserFactory getInstance() {
        return SingletonHolder.SINGLETON;
    }

    private Pattern odlDateFormatPattern = null;

    private LogParserFactory() {
        odlDateFormatPattern  = Pattern.compile(ODL_LINE_HEADER_REGEX);
    }

    public LogParser createLogParser(File logFile) throws LogParserException, IOException {
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new FileReader(logFile));
            String line = reader.readLine();
            LogFormat format = detectLogFormat(line);
            if (DEBUG) {
                System.out.println("Log format=" + format.name() + " for line:" + line);
            }
            switch(format) {
            case UNIFORM_LOG_FORMAT:
                return new UniformLogParser(logFile.getName());
            case ODL_LOG_FORMAT:
                return new ODLLogParser(logFile.getName());
            default:
                return new RawLogParser(logFile.getName());
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    Pattern getODLDateFormatPattern() {
        return odlDateFormatPattern;
    }

    private LogFormat detectLogFormat(String line) {
        if (line != null) {
            Matcher m = odlDateFormatPattern.matcher(line);
            if (m.matches()) {
                if (DEBUG) {
                    System.out.println("Matched ODL pattern for line:" + line);
                }
                return LogFormat.ODL_LOG_FORMAT;
            } else if (LogFormatHelper.isUniformFormatLogHeader(line)) {
                return LogFormat.UNIFORM_LOG_FORMAT;
            }
        }
        return LogFormat.UNKNOWN_LOG_FORMAT;
    }

}

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

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.OffsetDateTime;

final class UniformLogParser implements LogParser {

    private static final LocalStringManagerImpl LOCAL_STRINGS = new LocalStringManagerImpl(UniformLogParser.class);

    private static final String FIELD_SEPARATOR = "\\|";
    private static final String LOG_RECORD_BEGIN_MARKER = "[#|";
    private static final String LOG_RECORD_END_MARKER = "|#]";
    private static final int ULF_FIELD_COUNT = 6;



    @Override
    public void parseLog(BufferedReader reader, LogParserListener listener) throws LogParserException {
        String line = null;
        try {
            StringBuilder buffer = new StringBuilder();
            long position = 0L;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(LOG_RECORD_BEGIN_MARKER)) {
                    // Construct a parsed log record from the prior content
                    String logRecord = buffer.toString();
                    parseLogRecord(position, logRecord, listener);
                    position += logRecord.length();
                    buffer = new StringBuilder();
                }
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            // Last record
            String logRecord = buffer.toString();
            parseLogRecord(position, logRecord, listener);
        } catch (IOException e) {
            throw new LogParserException(line, e);
        }
    }


    private void parseLogRecord(long position, String logRecord, LogParserListener listener) {
        ParsedLogRecord parsedLogRecord = parse(logRecord);
        if (parsedLogRecord != null) {
            listener.foundLogRecord(position, parsedLogRecord);
        }
    }


    private ParsedLogRecord parse(String logRecord) {

        int beginIndex = logRecord.indexOf(LOG_RECORD_BEGIN_MARKER);
        if (beginIndex < 0) {
            return null;
        }
        int endIndex = logRecord.lastIndexOf(LOG_RECORD_END_MARKER);
        if (endIndex < 0) {
            return null;
        }
        if (logRecord.length() < LOG_RECORD_BEGIN_MARKER.length() + LOG_RECORD_END_MARKER.length()) {
            return null;
        }

        String logData = logRecord.substring(beginIndex + LOG_RECORD_BEGIN_MARKER.length(), endIndex);
        String[] fieldValues = logData.split(FIELD_SEPARATOR);
        if (fieldValues.length < ULF_FIELD_COUNT) {
            String msg = LOCAL_STRINGS.getLocalString("parser.illegal.ulf.record",
                "Illegal Uniform format log record {0} found", logRecord);
            throw new IllegalArgumentException(msg);
        }

        ParsedLogRecord parsedLogRecord = new ParsedLogRecord(logRecord);
        parsedLogRecord.setTimestamp(OffsetDateTime.parse(fieldValues[0], ISO_OFFSET_DATE_TIME_PARSER));
        parsedLogRecord.setLogLevel(fieldValues[1]);
        parsedLogRecord.setProductId(fieldValues[2]);
        parsedLogRecord.setLogger(fieldValues[3]);
        populateLogRecordFields(fieldValues[4], parsedLogRecord);

        final StringBuilder message = new StringBuilder();
        message.append(fieldValues[5].trim());
        if (fieldValues.length > ULF_FIELD_COUNT) {
            for (int i = ULF_FIELD_COUNT; i < fieldValues.length; i++) {
                message.append("|").append(fieldValues[i]);
            }
        }
        parsedLogRecord.setMessage(message.toString());
        return parsedLogRecord;
    }


    private void populateLogRecordFields(String fieldData, ParsedLogRecord parsedLogRecord) {
        String[] nv_pairs = fieldData.split(";");
        for (String pair : nv_pairs) {
            String[] nameValue = pair.split("=");
            if (nameValue.length == 2) {
                String key = nameValue[0];
                String value = nameValue[1];
                if ("_ThreadID".equals(key)) {
                    parsedLogRecord.setThreadId(LogParser.toLong(value));
                } else if ("_ThreadName".equals(key)) {
                    parsedLogRecord.setThreadName(value);
                } else if ("_MessageID".equals(key)) {
                    parsedLogRecord.setMessageKey(value);
                } else if ("_LevelValue".equals(key)) {
                    parsedLogRecord.setLogLevelValue(LogParser.toInteger(value));
                } else {
                    parsedLogRecord.setSupplementalValue(key, value);
                }
            }
        }
    }
}

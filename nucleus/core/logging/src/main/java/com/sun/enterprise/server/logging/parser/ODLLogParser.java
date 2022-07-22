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
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.main.jul.formatter.ExcludeFieldsSupport.SupplementalAttribute;

import static org.glassfish.main.jul.formatter.LogFormatDetector.P_TIMESTAMP;

/**
 * @author sanshriv
 */
final class ODLLogParser implements LogParser {

    private static final int ODL_FIXED_FIELD_COUNT = 5;
    private static final Pattern ODL_LINE_HEADER_PATTERN = Pattern.compile("\\[" + P_TIMESTAMP + "].*");
    private static final Pattern ODL_FIELD_PATTERN = Pattern.compile("(\\[[^\\[\\]]*?\\])+?");
    private static final Pattern ODL_TID_PATTERN = Pattern.compile("[ ]*_ThreadID=(.+) _ThreadName=(.+)");

    @Override
    public void parseLog(BufferedReader reader, LogParserListener listener) throws LogParserException {
        String line = null;
        try {
            StringBuilder buffer = new StringBuilder();
            long position = 0L;
            while ((line = reader.readLine()) != null) {
                Matcher m = ODL_LINE_HEADER_PATTERN.matcher(line);
                if (m.matches()) {
                    // Construct a parsed log record from the prior content
                    String logRecord = buffer.toString();
                    process(position, logRecord, listener);
                    position += logRecord.length();
                    buffer = new StringBuilder();
                }
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            // Last record
            String logRecord = buffer.toString();
            process(position, logRecord, listener);
        } catch (IOException e) {
            throw new LogParserException(line, e);
        }
    }


    private void process(long position, String logRecord, LogParserListener listener) {
        ParsedLogRecord parsedLogRecord = parse(logRecord);
        if (parsedLogRecord != null) {
            listener.foundLogRecord(position, parsedLogRecord);
        }
    }


    private ParsedLogRecord parse(String logRecord) {
        ParsedLogRecord parsedLogRecord = new ParsedLogRecord(logRecord);
        Matcher matcher = ODL_FIELD_PATTERN.matcher(logRecord);
        int start = 0;
        int end = 0;
        int fieldIndex = 0;
        while (matcher.find()) {
            fieldIndex++;
            start = matcher.start();
            if (end != 0 && start != end + 1) {
                break;
            }
            end = matcher.end();
            String text = matcher.group();
            text = text.substring(1, text.length() - 1);
            if (fieldIndex <= ODL_FIXED_FIELD_COUNT) {
                populateLogRecordFields(fieldIndex, text, parsedLogRecord);
            } else {
                populateLogRecordSuppAttrs(text, parsedLogRecord);
            }
        }
        String msg = logRecord.substring(end).trim();
        boolean multiLineBegin = false;
        if (msg.startsWith("[[")) {
            msg = msg.replaceFirst("\\[\\[", "").trim();
            multiLineBegin = true;
        }
        if (multiLineBegin && msg.endsWith("]]")) {
            int endIndex = msg.length() - 2;
            if (endIndex > 0) {
                msg = msg.substring(0, endIndex);
            }
        }
        parsedLogRecord.setMessage(msg);
        if (fieldIndex < ODL_FIXED_FIELD_COUNT) {
            return null;
        }
        return parsedLogRecord;
    }


    private void populateLogRecordSuppAttrs(String text, ParsedLogRecord parsedLogRecord) {
        int index = text.indexOf(':');
        if (index > 0) {
            String key = text.substring(0, index);
            String value = text.substring(index + 1).trim();
            if (SupplementalAttribute.TID.getId().equals(key)) {
                Matcher matcher = ODL_TID_PATTERN.matcher(value);
                if (matcher.find()) {
                    parsedLogRecord.setThreadId(LogParser.toLong(matcher.group(1)));
                    parsedLogRecord.setThreadName(matcher.group(2));
                }
            } else if (SupplementalAttribute.LEVEL_VALUE.getId().equals(key)) {
                parsedLogRecord.setLogLevelValue(LogParser.toInteger(value));
            } else {
                parsedLogRecord.setSupplementalValue(key, value);
            }
        }
    }


    private void populateLogRecordFields(int index, String fieldData, ParsedLogRecord parsedLogRecord) {
        switch (index) {
            case 1:
                parsedLogRecord.setTimestamp(OffsetDateTime.parse(fieldData, ISO_OFFSET_DATE_TIME_PARSER));
                break;
            case 2:
                parsedLogRecord.setProductId(fieldData);
                break;
            case 3:
                parsedLogRecord.setLogLevel(fieldData);
                break;
            case 4:
                parsedLogRecord.setMessageKey(fieldData);
                break;
            case 5:
                parsedLogRecord.setLogger(fieldData);
                break;
            default:
                break;
        }
    }
 }

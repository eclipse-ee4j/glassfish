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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.enterprise.server.logging.LogFacade;

/**
 * @author sanshriv
 *
 */
final class ODLLogParser implements LogParser {

    private static final int ODL_FIXED_FIELD_COUNT = 5;

    private static final String ODL_FIELD_REGEX = "(\\[[^\\[\\]]*?\\])+?";

    private static final class ODLFieldPatternHolder {
        static final Pattern ODL_FIELD_PATTERN = Pattern.compile(ODL_FIELD_REGEX);
    }

    private static final Map<String, String> ODL_STANDARD_FIELDS = new HashMap<String, String>(){

        private static final long serialVersionUID = -6870456038890663569L;

        {
            put("tid", ParsedLogRecord.THREAD_ID);
            put(ParsedLogRecord.EC_ID, ParsedLogRecord.EC_ID);
            put(ParsedLogRecord.USER_ID, ParsedLogRecord.USER_ID);
        }
    };

    private String streamName;

    public ODLLogParser(String name) {
        streamName = name;
    }

    @Override
    public void parseLog(BufferedReader reader, LogParserListener listener)
            throws LogParserException
    {

        try {
            String line = null;
            StringBuffer buffer = new StringBuffer();
            long position = 0L;
            while ((line = reader.readLine()) != null) {
                Matcher m = LogParserFactory.getInstance().getODLDateFormatPattern().matcher(line);
                if (m.matches()) {
                    // Construct a parsed log record from the prior content
                    String logRecord = buffer.toString();
                    parseLogRecord(position, logRecord, listener);
                    position += logRecord.length();
                    buffer = new StringBuffer();
                }
                buffer.append(line);
                buffer.append(LogParserFactory.NEWLINE);
            }
            // Last record
            String logRecord = buffer.toString();
            parseLogRecord(position, logRecord, listener);
        } catch(IOException e){
            throw new LogParserException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogFacade.LOGGING_LOGGER.log(Level.FINE, "Got exception while clsoing reader "+ streamName, e);
                }
            }
        }
    }

    private void parseLogRecord(long position, String logRecord, LogParserListener listener) {
        ParsedLogRecord parsedLogRecord = new ParsedLogRecord();
        if (initializeUniformFormatLogRecord(parsedLogRecord, logRecord)) {
            listener.foundLogRecord(position, parsedLogRecord);
        }
    }

    private boolean initializeUniformFormatLogRecord(
            ParsedLogRecord parsedLogRecord,
            String logRecord)
    {
        parsedLogRecord.setFormattedLogRecord(logRecord);
        Matcher matcher = ODLFieldPatternHolder.ODL_FIELD_PATTERN.matcher(logRecord);
        int start=0;
        int end=0;
        int fieldIndex=0;
        while (matcher.find()) {
            fieldIndex++;
            start = matcher.start();
            if (end != 0 && start != end+1) {
                break;
            }
            end = matcher.end();
            String text = matcher.group();
            text = text.substring(1, text.length()-1);
            if (fieldIndex <= ODL_FIXED_FIELD_COUNT) {
                populateLogRecordFields(fieldIndex, text, parsedLogRecord);
            } else {
                populateLogRecordSuppAttrs(text, parsedLogRecord);
            }
        }
        String msg = logRecord.substring(end);
        // System.out.println("Indexof=" + msg.indexOf("[["));
        msg = msg.trim();
        boolean multiLineBegin = false;
        if (msg.startsWith("[[")) {
            msg = msg.replaceFirst("\\[\\[", "").trim();
            multiLineBegin = true;
            multiLineBegin = true;
        }
        if (multiLineBegin && msg.endsWith("]]")) {
            int endIndex = msg.length() - 2;
            if (endIndex > 0) {
                msg = msg.substring(0, endIndex);
            }
        }
        parsedLogRecord.setFieldValue(ParsedLogRecord.LOG_MESSAGE, msg);
        if (fieldIndex < ODL_FIXED_FIELD_COUNT) {
            return false;
        }
        return true;
    }

    private void populateLogRecordSuppAttrs(String text,
            ParsedLogRecord parsedLogRecord) {
        int index = text.indexOf(':');
        if (index > 0) {
            String key = text.substring(0, index);
            String value = text.substring(index+1);
            value = value.trim();
            if (ODL_STANDARD_FIELDS.containsKey(key)) {
                parsedLogRecord.setFieldValue(ODL_STANDARD_FIELDS.get(key), value);
            } else {
                Properties props = (Properties) parsedLogRecord.getFieldValue(ParsedLogRecord.SUPP_ATTRS);
                props.put(key, value);
                if (key.equals(ParsedLogRecord.TIME_MILLIS)) {
                    parsedLogRecord.setFieldValue(ParsedLogRecord.TIME_MILLIS, value);
                }
            }
        }
    }

    private void populateLogRecordFields(int index, String fieldData,
            ParsedLogRecord parsedLogRecord)
    {
        switch(index) {
        case 1:
            parsedLogRecord.setFieldValue(ParsedLogRecord.DATE_TIME, fieldData);
            break;
        case 2:
            parsedLogRecord.setFieldValue(ParsedLogRecord.PRODUCT_ID, fieldData);
            break;
        case 3:
            parsedLogRecord.setFieldValue(ParsedLogRecord.LOG_LEVEL_NAME, fieldData);
            break;
        case 4:
            parsedLogRecord.setFieldValue(ParsedLogRecord.MESSAGE_ID, fieldData);
            break;
        case 5:
            parsedLogRecord.setFieldValue(ParsedLogRecord.LOGGER_NAME, fieldData);
            break;
        default:
            break;
        }
    }

 }

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

import com.sun.enterprise.server.logging.LogFacade;
import com.sun.enterprise.util.LocalStringManagerImpl;

final class UniformLogParser implements LogParser {

    final private static LocalStringManagerImpl LOCAL_STRINGS =
        new LocalStringManagerImpl(UniformLogParser.class);

    static final String FIELD_SEPARATOR = "\\|";

    static final String LOG_RECORD_BEGIN_MARKER = "[#|";

    static final String LOG_RECORD_END_MARKER = "|#]";

    private static final int ULF_FIELD_COUNT = 6;

    private static final Map<String,String> FIELD_NAME_ALIASES =
        new HashMap<String,String>()
    {
        private static final long serialVersionUID = -2041470292369513712L;
        {
            put("_ThreadID", ParsedLogRecord.THREAD_ID);
            // put("_ThreadName", "threadName");
            put("_TimeMillis", ParsedLogRecord.TIME_MILLIS);
            put("_LevelValue", ParsedLogRecord.LOG_LEVEL_VALUE);
            put("_UserID", ParsedLogRecord.USER_ID);
            put("_ECID", ParsedLogRecord.EC_ID);
            put("_MessageID",ParsedLogRecord.MESSAGE_ID);
            // put("ClassName", "className");
            // put("MethodName", "methodName");
        }
    };

    private String streamName;

    public UniformLogParser(String name) {
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
                if (line.startsWith(LOG_RECORD_BEGIN_MARKER)) {
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

        int beginIndex = logRecord.indexOf(LOG_RECORD_BEGIN_MARKER);
        if (beginIndex < 0) {
            return false;
        }
        int endIndex = logRecord.lastIndexOf(LOG_RECORD_END_MARKER);
        if (endIndex < 0) {
            return false;
        }

        if (logRecord.length() <
                (LOG_RECORD_BEGIN_MARKER.length() + LOG_RECORD_END_MARKER.length()))
        {
            return false;
        }

        String logData = logRecord.substring(
                beginIndex + LOG_RECORD_BEGIN_MARKER.length(), endIndex);

        String[] fieldValues = logData.split(FIELD_SEPARATOR);
        if (fieldValues.length < ULF_FIELD_COUNT) {
            String msg = LOCAL_STRINGS.getLocalString(
                    "parser.illegal.ulf.record", "Illegal Uniform format log record {0} found", logRecord);
            throw new IllegalArgumentException(msg);
        }

        for (int i=0; i < ULF_FIELD_COUNT; i++) {
           populateLogRecordFields(i, fieldValues[i], parsedLogRecord);
        }

        if (fieldValues.length > ULF_FIELD_COUNT) {
            StringBuffer buf = new StringBuffer();
            buf.append(parsedLogRecord.getFieldValue(ParsedLogRecord.LOG_MESSAGE));
            for (int i=ULF_FIELD_COUNT; i<fieldValues.length; i++) {
                buf.append("|");
                buf.append(fieldValues[i]);
            }
            parsedLogRecord.setFieldValue(ParsedLogRecord.LOG_MESSAGE, buf.toString());
        }
        return true;
    }

    private void populateLogRecordFields(int index, String fieldData,
            ParsedLogRecord parsedLogRecord)
    {
        switch(index) {
        case 0:
            parsedLogRecord.setFieldValue(ParsedLogRecord.DATE_TIME, fieldData);
            break;
        case 1:
            parsedLogRecord.setFieldValue(ParsedLogRecord.LOG_LEVEL_NAME, fieldData);
            break;
        case 2:
            parsedLogRecord.setFieldValue(ParsedLogRecord.PRODUCT_ID, fieldData);
            break;
        case 3:
            parsedLogRecord.setFieldValue(ParsedLogRecord.LOGGER_NAME, fieldData);
            break;
        case 4:
            String[] nv_pairs = fieldData.split(";");
            for (String pair : nv_pairs) {
                String[] nameValue = pair.split("=");
                if (nameValue.length == 2) {
                    String name = nameValue[0];
                    String value = nameValue[1];
                    if (FIELD_NAME_ALIASES.containsKey(name)) {
                        name = FIELD_NAME_ALIASES.get(name);
                        parsedLogRecord.setFieldValue(name, value);
                    } else {
                        Properties props = (Properties) parsedLogRecord.getFieldValue(ParsedLogRecord.SUPP_ATTRS);
                        props.put(name, value);
                    }
                }
            }
            break;
        case 5:
            parsedLogRecord.setFieldValue(ParsedLogRecord.LOG_MESSAGE, fieldData);
            break;
        default:
            break;
        }
    }

 }

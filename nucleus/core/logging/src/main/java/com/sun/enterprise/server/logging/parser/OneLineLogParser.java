/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.glassfish.main.jul.formatter.LogFormatDetector.P_TIME;
import static org.glassfish.main.jul.formatter.LogFormatDetector.P_TIMESTAMP;


/**
 * @author David Matejcek
 */
public class OneLineLogParser implements LogParser {

    private static final Pattern PATTERN = Pattern
        .compile("(" + P_TIMESTAMP + "|" + P_TIME + ")[ ]+([A-Z]+)[ ]+([\\S]+)[ ]+([\\S]*)(.*)");

    @Override
    public void parseLog(BufferedReader reader, LogParserListener listener) throws LogParserException {
        String line = null;
        try {
            long position = 0L;
            while ((line = reader.readLine()) != null) {
                ParsedLogRecord record = new ParsedLogRecord(line);
                Matcher matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    continue;
                }
                final String timestamp = matcher.group(1);
                if (timestamp.length() > 18) {
                    record.setTimestamp(OffsetDateTime.parse(timestamp, ISO_OFFSET_DATE_TIME_PARSER));
                } else {
                    record.setTime(LocalTime.parse(timestamp, ISO_LOCAL_TIME_PARSER));
                }
                record.setLogLevel(matcher.group(3));
                record.setThreadName(matcher.group(4));
                record.setLogger(matcher.group(5));
                // this can be also source class.method
                record.setMessage(matcher.group(6).trim());
                listener.foundLogRecord(position, record);
                position++;
            }
        } catch (IOException e) {
            throw new LogParserException(line, e);
        }
    }

}

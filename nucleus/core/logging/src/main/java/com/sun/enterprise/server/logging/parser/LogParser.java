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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

/**
 * @author sanshriv
 * @author David Matejcek
 */
public interface LogParser {

    // See GlassFishLogFormatter which has similar formatters, but these are more
    // benevolent and can parse records from old GF versions too.
    /** Example: 15:35:40.123456 */
    DateTimeFormatter ISO_LOCAL_TIME_PARSER = new DateTimeFormatterBuilder()
        .appendValue(HOUR_OF_DAY, 2).appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2).optionalStart()
        .appendFraction(NANO_OF_SECOND, 1, 9, true)
        .toFormatter(Locale.ROOT);

    /** Example: 2011-12-03T15:35:40.123456 */
    DateTimeFormatter ISO_LOCAL_DATE_TIME_PARSER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(ISO_LOCAL_TIME_PARSER)
        .toFormatter(Locale.ROOT);

    /** ISO-8601. Example: 2011-12-03T15:35:40.123456+01:00 */
    DateTimeFormatter ISO_OFFSET_DATE_TIME_PARSER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(ISO_LOCAL_DATE_TIME_PARSER)
        .appendPattern("X")
        .toFormatter(Locale.ROOT);

    /**
     * Converter which ignores errors.
     *
     * @return {@link Long} or null
     */
    static Long toLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converter which ignores errors.
     *
     * @return {@link Integer} or null
     */
    static Integer toInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    void parseLog(BufferedReader reader, LogParserListener listener) throws LogParserException;

}

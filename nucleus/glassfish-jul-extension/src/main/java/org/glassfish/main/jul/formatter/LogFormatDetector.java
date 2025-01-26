/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

/**
 * Detector of known standardized log formats.
 *
 * @author David Matejcek
 */
public class LogFormatDetector {

    private static final String GZIP_EXTENSION = ".gz";
    private static final int ODL_SUBSTRING_LEN = 5;
    private static final String ODL_LINE_BEGIN_REGEX = "\\[[\\-\\:\\d]{4}";
    private static final Pattern ODL_PATTERN = Pattern.compile(ODL_LINE_BEGIN_REGEX);

    /**
     * {@link Pattern} string for usual time format: HH:mm:ss.SSS
     */
    public static final String P_TIME = "\\d\\d:\\d\\d:\\d\\d\\.[\\d]{3,9}";
    /**
     * {@link Pattern} string for usual time zone format: +02:00, +0200 or Z
     */
    public static final String P_TIMEZONE = "([0-9:+-]{5,8}|Z)";
    /**
     * {@link Pattern} string for usual ISO-8601 timestamp format: 2021-05-20T12:45:33.123456Z
     */
    public static final String P_TIMESTAMP = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}T" + P_TIME + P_TIMEZONE;

    /**
     * {@link Pattern} string for {@link Level} name: usually upper case letters.
     */
    public static final String P_LEVEL_NAME = "[A-Z]+";
    /**
     * {@link Pattern} string for {@link Level} value: usually int value.
     */
    public static final String P_LEVEL_VALUE = "[0-9]{3,4}";
    /**
     * {@link Pattern} string for usual {@link Logger} name: if present, contains letters and dots.
     */
    public static final String P_LOGGER_NAME = "[a-z.]*";
    /**
     * {@link Pattern} string for usual message key used with resource bundles: if present, letters,
     * dots and numbers.
     */
    public static final String P_MESSAGE_KEY = "[a-zA-Z0-9.]*";
    /**
     * {@link Pattern} string for a product id: any non-mandatory text.
     */
    public static final String P_PRODUCT_ID = ".*";

    /**
     * @param logFile
     * @param expectedCharset
     * @return full class name of the concrete detected {@link Formatter} or null if the file is
     *         null or could not be read.
     */
    public String detectFormatter(final File logFile, final Charset expectedCharset) {
        if (logFile == null || !logFile.canRead()) {
            return null;
        }
        final String firstLine;
        try (BufferedReader br = new BufferedReader(new FileReader(logFile, expectedCharset))) {
            firstLine = br.readLine();
        } catch (Exception e) {
            GlassFishLoggingTracer.error(getClass(), e.getMessage(), e);
            return null;
        }

        return detectFormatter(firstLine);
    }


    /**
     * @param firstLine
     * @return null for unknown file format, full class name otherwise.
     */
    public String detectFormatter(final String firstLine) {
        if (firstLine == null || firstLine.isEmpty()) {
            return null;
        }
        if (isODLFormatLogHeader(firstLine)) {
            return ODLLogFormatter.class.getName();
        }
        if (isUniformFormatLogHeader(firstLine)) {
            return UniformLogFormatter.class.getName();
        }
        if (isOneLineLFormatLogHeader(firstLine)) {
            return OneLineFormatter.class.getName();
        }
        return null;
    }


    /**
     * @param firstLine
     * @return true if the given line is probably a beginning of a ODL log record.
     */
    public boolean isODLFormatLogHeader(final String firstLine) {
        return firstLine.length() > ODL_SUBSTRING_LEN
            && ODL_PATTERN.matcher(firstLine.substring(0, ODL_SUBSTRING_LEN)).matches()
            && countOccurrences(firstLine, '[') > 4;
    }


    /**
     * @param firstLine
     * @return true if the given line is probably a beginning of a {@link OneLineFormatter}'s log record.
     */
    public boolean isOneLineLFormatLogHeader(final String firstLine) {
        return firstLine.matches(P_TIME + "\\s+[A-Z]{4,13}\\s+.+\\s+.+\\s+.+");
    }


    /**
     * @param firstLine
     * @return true if the given line is probably a beginning of a Uniform log record.
     */
    public boolean isUniformFormatLogHeader(final String firstLine) {
        return firstLine.startsWith("[#|") && countOccurrences(firstLine, '|') > 4;
    }


    /**
     * Determines whether the given file is compressed (name ends with .gz).
     *
     * @param filename
     * @return true if the filename ends with {@value #GZIP_EXTENSION}
     */
    public boolean isCompressedFile(final String filename) {
        return filename.endsWith(GZIP_EXTENSION);
    }


    private int countOccurrences(final String firstLine, final char typicalCharacter) {
        int count = 0;
        for (int i = 0; i < firstLine.length(); i++) {
            if (firstLine.charAt(i) == typicalCharacter) {
                count++;
            }
        }
        return count;
    }
}

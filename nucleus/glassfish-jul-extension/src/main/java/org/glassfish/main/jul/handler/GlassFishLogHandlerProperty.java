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

package org.glassfish.main.jul.handler;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.cfg.LogProperty;

/**
 * Configuration property set of this handler.
 */
public enum GlassFishLogHandlerProperty implements LogProperty {

    /** False means that handler will stay in logging structure, but will ignore incoming records */
    ENABLED("enabled"),
    /** Minimal acceptable level of the incoming log record */
    LEVEL("level"),
    /** Absolute path to the output file */
    OUTPUT_FILE("file"),
    /** Charset */
    ENCODING("encoding"),
    /** Class of the {@link Formatter} used with this handler */
    FORMATTER(HandlerConfigurationHelper.FORMATTER.getPropertyName()),
    /**
     * LogRecord buffer size. If the buffer is full and it is not possible to add new record for
     * {@link #BUFFER_TIMEOUT} seconds, buffer will reset and replace all records with just one
     * severe {@link LogRecord} explaining what happened.
     */
    BUFFER_CAPACITY("buffer.capacity"),
    /**
     * LogRecord buffer timeout for adding new records if the buffer is full.
     * If the buffer is full and it is not possible to add new record for
     * this count of seconds, buffer will reset and replace all records with just one
     * severe {@link LogRecord} explaining what happened.
     * <p>
     * 0 means wait forever.
     */
    BUFFER_TIMEOUT("buffer.timeoutInSeconds"),
    /** Count of records processed until handler flushes the output */
    FLUSH_FREQUENCY("flushFrequency"),
    /** Log STDOUT and STDERR to the log file too */
    REDIRECT_STANDARD_STREAMS("redirectStandardStreams"),
    /** Compress rolled file to a zio file */
    ROTATION_COMPRESS("rotation.compress"),
    /** File will be rotated after mignight */
    ROTATION_ON_DATE_CHANGE("rotation.rollOnDateChange"),
    /** File containing more megabytes (1 000 000 B) will be rotated */
    ROTATION_LIMIT_SIZE("rotation.limit.megabytes"),
    /** File will be rotated after given count of minutes */
    ROTATION_LIMIT_TIME("rotation.limit.minutes"),
    /** Maximal count of archived files */
    ROTATION_MAX_HISTORY("rotation.maxArchiveFiles"),
    ;
    public static final int MINIMUM_ROTATION_LIMIT_MB = 1;
    public static final int DEFAULT_ROTATION_LIMIT_MB = 100;
    public static final int DEFAULT_BUFFER_CAPACITY = 10_000;
    public static final int DEFAULT_BUFFER_TIMEOUT = 0;

    private final String propertyName;

    GlassFishLogHandlerProperty(final String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @return full name using the {@link GlassFishLogHandler} class.
     */
    public String getPropertyFullName() {
        return getPropertyFullName(GlassFishLogHandler.class);
    }

}
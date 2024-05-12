/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.record.MessageResolver;
import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.KEY_FORMATTER_PRINT_SOURCE_SUFFIX;
import static org.glassfish.main.jul.formatter.GlassFishLogFormatter.GlassFishLogFormatterProperty.PRINT_SEQUENCE_NUMBER;
import static org.glassfish.main.jul.formatter.GlassFishLogFormatter.GlassFishLogFormatterProperty.PRINT_SOURCE;
import static org.glassfish.main.jul.formatter.GlassFishLogFormatter.GlassFishLogFormatterProperty.TIMESTAMP_FORMAT;

/**
 * A special {@link Formatter} able to notify some delegate about the {@link LogRecord} which passed
 * through this instance.
 *
 * @author David Matejcek
 */
public abstract class GlassFishLogFormatter extends Formatter {
    /** Resolves {@link LogRecord} to {@link GlassFishLogRecord} */
    protected static final MessageResolver MSG_RESOLVER = new MessageResolver();


    // This was required, because we need 3 decimal numbers of the second fraction
    // DateTimeFormatter.ISO_LOCAL_DATE_TIME prints just nonzero values
    /** Example: 15:35:40.123456 */
    protected static final DateTimeFormatter ISO_LOCAL_TIME = new DateTimeFormatterBuilder()
        .appendValue(HOUR_OF_DAY, 2).appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2).optionalStart()
        .appendFraction(NANO_OF_SECOND, 6, 6, true)
        .toFormatter(Locale.ROOT);

    /** Example: 2011-12-03T15:35:40.123456 */
    protected static final DateTimeFormatter ISO_LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(ISO_LOCAL_TIME)
        .toFormatter(Locale.ROOT);

    /** ISO-8601. Example: 2011-12-03T15:35:40.123456+01:00 */
    protected static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(ISO_LOCAL_DATE_TIME)
        .appendOffsetId()
        .toFormatter(Locale.ROOT);

    static {
        GlassFishLoggingTracer.trace(GlassFishLogFormatter.class, () -> "ISO local time: " + ISO_LOCAL_TIME
            + ", ISO Local datetime: " + ISO_LOCAL_DATE_TIME + ", ISO-8601 formatter: " + DEFAULT_DATETIME_FORMATTER);
    }

    private boolean printSequenceNumber;
    private boolean printSource;
    private DateTimeFormatter timestampFormatter = DEFAULT_DATETIME_FORMATTER;


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     *
     * @param printSource
     * @param timestampFormatter
     */
    public GlassFishLogFormatter(final boolean printSource, final DateTimeFormatter timestampFormatter) {
        this.printSource = printSource;
        this.timestampFormatter = timestampFormatter;
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
    }


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     *
     * @param handlerId
     * @param timestampFormatter
     * @param printSource
     */
    public GlassFishLogFormatter(final HandlerId handlerId, final boolean printSource,
        final DateTimeFormatter timestampFormatter) {
        this.printSource = printSource;
        this.timestampFormatter = timestampFormatter;
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
        configure(this, FormatterConfigurationHelper.forHandlerId(handlerId));
    }


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     *
     * @param handlerId
     */
    public GlassFishLogFormatter(final HandlerId handlerId) {
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
        configure(this, FormatterConfigurationHelper.forHandlerId(handlerId));
    }


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     */
    public GlassFishLogFormatter() {
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
    }


    private static void configure(final GlassFishLogFormatter formatter, final FormatterConfigurationHelper helper) {
        formatter.printSequenceNumber = helper.getBoolean(PRINT_SEQUENCE_NUMBER, formatter.printSequenceNumber);
        formatter.printSource = helper.getBoolean(PRINT_SOURCE, formatter.printSource);
        formatter.timestampFormatter = helper.getDateTimeFormatter(TIMESTAMP_FORMAT, formatter.timestampFormatter);
    }


    /**
     * Formats the record.
     *
     * @param record
     * @return formatted record, final record for output
     */
    protected abstract String formatRecord(LogRecord record);


    /**
     * @param printSequenceNumber true enables printing the log record sequence number
     */
    public void setPrintSequenceNumber(final boolean printSequenceNumber) {
        this.printSequenceNumber = printSequenceNumber;
    }


    /**
     * @return true enables printing the log record sequence number
     */
    public boolean isPrintSequenceNumber() {
        return printSequenceNumber;
    }


    /**
     * @param printSource if true, the source class and method will be printed to the output (but
     *            only if they are set)
     */
    public void setPrintSource(final boolean printSource) {
        this.printSource = printSource;
    }


    /**
     * @return if true, the source class and method will be printed to the output (but
     *         only if they are set)
     */
    public boolean isPrintSource() {
        return printSource;
    }


    /**
     * @return {@link DateTimeFormatter} used for timestamps
     */
    public final DateTimeFormatter getTimestampFormatter() {
        return timestampFormatter;
    }


    /**
     * @param timestampFormatter {@link DateTimeFormatter} used for timestamps. Null sets default.
     */
    public final void setTimestampFormatter(final DateTimeFormatter timestampFormatter) {
        this.timestampFormatter = timestampFormatter == null ? DEFAULT_DATETIME_FORMATTER : timestampFormatter;
    }


    /**
     * @param format The date format to set for records. Null sets default.
     *            See {@link DateTimeFormatter} for details.
     */
    public final void setTimestampFormatter(final String format) {
        setTimestampFormatter(format == null ? DEFAULT_DATETIME_FORMATTER : DateTimeFormatter.ofPattern(format));
    }


    @Override
    public String formatMessage(final LogRecord record) {
        return getPrintedMessage(MSG_RESOLVER.resolve(record));
    }


    @Override
    public final String format(final LogRecord record) {
        return formatRecord(record);
    }


    /**
     * @param record if null, this method returns null too
     * @return a record's message plus printed stacktrace if some throwable is present.
     */
    protected String getPrintedMessage(final GlassFishLogRecord record) {
        if (record == null) {
            return null;
        }
        final String message = record.getMessage();
        final String stackTrace = record.getThrownStackTrace();
        if (message == null || message.isEmpty()) {
            return stackTrace;
        }
        if (stackTrace == null) {
            return message;
        }
        return message + System.lineSeparator() + stackTrace;
    }


    /**
     * Configuration property set of this formatter
     */
    public enum GlassFishLogFormatterProperty implements LogProperty {

        /** Format used by {@link DateTimeFormatter} */
        TIMESTAMP_FORMAT("timestampFormat"),
        /** Enable printing the sequence number of the LogRecord. See {@link LogRecord#getSequenceNumber()} */
        PRINT_SEQUENCE_NUMBER("printSequenceNumber"),
        /**
         * Enable printing the source class and method of the LogRecord.
         * See {@link LogRecord#getSourceClassName()} and {@link LogRecord#getSourceMethodName()}
         */
        PRINT_SOURCE(KEY_FORMATTER_PRINT_SOURCE_SUFFIX),
        ;

        private final String propertyName;

        GlassFishLogFormatterProperty(final String propertyName) {
            this.propertyName = propertyName;
        }


        @Override
        public String getPropertyName() {
            return propertyName;
        }
    }
}

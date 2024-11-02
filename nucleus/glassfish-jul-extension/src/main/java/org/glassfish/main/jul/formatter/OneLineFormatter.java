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

import java.util.Arrays;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.main.jul.cfg.GlassFishLoggingConstants;
import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.record.GlassFishLogRecord;

import static org.glassfish.main.jul.formatter.OneLineFormatter.OneLineFormatterProperty.SIZE_CLASS;
import static org.glassfish.main.jul.formatter.OneLineFormatter.OneLineFormatterProperty.SIZE_LEVEL;
import static org.glassfish.main.jul.formatter.OneLineFormatter.OneLineFormatterProperty.SIZE_THREAD;


/**
 * Fast {@link Formatter} usable in tests or even in production if you need only simple logs with
 * time, level and messages.
 * <p>
 * Note that if you configured the formatter from your code (and not a property file),
 * if you want to source class and method automatically detected, you have to enable
 * {@link GlassFishLoggingConstants#KEY_CLASS_AND_METHOD_DETECTION_ENABLED}. Without that the output
 * will contain just class and method fields manually set to the LogRecord or using {@link Logger}
 * methods which have these parameters to do that.
 *
 * @author David Matejcek
 */
public class OneLineFormatter extends GlassFishLogFormatter {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private int sizeOfLevel = 7;
    private int sizeOfThread = 20;
    private int sizeOfClass = 60;


    /**
     * Creates formatter with default {@value GlassFishLogFormatter#ISO_LOCAL_TIME} time format if
     * not set otherwise in the logging configuration.
     *
     * @param handlerId
     */
    public OneLineFormatter(final HandlerId handlerId) {
        super(handlerId, true, ISO_LOCAL_TIME);
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
        configure(this, FormatterConfigurationHelper.forHandlerId(handlerId));
    }


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     */
    public OneLineFormatter() {
        super(true, ISO_LOCAL_TIME);
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
    }


    private static void configure(OneLineFormatter formatter, final FormatterConfigurationHelper helper) {
        formatter.sizeOfLevel = helper.getNonNegativeInteger(SIZE_LEVEL, formatter.sizeOfLevel);
        formatter.sizeOfThread = helper.getNonNegativeInteger(SIZE_THREAD, formatter.sizeOfThread);
        formatter.sizeOfClass = helper.getNonNegativeInteger(SIZE_CLASS, formatter.sizeOfClass);
    }


    @Override
    public String formatRecord(final LogRecord record) {
        return formatEnhancedLogRecord(MSG_RESOLVER.resolve(record));
    }


    @Override
    public String formatMessage(final LogRecord record) {
        throw new UnsupportedOperationException("String formatMessage(LogRecord record)");
    }


    private String formatEnhancedLogRecord(final GlassFishLogRecord record) {
        if (record.getMessage() == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(256);
        sb.append(getTimestampFormatter().format(record.getTime()));
        addPadded(record.getLevel(), this.sizeOfLevel, sb);
        addPadded(record.getThreadName(), this.sizeOfThread, sb);
        if (isPrintSource()) {
            addPadded(record.getSourceClassName(), this.sizeOfClass, sb);
            sb.append('.');
            if (record.getSourceMethodName() != null) {
                sb.append(record.getSourceMethodName());
            }
        } else {
            addPadded(record.getLoggerName(), sizeOfClass, sb);
        }
        sb.append(' ').append(record.getMessage());

        if (record.getThrown() != null) {
            sb.append(LINE_SEPARATOR);
            sb.append(record.getThrownStackTrace());
        }

        return sb.append(LINE_SEPARATOR).toString();
    }


    private void addPadded(final Object value, final int size, final StringBuilder sb) {
        final String text = value == null ? "" : String.valueOf(value);
        sb.append(' ');
        sb.append(getPad(text, size));
        sb.append(text.length() <= size ? text : text.substring(text.length() - size));
    }


    private char[] getPad(final String text, final int size) {
        final int countOfSpaces = size - text.length();
        if (countOfSpaces <= 0) {
            return new char[0];
        }
        final char[] spaces = new char[countOfSpaces];
        Arrays.fill(spaces, ' ');
        return spaces;
    }

    /**
     * Configuration property set of this formatter
     */
    public enum OneLineFormatterProperty implements LogProperty {

        /** Count of characters for the log record's level */
        SIZE_LEVEL("size.level"),
        /** Count of characters for the log record's thread name */
        SIZE_THREAD("size.thread"),
        /** Count of characters for the log record's source class */
        SIZE_CLASS("size.class"),
        ;

        private final String propertyName;

        OneLineFormatterProperty(final String propertyName) {
            this.propertyName = propertyName;
        }


        @Override
        public String getPropertyName() {
            return propertyName;
        }
    }
}

/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.formatter.ExcludeFieldsSupport.SupplementalAttribute;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.record.MessageResolver;

import static java.lang.System.lineSeparator;
import static org.glassfish.main.jul.formatter.UniformLogFormatter.UniformFormatterProperty.EXCLUDED_FIELDS;
import static org.glassfish.main.jul.formatter.UniformLogFormatter.UniformFormatterProperty.MULTILINE;
import static org.glassfish.main.jul.formatter.UniformLogFormatter.UniformFormatterProperty.RECORD_MARKER_BEGIN;
import static org.glassfish.main.jul.formatter.UniformLogFormatter.UniformFormatterProperty.RECORD_MARKER_END;
import static org.glassfish.main.jul.formatter.UniformLogFormatter.UniformFormatterProperty.SEPARATOR_FIELD;

/**
 * UniformLogFormatter conforms to the logging format defined by the
 * Log Working Group in Java Webservices Org.
 * The specified format is
 * "[#|DATETIME|LOG_LEVEL|PRODUCT_ID|LOGGER NAME|OPTIONAL KEY VALUE PAIRS|MESSAGE|#]\n"
 *
 * @author Hemanth Puttaswamy
 * @author David Matejcek - refactoring
 */
public class UniformLogFormatter extends GlassFishLogFormatter {

    private static final int REC_BUFFER_CAPACITY = 512;

    private static final String MULTILINE_INDENTATION = "  ";
    private static final char FIELD_SEPARATOR = '|';
    private static final String RECORD_BEGIN_MARKER = "[#|";
    private static final String RECORD_END_MARKER = "|#]";
    private static final String PAIR_SEPARATOR = ";";
    private static final String VALUE_SEPARATOR = "=";

    private static final String LABEL_CLASSNAME = "ClassName";
    private static final String LABEL_METHODNAME = "MethodName";
    private static final String LABEL_RECORDNUMBER = "RecordNumber";

    private final ExcludeFieldsSupport excludeFieldsSupport = new ExcludeFieldsSupport();
    private String recordBeginMarker = RECORD_BEGIN_MARKER;
    private String recordEndMarker = RECORD_END_MARKER;
    private char recordFieldSeparator = FIELD_SEPARATOR;
    private boolean multiline = true;

    // this constructor is used by reflection in HandlerConfigurationHelper
    public UniformLogFormatter(final HandlerId handlerId) {
        super(handlerId);
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
        configure(this, FormatterConfigurationHelper.forHandlerId(handlerId));
    }


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     */
    public UniformLogFormatter() {
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
    }


    private static void configure(final UniformLogFormatter formatter, final FormatterConfigurationHelper helper) {
        formatter.setExcludeFields(helper.getString(EXCLUDED_FIELDS, formatter.excludeFieldsSupport.toString()));
        formatter.setMultiline(helper.getBoolean(MULTILINE, formatter.multiline));
        formatter.setRecordFieldSeparator(helper.getCharacter(SEPARATOR_FIELD, formatter.recordFieldSeparator));
        formatter.setRecordBeginMarker(helper.getString(RECORD_MARKER_BEGIN, formatter.recordBeginMarker));
        formatter.setRecordEndMarker(helper.getString(RECORD_MARKER_END, formatter.recordEndMarker));
    }


    @Override
    public String formatRecord(final LogRecord record) {
        return formatGlassFishLogRecord(MSG_RESOLVER.resolve(record));
    }


    /**
     * @param recordBeginMarker separates log records, marks beginning of the record. Default:
     *            {@value #RECORD_BEGIN_MARKER}
     */
    public void setRecordBeginMarker(final String recordBeginMarker) {
        this.recordBeginMarker = recordBeginMarker == null ? RECORD_BEGIN_MARKER : recordBeginMarker;
    }


    /**
     * @param recordEndMarker separates log records, marks ending of the record. Default:
     *            {@value #RECORD_END_MARKER}
     */
    public void setRecordEndMarker(final String recordEndMarker) {
        this.recordEndMarker = recordEndMarker == null ? RECORD_END_MARKER : recordEndMarker;
    }


    /**
     * @param recordFieldSeparator separates log record fields, default: {@value #FIELD_SEPARATOR}
     */
    public void setRecordFieldSeparator(final Character recordFieldSeparator) {
        this.recordFieldSeparator = recordFieldSeparator == null ? FIELD_SEPARATOR : recordFieldSeparator;
    }


    /**
     * @param multiline the multiline to set
     */
    public void setMultiline(final boolean multiline) {
        this.multiline = multiline;
    }


    /**
     * @param excludeFields the excludeFields to set
     */
    public void setExcludeFields(final String excludeFields) {
        this.excludeFieldsSupport.setExcludedFields(excludeFields);
    }


    private String formatGlassFishLogRecord(final GlassFishLogRecord record) {
        try {
            final String message = getPrintedMessage(record);
            if (message == null) {
                return "";
            }

            final String timestamp = getTimestampFormatter().format(record.getTime());
            final Level logLevel = record.getLevel();
            final StringBuilder output = new StringBuilder(REC_BUFFER_CAPACITY).append(recordBeginMarker);
            appendTimestamp(output, timestamp);
            appendLogLevel(output, logLevel);
            appendProductId(output);
            appendLoggerName(output, record.getLoggerName());
            appendDetails(output, record);

            if (multiline) {
                output.append(lineSeparator());
                output.append(MULTILINE_INDENTATION);
            }
            output.append(message);
            output.append(recordEndMarker);
            output.append(lineSeparator()).append(lineSeparator());
            return output.toString();
        } catch (final Exception e) {
            new ErrorManager().error("Error in formatting Logrecord", e, ErrorManager.FORMAT_FAILURE);
            return record.getMessage();
        }
    }


    private void appendTimestamp(final StringBuilder output, final String timestamp) {
        output.append(timestamp);
        output.append(recordFieldSeparator);
    }


    private void appendLogLevel(final StringBuilder output, final Level logLevel) {
        output.append(logLevel.getName());
        output.append(recordFieldSeparator);
    }


    private void appendProductId(final StringBuilder output) {
        final String productId = LoggingSystemEnvironment.getProductId();
        if (productId != null) {
            output.append(productId);
        }
        output.append(recordFieldSeparator);
    }


    private void appendLoggerName(final StringBuilder output, final String loggerName) {
        if (loggerName != null) {
            output.append(loggerName);
        }
        output.append(recordFieldSeparator);
    }


    private void appendDetails(final StringBuilder output, final GlassFishLogRecord record) {
        if (!excludeFieldsSupport.isSet(SupplementalAttribute.TID)) {
            output.append("_ThreadID").append(VALUE_SEPARATOR).append(record.getThreadID()).append(PAIR_SEPARATOR);
            output.append("_ThreadName").append(VALUE_SEPARATOR).append(record.getThreadName()).append(PAIR_SEPARATOR);
        }

        // Include the integer level value in the log
        final Level level = record.getLevel();
        if (!excludeFieldsSupport.isSet(SupplementalAttribute.LEVEL_VALUE)) {
            output.append("_LevelValue").append(VALUE_SEPARATOR).append(level.intValue()).append(PAIR_SEPARATOR);
        }

        if (record.getMessageKey() != null) {
            output.append("_MessageID").append(VALUE_SEPARATOR).append(record.getMessageKey()).append(PAIR_SEPARATOR);
        }

        if (isPrintSource()) {
            final String sourceClassName = record.getSourceClassName();
            if (sourceClassName != null) {
                output.append(LABEL_CLASSNAME).append(VALUE_SEPARATOR).append(sourceClassName).append(PAIR_SEPARATOR);
            }

            final String sourceMethodName = record.getSourceMethodName();
            if (sourceMethodName != null) {
                output.append(LABEL_METHODNAME).append(VALUE_SEPARATOR).append(sourceMethodName).append(PAIR_SEPARATOR);
            }
        }

        if (isPrintSequenceNumber()) {
            final long recNumber = record.getSequenceNumber();
            output.append(LABEL_RECORDNUMBER).append(VALUE_SEPARATOR).append(recNumber).append(PAIR_SEPARATOR);
        }

        output.append(recordFieldSeparator);
    }

    /**
     * Configuration property set of this formatter
     */
    public enum UniformFormatterProperty implements LogProperty {

        /** Excluded fields from the output */
        EXCLUDED_FIELDS("excludedFields"),
        /** If false, each log record is on a single line (except messages containing new lines) */
        MULTILINE("multiline"),
        /** Character between record fields */
        SEPARATOR_FIELD("fieldSeparator"),
        /** Record prefix */
        RECORD_MARKER_BEGIN("recordMarker.begin"),
        /** Record suffix */
        RECORD_MARKER_END("recordMarker.end"),
        ;

        private final String propertyName;

        UniformFormatterProperty(final String propertyName) {
            this.propertyName = propertyName;
        }


        @Override
        public String getPropertyName() {
            return propertyName;
        }
    }
}

/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.formatter.ExcludeFieldsSupport.SupplementalAttribute;
import org.glassfish.main.jul.record.GlassFishLogRecord;

import static java.lang.System.lineSeparator;
import static org.glassfish.main.jul.formatter.ODLLogFormatter.ODLFormatterProperty.EXCLUDED_FIELDS;
import static org.glassfish.main.jul.formatter.ODLLogFormatter.ODLFormatterProperty.FIELD_SEPARATOR;
import static org.glassfish.main.jul.formatter.ODLLogFormatter.ODLFormatterProperty.MULTILINE;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.error;


/**
 * ODLLogFormatter (Oracle Diagnostic Logging Format) conforms to the logging format defined
 * by the Log Working Group in Oracle.
 * The specified format is
 *
 * <pre>
 * [[Timestamp] [Product ID] [Level] [Message ID] [Logger Name] [Thread ID] [Extra Attributes] [Message]]\n
 * </pre>
 *
 * @author Naman Mehta
 * @author David Matejcek - refactoring
 */
public class ODLLogFormatter extends GlassFishLogFormatter {

    private static final int REC_BUFFER_CAPACITY = 512;

    private static final String FIELD_BEGIN_MARKER = "[";
    private static final String FIELD_END_MARKER = "]";
    private static final String DEFAULT_FIELD_SEPARATOR = " ";
    private static final String MULTILINE_INDENTATION = "  ";

    private static final String LABEL_CLASSNAME = "CLASSNAME";
    private static final String LABEL_METHODNAME = "METHODNAME";
    private static final String LABEL_RECORDNUMBER = "RECORDNUMBER";

    private final ExcludeFieldsSupport excludeFieldsSupport = new ExcludeFieldsSupport();
    private String fieldSeparator = DEFAULT_FIELD_SEPARATOR;
    private boolean multiline = true;

    public ODLLogFormatter(final HandlerId handlerId) {
        super(handlerId);
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
        configure(this, FormatterConfigurationHelper.forHandlerId(handlerId));
    }


    /**
     * Creates an instance and initializes defaults from log manager's configuration
     */
    public ODLLogFormatter() {
        configure(this, FormatterConfigurationHelper.forFormatterClass(getClass()));
    }


    private static void configure(final ODLLogFormatter formatter, final FormatterConfigurationHelper helper) {
        formatter.setExcludeFields(helper.getString(EXCLUDED_FIELDS, formatter.excludeFieldsSupport.toString()));
        formatter.multiline = helper.getBoolean(MULTILINE, formatter.multiline);
        formatter.fieldSeparator = helper.getString(FIELD_SEPARATOR, formatter.fieldSeparator);
    }


    @Override
    public String formatRecord(final LogRecord record) {
        return formatGlassFishLogRecord(MSG_RESOLVER.resolve(record));
    }


    /**
     * @param excludeFields comma separated field names which should not be in the ouptut
     */
    public void setExcludeFields(final String excludeFields) {
        this.excludeFieldsSupport.setExcludedFields(excludeFields);
    }


    /**
     * @param multiline true if the log message is on the next line. Default: true.
     */
    public void setMultiline(final boolean multiline) {
        this.multiline = multiline;
    }


    private String formatGlassFishLogRecord(final GlassFishLogRecord record) {
        try {
            final String message = getPrintedMessage(record);
            if (message == null) {
                return "";
            }
            final boolean forceMultiline = multiline || message.indexOf('\n') >= 0;
            final Level logLevel = record.getLevel();
            final String msgId = record.getMessageKey();
            final String loggerName = record.getLoggerName();
            final String threadName = record.getThreadName();
            final StringBuilder output = new StringBuilder(REC_BUFFER_CAPACITY);
            appendTimestamp(output, record.getTime());
            appendProductId(output);
            appendLogLevel(output, logLevel);
            appendMessageKey(output, msgId);
            appendLoggerName(output, loggerName);
            appendThread(output, record.getThreadID(), threadName);
            appendLogLevelAsInt(output, logLevel);
            appendSequenceNumber(output, record.getSequenceNumber());
            appendSource(output, record.getSourceClassName(), record.getSourceMethodName());

            if (forceMultiline) {
                output.append(FIELD_BEGIN_MARKER).append(FIELD_BEGIN_MARKER);
                output.append(lineSeparator());
                output.append(MULTILINE_INDENTATION);
            }
            output.append(message);
            if (forceMultiline) {
                output.append(FIELD_END_MARKER).append(FIELD_END_MARKER);
            }
            output.append(lineSeparator()).append(lineSeparator());
            return output.toString();
        } catch (final Exception e) {
            error(getClass(), "Error in formatting Logrecord", e);
            return record.getMessage();
        }
    }

    private void appendTimestamp(final StringBuilder output, final OffsetDateTime timestamp) {
        output.append(FIELD_BEGIN_MARKER);
        output.append(getTimestampFormatter().format(timestamp));
        output.append(FIELD_END_MARKER).append(fieldSeparator);
    }

    private void appendProductId(final StringBuilder output) {
        output.append(FIELD_BEGIN_MARKER);
        final String productId = LoggingSystemEnvironment.getProductId();
        if (productId != null) {
            output.append(productId);
        }
        output.append(FIELD_END_MARKER).append(fieldSeparator);
    }

    private void appendLogLevel(final StringBuilder output, final Level logLevel) {
        output.append(FIELD_BEGIN_MARKER);
        output.append(logLevel.getName());
        output.append(FIELD_END_MARKER).append(fieldSeparator);
    }

    private void appendMessageKey(final StringBuilder output, final String msgId) {
        output.append(FIELD_BEGIN_MARKER);
        if (msgId != null) {
            output.append(msgId);
        }
        output.append(FIELD_END_MARKER).append(fieldSeparator);
    }

    private void appendLoggerName(final StringBuilder output, final String loggerName) {
        output.append(FIELD_BEGIN_MARKER);
        if (loggerName != null) {
            output.append(loggerName);
        }
        output.append(FIELD_END_MARKER).append(fieldSeparator);
    }

    private void appendThread(final StringBuilder output, final int threadId, final String threadName) {
        if (!excludeFieldsSupport.isSet(SupplementalAttribute.TID)) {
            output.append(FIELD_BEGIN_MARKER);
            output.append("tid: ").append("_ThreadID=").append(threadId).append(" _ThreadName=").append(threadName);
            output.append(FIELD_END_MARKER).append(fieldSeparator);
        }
    }

    private void appendLogLevelAsInt(final StringBuilder output, final Level logLevel) {
        if (!excludeFieldsSupport.isSet(SupplementalAttribute.LEVEL_VALUE)) {
            output.append(FIELD_BEGIN_MARKER);
            output.append("levelValue: ").append(logLevel.intValue());
            output.append(FIELD_END_MARKER).append(fieldSeparator);
        }
    }

    private void appendSequenceNumber(final StringBuilder output, final long sequenceNumber) {
        if (isPrintSequenceNumber()) {
            output.append(FIELD_BEGIN_MARKER);
            output.append(LABEL_RECORDNUMBER).append(": ").append(sequenceNumber);
            output.append(FIELD_END_MARKER).append(fieldSeparator);
        }
    }

    private void appendSource(final StringBuilder output, final String className, final String methodName) {
        if (!isPrintSource()) {
            return;
        }
        if (className != null) {
            output.append(FIELD_BEGIN_MARKER);
            output.append(LABEL_CLASSNAME).append(": ").append(className);
            output.append(FIELD_END_MARKER).append(fieldSeparator);
        }
        if (methodName != null) {
            output.append(FIELD_BEGIN_MARKER);
            output.append(LABEL_METHODNAME).append(": ").append(methodName);
            output.append(FIELD_END_MARKER).append(fieldSeparator);
        }
    }

    /**
     * Configuration property set of this formatter
     */
    public enum ODLFormatterProperty implements LogProperty {

        /** Excluded fields from the output */
        EXCLUDED_FIELDS("excludedFields"),
        /** If false, each log record is on a single line (except messages containing new lines) */
        MULTILINE("multiline"),
        /** Character between record fields */
        FIELD_SEPARATOR("fieldSeparator"),
        ;

        private final String propertyName;

        ODLFormatterProperty(final String propertyName) {
            this.propertyName = propertyName;
        }


        @Override
        public String getPropertyName() {
            return propertyName;
        }
    }
}

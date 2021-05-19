/*
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

package com.sun.enterprise.server.logging;


import com.sun.appserv.server.util.Version;
import org.jvnet.hk2.annotations.ContractsProvided;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * ODLLogFormatter conforms to the logging format defined by the
 * Log Working Group in Oracle.
 * The specified format is
 * "[[timestamp] [organization ID] [Message Type/Level] [Message ID] [Logger
 * Name] [Thread ID] [User ID] [ECID] [Extra Attributes] [Message]]\n"
 *
 * @author Naman Mehta
 */
@Service()
@ContractsProvided({ODLLogFormatter.class, Formatter.class})
@PerLookup
public class ODLLogFormatter extends Formatter implements LogEventBroadcaster {

    // loggerResourceBundleTable caches references to all the ResourceBundle
    // and can be searched using the LoggerName as the key
    private Map<String, ResourceBundle> loggerResourceBundleTable;

    private LogManager logManager;

    private static boolean LOG_SOURCE_IN_KEY_VALUE = false;

    private static boolean RECORD_NUMBER_IN_KEY_VALUE = false;

    private static String userID = "";

    private static String ecID = "";

    private FormatterDelegate _delegate = null;

    private UniformLogFormatter uniformLogFormatter = new UniformLogFormatter();

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static {
        String logSource = System.getProperty(
                "com.sun.aas.logging.keyvalue.logsource");
        if ((logSource != null)
                && (logSource.equals("true"))) {
            LOG_SOURCE_IN_KEY_VALUE = true;
        }

        String recordCount = System.getProperty(
                "com.sun.aas.logging.keyvalue.recordnumber");
        if ((recordCount != null)
                && (recordCount.equals("true"))) {
            RECORD_NUMBER_IN_KEY_VALUE = true;
        }

        userID = System.getProperty("com.sun.aas.logging.userID");

        ecID = System.getProperty("com.sun.aas.logging.ecID");
    }

    private long recordNumber = 0;

    private String recordFieldSeparator;
    private String recordDateFormat;

    private LogEventBroadcaster logEventBroadcasterDelegate;

    private boolean multiLineMode;

    private ExcludeFieldsSupport excludeFieldsSupport = new ExcludeFieldsSupport();

    private static final String FIELD_BEGIN_MARKER = "[";
    private static final String FIELD_END_MARKER = "]";

    private static final char FIELD_SEPARATOR = ' ';

    private static final String RFC_3339_DATE_FORMAT =
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final String INDENT = "  ";

    public ODLLogFormatter() {
        super();
        loggerResourceBundleTable = new HashMap<String,ResourceBundle>();
        logManager = LogManager.getLogManager();
    }

    public ODLLogFormatter(FormatterDelegate delegate) {
        this();
        _delegate = delegate;
    }


    public void setDelegate(FormatterDelegate delegate) {
        _delegate = delegate;
    }

    /**
     * _REVISIT_: Replace the String Array with an HashMap and do some
     * benchmark to determine whether StringCat is faster or Hashlookup for
     * the template is faster.
     */


    @Override
    public String format(LogRecord record) {
        return odlLogFormat(record);
    }

    @Override
    public String formatMessage(LogRecord record) {
        return odlLogFormat(record);
    }


    /**
     * GlassFish can override to specify their product version
     */
    protected String getProductId() {

        String version = Version.getAbbreviatedVersion() + Version.getVersionPrefix() +
                Version.getMajorVersion() + "." + Version.getMinorVersion();
        return (version);
    }


    /**
     * Note: This method is not synchronized, we are assuming that the
     * synchronization will happen at the Log Handler.publish( ) method.
     */
    private String odlLogFormat(LogRecord record) {

        try {

            LogEventImpl logEvent = new LogEventImpl();

            // creating message from log record using resource bundle and appending parameters
            String message = getLogMessage(record);
            if (message == null || message.isEmpty()) {
                return "";
            }
            boolean multiLine = multiLineMode || isMultiLine(message);

            // Starting formatting message
            // Adding record begin marker
            StringBuilder recordBuffer = new StringBuilder();

            // A Dummy Container Date Object is used to format the date
            Date date = new Date();

            // Adding timestamp
            SimpleDateFormat dateFormatter = new SimpleDateFormat(getRecordDateFormat() != null ? getRecordDateFormat() : RFC_3339_DATE_FORMAT);
            date.setTime(record.getMillis());
            recordBuffer.append(FIELD_BEGIN_MARKER);
            String timestamp=dateFormatter.format(date);
            logEvent.setTimestamp(timestamp);
            recordBuffer.append(timestamp);
            recordBuffer.append(FIELD_END_MARKER);
            recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);

            // Adding organization ID
            recordBuffer.append(FIELD_BEGIN_MARKER);
            logEvent.setComponentId(uniformLogFormatter.getProductId());
            recordBuffer.append(uniformLogFormatter.getProductId());
            recordBuffer.append(FIELD_END_MARKER);
            recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);

            // Adding messageType
            Level logLevel = record.getLevel();
            recordBuffer.append(FIELD_BEGIN_MARKER);
            String odlLevel = logLevel.getName();
            logEvent.setLevel(odlLevel);
            recordBuffer.append(odlLevel);
            recordBuffer.append(FIELD_END_MARKER);
            recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);

            // Adding message ID
            recordBuffer.append(FIELD_BEGIN_MARKER);
            String msgId  = UniformLogFormatter.getMessageId(record);
            recordBuffer.append((msgId == null) ? "" : msgId);
            logEvent.setMessageId(msgId);
            recordBuffer.append(FIELD_END_MARKER);
            recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);

            // Adding logger Name / module Name
            recordBuffer.append(FIELD_BEGIN_MARKER);
            String loggerName = record.getLoggerName();
            loggerName = (loggerName == null) ? "" : loggerName;
            recordBuffer.append(loggerName);
            logEvent.setLogger(loggerName);
            recordBuffer.append(FIELD_END_MARKER);
            recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);

            // Adding thread ID
            if (!excludeFieldsSupport.isSet(ExcludeFieldsSupport.SupplementalAttribute.TID)) {
                recordBuffer.append(FIELD_BEGIN_MARKER);
                recordBuffer.append("tid: _ThreadID=");
                recordBuffer.append(record.getThreadID());
                logEvent.setThreadId(record.getThreadID());
                String threadName;
                if (record instanceof GFLogRecord) {
                    threadName = ((GFLogRecord)record).getThreadName();
                } else {
                    threadName = Thread.currentThread().getName();
                }
                recordBuffer.append(" _ThreadName=");
                logEvent.setThreadName(threadName);
                recordBuffer.append(threadName);
                recordBuffer.append(FIELD_END_MARKER);
                recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
            }

            // Adding user ID
            if (!excludeFieldsSupport.isSet(ExcludeFieldsSupport.SupplementalAttribute.USERID) &&
                    userID != null && !("").equals(userID.trim()))
            {
                recordBuffer.append(FIELD_BEGIN_MARKER);
                recordBuffer.append("userId: ");
                logEvent.setUser(userID);
                recordBuffer.append(userID);
                recordBuffer.append(FIELD_END_MARKER);
                recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
            }

            // Adding ec ID
            if (!excludeFieldsSupport.isSet(ExcludeFieldsSupport.SupplementalAttribute.ECID) &&
                    ecID != null && !("").equals(ecID.trim()))
            {
                recordBuffer.append(FIELD_BEGIN_MARKER);
                recordBuffer.append("ecid: ");
                logEvent.setECId(ecID);
                recordBuffer.append(ecID);
                recordBuffer.append(FIELD_END_MARKER);
                recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
            }

            // Include the raw time stamp
            if (!excludeFieldsSupport.isSet(ExcludeFieldsSupport.SupplementalAttribute.TIME_MILLIS)) {
                recordBuffer.append(FIELD_BEGIN_MARKER);
                recordBuffer.append("timeMillis: ");
                logEvent.setTimeMillis(record.getMillis());
                recordBuffer.append(record.getMillis());
                recordBuffer.append(FIELD_END_MARKER);
                recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
            }

            // Include the level value
            if (!excludeFieldsSupport.isSet(ExcludeFieldsSupport.SupplementalAttribute.LEVEL_VALUE)) {
                recordBuffer.append(FIELD_BEGIN_MARKER);
                recordBuffer.append("levelValue: ");
                logEvent.setLevelValue(logLevel.intValue());
                recordBuffer.append(logLevel.intValue());
                recordBuffer.append(FIELD_END_MARKER);
                recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
            }

            // Adding extra Attributes - record number
            if (RECORD_NUMBER_IN_KEY_VALUE) {
                recordBuffer.append(FIELD_BEGIN_MARKER);
                recordNumber++;
                recordBuffer.append("RECORDNUMBER: ");
                logEvent.getSupplementalAttributes().put("RECORDNUMBER", recordNumber);
                recordBuffer.append(recordNumber);
                recordBuffer.append(FIELD_END_MARKER);
                recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
            }

            // Adding extra Attributes - class name and method name for FINE and higher level messages
            Level level = record.getLevel();
            if (LOG_SOURCE_IN_KEY_VALUE ||
                    (level.intValue() <= Level.FINE.intValue())) {
                String sourceClassName = record.getSourceClassName();
                if (sourceClassName != null && !sourceClassName.isEmpty()) {
                    recordBuffer.append(FIELD_BEGIN_MARKER);
                    recordBuffer.append("CLASSNAME: ");
                    logEvent.getSupplementalAttributes().put("CLASSNAME", sourceClassName);
                    recordBuffer.append(sourceClassName);
                    recordBuffer.append(FIELD_END_MARKER);
                    recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
                }
                String sourceMethodName = record.getSourceMethodName();
                if (sourceMethodName != null && !sourceMethodName.isEmpty()) {
                    recordBuffer.append(FIELD_BEGIN_MARKER);
                    recordBuffer.append("METHODNAME: ");
                    logEvent.getSupplementalAttributes().put("METHODNAME", sourceMethodName);
                    recordBuffer.append(sourceMethodName);
                    recordBuffer.append(FIELD_END_MARKER);
                    recordBuffer.append(getRecordFieldSeparator() != null ? getRecordFieldSeparator() : FIELD_SEPARATOR);
                }
            }

            if (_delegate != null) {
                _delegate.format(recordBuffer, level);
            }

            if (multiLine) {
                recordBuffer.append(FIELD_BEGIN_MARKER).append(FIELD_BEGIN_MARKER);
                recordBuffer.append(LINE_SEPARATOR);
                recordBuffer.append(INDENT);
            }
            recordBuffer.append(message);
            logEvent.setMessage(message);
            if (multiLine) {
                recordBuffer.append(FIELD_END_MARKER).append(FIELD_END_MARKER);
            }
            recordBuffer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
            informLogEventListeners(logEvent);
            return recordBuffer.toString();
        } catch (Exception ex) {
            new ErrorManager().error(
                    "Error in formatting Logrecord", ex,
                    ErrorManager.FORMAT_FAILURE);
            // We've already notified the exception, the following
            // return is to keep javac happy
            return "";
        }
    }

    private boolean isMultiLine(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String[] lines = message.split(LINE_SEPARATOR);
        return (lines.length > 1);
    }

    private String getLogMessage(LogRecord record) throws IOException {
        String logMessage = record.getMessage();
        if (logMessage == null) {
            logMessage = "";
        }
        if (logMessage.indexOf("{0") >= 0 && logMessage.contains("}") && record.getParameters() != null) {
            // If we find {0} or {1} etc., in the message, then it's most
            // likely finer level messages for Method Entry, Exit etc.,
            logMessage = java.text.MessageFormat.format(
                    logMessage, record.getParameters());
        } else {
            ResourceBundle rb = getResourceBundle(record.getLoggerName());
            if (rb != null && rb.containsKey(logMessage)) {
                try {
                    logMessage = MessageFormat.format(
                            rb.getString(logMessage),
                            record.getParameters());
                } catch (java.util.MissingResourceException e) {
                    // If we don't find an entry, then we are covered
                    // because the logMessage is initialized already
                }
            }
        }
        Throwable throwable = UniformLogFormatter.getThrowable(record);
        if (throwable != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(logMessage);
            buffer.append(LINE_SEPARATOR);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.close();
            buffer.append(sw.toString());
            logMessage = buffer.toString();
            sw.close();
        }
        return logMessage;
    }

    private synchronized ResourceBundle getResourceBundle(String loggerName) {
        if (loggerName == null) {
            return null;
        }
        ResourceBundle rb = loggerResourceBundleTable.get(
                loggerName);
        /*
                * Note that logManager.getLogger(loggerName) untrusted code may create loggers with
                * any arbitrary names this method should not be relied on so added code for checking null.
                */
        if (rb == null && logManager.getLogger(loggerName) != null) {
            rb = logManager.getLogger(loggerName).getResourceBundle();
            loggerResourceBundleTable.put(loggerName, rb);
        }
        return rb;
    }

    public String getRecordFieldSeparator() {
        return recordFieldSeparator;
    }

    public void setRecordFieldSeparator(String recordFieldSeparator) {
        this.recordFieldSeparator = recordFieldSeparator;
    }

    public String getRecordDateFormat() {
        return recordDateFormat;
    }

    public void setRecordDateFormat(String recordDateFormat) {
        this.recordDateFormat = recordDateFormat;
    }

    public String getMessageWithoutMessageID(String message) {
        String messageID = "";
        if (message.contains(": ")) {
            StringTokenizer st = new StringTokenizer(message, ":");
            messageID = st.nextToken();
            if (messageID.contains(" ")) {
                // here message ID is not proper value so returning original message only
                return message;
            } else {
                // removing message Id and returning message
                message = st.nextToken();
                return message.substring(1, message.length());
            }
        }
        return message;
    }

    void setLogEventBroadcaster(LogEventBroadcaster logEventBroadcaster) {
        logEventBroadcasterDelegate = logEventBroadcaster;
    }

    @Override
    public void informLogEventListeners(LogEvent logEvent) {
        if (logEventBroadcasterDelegate != null) {
            logEventBroadcasterDelegate.informLogEventListeners(logEvent);
        }
    }

    /**
     * @param multiLineMode the multiLineMode to set
     */
    void setMultiLineMode(boolean value) {
        this.multiLineMode = value;
    }

    void setExcludeFields(String excludeFields) {
        excludeFieldsSupport.setExcludeFields(excludeFields);
    }

}

/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.mbean;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.server.logging.ServerLogFileManager;
import com.sun.enterprise.server.logging.diagnostics.MessageIdCatalog;
import com.sun.enterprise.server.logging.logviewer.backend.LogFilter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.ObjectName;

import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.util.InjectedValues;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.ThrowableMapper;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.admin.amx.util.jmx.NotificationBuilder;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.jul.handler.GlassFishLogHandlerProperty;
import org.glassfish.server.ServerEnvironmentImpl;

import static org.glassfish.admin.amx.logging.LogAnalyzer.SEVERE_COUNT_KEY;
import static org.glassfish.admin.amx.logging.LogAnalyzer.TIMESTAMP_KEY;
import static org.glassfish.admin.amx.logging.LogAnalyzer.WARNING_COUNT_KEY;
import static org.glassfish.admin.amx.logging.LogFileAccess.ACCESS_KEY;
import static org.glassfish.admin.amx.logging.LogFileAccess.MOST_RECENT_NAME;
import static org.glassfish.admin.amx.logging.LogFileAccess.SERVER_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.ALL_LOG_RECORD_NOTIFICATION_TYPES;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_AS_STRING_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_CONFIG_NOTIFICATION_TYPE;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_FINER_NOTIFICATION_TYPE;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_FINEST_NOTIFICATION_TYPE;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_FINE_NOTIFICATION_TYPE;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_INFO_NOTIFICATION_TYPE;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_LEVEL_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_LOGGER_NAME_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_MESSAGE_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_MILLIS_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_ROOT_CAUSE_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_SEQUENCE_NUMBER_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_SEVERE_NOTIFICATION_TYPE;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_SOURCE_CLASS_NAME_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_SOURCE_METHOD_NAME_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_THREAD_ID_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_THROWN_KEY;
import static org.glassfish.admin.amx.logging.LogRecordEmitter.LOG_RECORD_WARNING_NOTIFICATION_TYPE;
import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_ROOT_HANDLERS;


public final class LoggingImpl extends AMXImplBase {

    private static MBeanNotificationInfo[] SELF_NOTIFICATION_INFOS = null;

    private final Map<Level, String> mLevelToNotificationTypeMap;
    private final Map<String, NotificationBuilder> mNotificationTypeToNotificationBuilderMap;
    private final LoggingConfigImpl loggingConfig;
    private final ServerLogFileManager logManagerService;
    private final LogFilter logFilter;
    private final MessageIdCatalog msgIdCatalog;
    private final Logger logger;
    private final ServiceLocator mHabitat;

    /**
     * Used internally to get the Logging ObjectName for a particular server
     * Logging MBean is a special-case because it needs to load as early as
     * possible.
     */
    public static ObjectName getObjectName(final String serverName) {
        final String requiredProps = Util.makeRequiredProps(Util.deduceType(Logging.class), serverName);
        final String ServerRootMonitorType = "ServerRootMonitor";
        final String parentProp = Util.makeProp(ServerRootMonitorType, serverName);
        final String props = Util.concatenateProps(requiredProps, parentProp);

        return Util.newObjectName(AMXGlassfish.DEFAULT.amxJMXDomain(), props);
    }

    public LoggingImpl(final ObjectName parent, final String serverName) {
        super(parent, Logging.class);

        mLevelToNotificationTypeMap = initLevelToNotificationTypeMap();
        mNotificationTypeToNotificationBuilderMap = new HashMap<>();
        final ServerEnvironmentImpl env = InjectedValues.getInstance().getServerEnvironment();
        loggingConfig = new LoggingConfigImpl();
        loggingConfig.setupConfigDir(env.getConfigDirPath(), env.getLibPath());
        msgIdCatalog = new MessageIdCatalog();
        mHabitat = InjectedValues.getInstance().getHabitat();
        logManagerService = mHabitat.getService(ServerLogFileManager.class);
        logFilter = mHabitat.getService(LogFilter.class);
        logger = Logger.getAnonymousLogger();

    }


    /**
     * getMBeanInfo() can be called frequently. By making this static, we avoid
     * needlessly creating new Objects.
     */
    private static synchronized MBeanNotificationInfo[] getSelfNotificationInfos() {
        if (SELF_NOTIFICATION_INFOS == null) {
            final String[] types = SetUtil.toStringArray(ALL_LOG_RECORD_NOTIFICATION_TYPES);
            final MBeanNotificationInfo selfInfo = new MBeanNotificationInfo(
                    types, Notification.class.getName(), "LogRecord notifications");

            SELF_NOTIFICATION_INFOS = new MBeanNotificationInfo[]{selfInfo};
        }
        return (SELF_NOTIFICATION_INFOS);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        final MBeanNotificationInfo[] superInfos = super.getNotificationInfo();
        return JMXUtil.mergeMBeanNotificationInfos(superInfos, getSelfNotificationInfos());
    }

    private void unimplemented() {
        throw new RuntimeException("Not implemented.");
    }


    public void setModuleLogLevel(final String module, final String level) {
        try {
            loggingConfig.setLoggingProperty(module + ".level", level);
        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE, "Can not set module log level");
        }
    }

    public String getModuleLogLevel(final String module) {
        try {
            Map<String, String> props = loggingConfig.getLoggingProperties();
            if (props != null) {
                return props.get(module + ".level");
            }
            return null;
        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE, "Can not get module log level");
            return null;
        }
    }

    public Map<String, String> getLoggingProperties() {
        try {
            Map<String, String> props = loggingConfig.getLoggingProperties();
            return props;
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "Can not get module log level");
            return null;
        }
    }

    public void updateLoggingProperties(final Map<String, String> properties) {
        try {
            loggingConfig.updateLoggingProperties(properties);
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "Can not get module log level");
        }
    }

    public int getLogLevelListenerCount(final Level logLevel) {
        final String notifType = logLevelToNotificationType(logLevel);

        final int count = getNotificationEmitter().getNotificationTypeListenerCount(notifType);
        return (count);
    }

    public String[] getLogFileKeys() {
        unimplemented();
        return new String[]{SERVER_KEY, ACCESS_KEY};
    }

    public synchronized String[] getLogFileNames(final String key) {
        if (!SERVER_KEY.equals(key)) {
            throw new IllegalArgumentException(key);
        }
        return null;
    }

    public Map<String, String> getLoggingAttributes() {
        try {
            final Map<String, String> props = loggingConfig.getLoggingProperties();
            if (props == null) {
                return null;
            }
            final Map<String, String> attributes = new HashMap<>();
            attributes.put(KEY_ROOT_HANDLERS.getPropertyName(), props.get(KEY_ROOT_HANDLERS.getPropertyName()));
            Arrays.stream(GlassFishLogHandlerProperty.values()).map(GlassFishLogHandlerProperty::getPropertyFullName)
                .forEach(k -> attributes.put(k, props.get(k)));
            return attributes;
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "Can not get logging attributes");
            return null;
        }
    }

    public void updateLoggingAttributes(final Map<String, String> properties) {
        try {
            loggingConfig.updateLoggingProperties(properties);
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "Can not set logging attributes");
        }
    }

    public synchronized void rotateAllLogFiles() {
        logManagerService.roll();
    }

    public synchronized void rotateLogFile(final String key) {

        if (ACCESS_KEY.equals(key)) {
            throw new IllegalArgumentException("not supported: " + key);
        } else if (SERVER_KEY.equals(key)) {
            logManagerService.roll();
        } else {
            throw new IllegalArgumentException("" + key);
        }
    }

    private Properties attributesToProps(List<Attribute> attrs) {
        final Properties props = new Properties();

        if (attrs != null) {
            for (Attribute attr : attrs) {
                final Object value = attr.getValue();
                if (value == null) {
                    throw new IllegalArgumentException(attr.getName() + "=" + null);
                }

                props.put(attr.getName(), value.toString());
            }
        }

        return (props);
    }

    private List<Serializable[]> convertQueryResult(final AttributeList queryResult) {
        // extract field descriptions into a String[]
        final AttributeList fieldAttrs = (AttributeList) ((Attribute) queryResult.get(0)).getValue();
        final String[] fieldHeaders = new String[fieldAttrs.size()];
        for (int i = 0; i < fieldHeaders.length; ++i) {
            final Attribute attr = (Attribute) fieldAttrs.get(i);
            fieldHeaders[ i] = (String) attr.getValue();
        }

        final List<List<Serializable>> srcRecords = TypeCast.asList(
                ((Attribute) queryResult.get(1)).getValue());

        // create the new results, making the first Object[] be the field headers
        final List<Serializable[]> results = new ArrayList<>(srcRecords.size());
        results.add(fieldHeaders);

        // extract every record
        for (int recordIdx = 0; recordIdx < srcRecords.size(); ++recordIdx) {
            final List<Serializable> record = srcRecords.get(recordIdx);

            assert (record.size() == fieldHeaders.length);
            final Serializable[] fieldValues = new Serializable[fieldHeaders.length];
            for (int fieldIdx = 0; fieldIdx < fieldValues.length; ++fieldIdx) {
                fieldValues[ fieldIdx] = record.get(fieldIdx);
            }

            results.add(fieldValues);
        }

        return results;
    }

    // code in LogBean.java code in v2
    public List<Serializable[]> queryServerLog(
            String name,
            long startIndex,
            boolean searchForward,
            int maximumNumberOfResults,
            Long fromTime,
            Long toTime,
            String logLevel,
            Set<String> modules,
            List<Attribute> nameValuePairs,
            String anySearch) {
        final List<Serializable[]> result = queryServerLogInternal(name, startIndex, searchForward,
            maximumNumberOfResults, fromTime, toTime, logLevel, modules, nameValuePairs, anySearch);
        return result;
    }

    private List<Serializable[]> queryServerLogInternal(
            final String name,
            final long startIndex,
            final boolean searchForward,
            final int maximumNumberOfResults,
            final Long fromTime,
            final Long toTime,
            final String logLevel,
            final Set<String> modules,
            final List<Attribute> nameValuePairs,
            final String anySearch) {

        if (name == null) {
            throw new IllegalArgumentException("use MOST_RECENT_NAME, not null");
        }

        boolean sortAscending = true;
        List<String> moduleList = null;
        if (modules != null) {
            moduleList = ListUtil.newListFromCollection(modules);
        }
        final Properties props = attributesToProps(nameValuePairs);

        String actualName;
        if (MOST_RECENT_NAME.equals(name)) {
            actualName = null;
        } else {
            actualName = name;
        }
        if (!searchForward) {
            sortAscending = false;
        }

        final AttributeList result = logFilter.getLogRecordsUsingQuery(actualName,
                Long.valueOf(startIndex),
                searchForward, sortAscending,
                maximumNumberOfResults,
                fromTime == null ? null : Instant.ofEpochMilli(fromTime),
                toTime == null ? null : Instant.ofEpochMilli(toTime),
                logLevel, false, moduleList, props, anySearch);


        return convertQueryResult(result);
    }

    public Map<String, Number>[] getErrorInfo() {
        unimplemented();

        final List<Map<String, Object>> infos = new ArrayList<>(); //getLogMBean().getErrorInformation();

        final Map<String, Number>[] results = TypeCast.asArray(new HashMap[infos.size()]);

        for (int i = 0; i < results.length; ++i) {
            final Map<String, Object> info = infos.get(i);

            assert (info.keySet().size() == 3);

            final Long timestamp = Long.parseLong(info.get(TIMESTAMP_KEY).toString());
            final Integer severeCount = Integer.parseInt(info.get(SEVERE_COUNT_KEY).toString());
            final Integer warningCount = Integer.parseInt(info.get(WARNING_COUNT_KEY).toString());

            final Map<String, Number> item = new HashMap<>(info.size());
            item.put(TIMESTAMP_KEY, timestamp);
            item.put(SEVERE_COUNT_KEY, severeCount);
            item.put(WARNING_COUNT_KEY, warningCount);

            results[ i] = item;
        }

        return results;
    }


    public void setKeepErrorStatisticsForIntervals(final int num) {
        unimplemented();
        //getLogMBean().setKeepErrorStatisticsForIntervals( num );
    }

    public int getKeepErrorStatisticsForIntervals() {
        return 0;
        /*
         unimplemented();
         return getLogMBean().getKeepErrorStatisticsForIntervals();
         */
    }

    public void setErrorStatisticsIntervalMinutes(final long minutes) {
        unimplemented();
        //getLogMBean().setErrorStatisticsIntervalDuration( minutes );
    }

    public long getErrorStatisticsIntervalMinutes() {
        return 0;
        /*
         unimplemented();
         return getLogMBean().getErrorStatisticsIntervalDuration();
         */
    }

    public String[] getLoggerNames() {
        return EMPTY_STRING_ARRAY;
        /*unimplemented();
         final List<String>  names   =
         TypeCast.checkList( getLogMBean().getLoggerNames(), String.class );

         return names.toArray( EMPTY_STRING_ARRAY ); */
    }

    public String[] getLoggerNamesUnder(final String loggerName) {
        unimplemented();
        /*
         final List<String>  names   = TypeCast.checkList(
         getLogMBean().getLoggerNamesUnder( loggerName ), String.class );

         return names.toArray( EMPTY_STRING_ARRAY );
         */
        return null;
    }

    public String[] getDiagnosticCauses(final String messageID, final String moduleName) {

        final List<String> causes = msgIdCatalog.getDiagnosticCausesForMessageId(messageID, moduleName);

        String[] result = null;
        if (causes != null) {
            result = CollectionUtil.toArray(causes, String.class);
        }

        return result;


    }

    public String[] getDiagnosticChecks(String messageID, String moduleName) {

        final List<String> checks = msgIdCatalog.getDiagnosticChecksForMessageId(messageID, moduleName);

        String[] result = null;
        if (checks != null) {
            result = CollectionUtil.toArray(checks, String.class);
        }

        return result;
    }

    public String getDiagnosticURI(final String messageID) {
        unimplemented();
        return null; //getLogMBean().getDiagnosticURIForMessageId( messageID );
    }
    private static final Object[] LEVELS_AND_NOTIF_TYPES = new Object[]{
        Level.SEVERE, LOG_RECORD_SEVERE_NOTIFICATION_TYPE,
        Level.WARNING, LOG_RECORD_WARNING_NOTIFICATION_TYPE,
        Level.INFO, LOG_RECORD_INFO_NOTIFICATION_TYPE,
        Level.CONFIG, LOG_RECORD_CONFIG_NOTIFICATION_TYPE,
        Level.FINE, LOG_RECORD_FINE_NOTIFICATION_TYPE,
        Level.FINER, LOG_RECORD_FINER_NOTIFICATION_TYPE,
        Level.FINEST, LOG_RECORD_FINEST_NOTIFICATION_TYPE,};

    private static Map<Level, String> initLevelToNotificationTypeMap() {
        final Map<Level, String> m = new HashMap<>();

        for (int i = 0; i < LEVELS_AND_NOTIF_TYPES.length; i += 2) {
            final Level level = (Level) LEVELS_AND_NOTIF_TYPES[ i];
            final String notifType = (String) LEVELS_AND_NOTIF_TYPES[ i + 1];
            m.put(level, notifType);
        }

        return (Collections.unmodifiableMap(m));
    }

    private String logLevelToNotificationType(final Level level) {
        return mLevelToNotificationTypeMap.get(level);
    }

    @Override
    protected void preRegisterDone()
            throws Exception {
        initNotificationTypeToNotificationBuilderMap(getObjectName());
    }

    private void initNotificationTypeToNotificationBuilderMap(final ObjectName objectName) {
        mNotificationTypeToNotificationBuilderMap.clear();
        for (final String notifType : ALL_LOG_RECORD_NOTIFICATION_TYPES) {
            mNotificationTypeToNotificationBuilderMap.put(
                    notifType,
                    new NotificationBuilder(notifType, objectName));
        }
    }

    private NotificationBuilder notificationTypeToNotificationBuilder(final String notificationType) {
        NotificationBuilder builder =
                mNotificationTypeToNotificationBuilderMap.get(notificationType);

        assert (builder != null);

        return builder;
    }

    private Map<String, Serializable> logRecordToMap(
            final LogRecord record,
            final String recordAsString) {
        final Map<String, Serializable> m = new HashMap<>();

        m.put(LOG_RECORD_AS_STRING_KEY, recordAsString);
        m.put(LOG_RECORD_LEVEL_KEY, record.getLevel());
        m.put(LOG_RECORD_LOGGER_NAME_KEY, record.getLoggerName());
        m.put(LOG_RECORD_MESSAGE_KEY, record.getMessage());
        m.put(LOG_RECORD_MILLIS_KEY, record.getMillis());
        m.put(LOG_RECORD_SEQUENCE_NUMBER_KEY, record.getSequenceNumber());
        m.put(LOG_RECORD_SOURCE_CLASS_NAME_KEY, record.getSourceClassName());
        m.put(LOG_RECORD_SOURCE_METHOD_NAME_KEY, record.getSourceMethodName());
        m.put(LOG_RECORD_THREAD_ID_KEY, record.getThreadID());
        final Throwable thrown = record.getThrown();
        if (thrown != null) {
            final Throwable mapped = new ThrowableMapper(thrown).map();
            m.put(LOG_RECORD_THROWN_KEY, mapped);

            final Throwable rootCause = ExceptionUtil.getRootCause(thrown);
            if (rootCause != thrown) {
                final Throwable mappedRootCause = new ThrowableMapper(rootCause).map();
                m.put(LOG_RECORD_ROOT_CAUSE_KEY, mappedRootCause);
            }
        }
        return m;
    }
    private long mMyThreadID = -1;

    /**
     * Internal use only, called by
     * com.sun.enterprise.server.logging.AMXLoggingHook.
     */
    public void privateLoggingHook(
            final LogRecord logRecord,
            final Formatter formatter) {
        //debug( "LoggingImpl.privateLoggingHook: " + formatter.format( logRecord ) );

        if (logRecord.getThreadID() == mMyThreadID) {
            debug("privateLoggingHook: recusive call!!!");
            throw new RuntimeException("recursive call");
        }
        synchronized (this) {
            mMyThreadID = Thread.currentThread().getId();

            final Level level = logRecord.getLevel();

            try {
                // don't construct a Notification if there are no listeners.
                if (getLogLevelListenerCount(level) != 0) {
                    final String notifType = logLevelToNotificationType(level);

                    final NotificationBuilder builder =
                            notificationTypeToNotificationBuilder(notifType);

                    // Notification.getMessage() will be the formatted log record
                    final String logRecordAsString = formatter.format(logRecord);

                    final Map<String, Serializable> userData =
                            logRecordToMap(logRecord, logRecordAsString);

                    final Notification notif =
                            builder.buildNewWithMap(logRecordAsString, userData);

                    debug("privateLoggingHook: sending: " + notif);
                    sendNotification(notif);
                } else {
                    // debug( "privateLogHook: no listeners for level " + level );
                }
            } finally {
                mMyThreadID = -1;
            }
        }
    }


    public void testEmitLogMessage(final String level, final String message) {
        setMBeanLogLevel(level);
        debug("testEmitLogMessage: logging: message = " + message);
        getLogger().log(Level.parse(level), message);
    }
}

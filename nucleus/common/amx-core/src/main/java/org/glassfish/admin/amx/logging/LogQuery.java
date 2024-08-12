/*
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

package org.glassfish.admin.amx.logging;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.management.Attribute;
import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * Provides access to log messages already present in the log file.
 *
 * @since AS 9.0
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public interface LogQuery {

    /**
     * The lowest supported log level for which queries may be performed.
     */
    String LOWEST_SUPPORTED_QUERY_LEVEL = Level.WARNING.toString();

    /**
     * Query a server log file for records beginning at index <code>startIndex</code>.
     * <p>
     * The <code>name</code> parameter may be {@link LogFileAccess#MOST_RECENT_NAME}
     * to query the current server log file, or may be any specific server log
     * file as returned by {@link LogFileAccess#getLogFileNames}.
     * <p>
     * To query log records starting at the beginning of the file and moving forward,
     * use startIndex={@link #FIRST_RECORD}. To query records beginning at the end of the
     * file and moving backwards, use startIndex={@link #LAST_RECORD} and
     * specify <code>searchForward=false</code>.
     * <p>
     * If <code>searchForward</code> is true,
     * then log records beginning with
     * <code>startRecord</code> (inclusive) and later
     * are considered by the query.<br>
     * If <code>searchForward</code> is false,
     * then log records beginning at
     * <code>startRecord - 1</code> and earlier are considered by the query.
     * <p>
     * Because a log file could be deleted
     * <p>
     * <b>QUESTIONS TO RESOLVE<b>
     * <ul>
     * <li>What are the legal keys and values of 'nameValueMap'</li>
     * </ul>
     * <p>
     *
     * @param name a specific log file name or {@link LogFileAccess#MOST_RECENT_NAME}
     * @param startIndex the location within the LogFile to begin.
     * @param searchForward true to move forward, false to move backward from
     *            <code>startIndex</code>
     * @param maxRecords the maximum number of results to be returned, {@link #ALL_RECORDS} for all
     * @param fromTime the lower bound time, may be null (inclusive)
     * @param toTime the upper bound time, may be null (exclusive)
     * @param logLevel the minimum log level to return, see {@link Level}
     * @param modules one or more modules as defined in {@link LogModuleNames} or
     *            any valid Logger name
     * @param nameValuePairs name-value pairs to match. Names need not be unique.
     * @return LogQueryResult when using AMX client proxy.
     *         Actual type returned from the MBean is List&lt;Serializable[]>
     *         The first Serializable[] is a String[] which contains the field names.
     *         Subsequent Serializable[] each represent a log record with each element representing
     *         a field within that log record.
     * @see LogRecordFields
     * @see LogModuleNames
     *      Over the wire transmission of 'UnprocessedConfigChange' would require the client to have
     *      its class;
     *      as delivered the Object[] contains only standard JDK types.
     *      See the Javadoc for {@link LogQueryResult} for the order of values in the Object[].
     *      Clients with access to the class can use {@link SystemStatus.Helper#toLogQueryResult}
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO) List<Serializable[]>
    queryServerLog(
        @Param(name="logFilename") String  logFilename,
        @Param(name="startIndex") long     startIndex,
        @Param(name="searchForward") boolean searchForward,
        @Param(name="maxRecords") int     maxRecords,
        @Param(name="fromTime") Long    fromTime,
        @Param(name="toTime") Long    toTime,
        @Param(name="logLevel") String   logLevel,
        @Param(name="modules") Set<String>      modules,
        @Param(name="nameValuePairs") List<Attribute>          nameValuePairs,
        @Param(name = "anySearch") String anySearch);

    /**
     * Value for the <code>maximumNumberOfResults</code> parameter to
     * {@link #queryServerLog} which returns all results.
     */
    int ALL_RECORDS = -1;

    /**
     * Value for the <code>startIndex</code> parameter to
     * {@link #queryServerLog}.
     */
    int FIRST_RECORD = 0;

    /**
     * Value for the <code>startIndex</code> parameter to
     * {@link #queryServerLog}.
     */
    int LAST_RECORD = -1;

    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String[] getDiagnosticCauses(
        @Param(name = "messageID") String messageID,
        @Param(name = "moduleName") String moduleName);


    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String[] getDiagnosticChecks(
        @Param(name = "messageID") String messageID,
        @Param(name = "moduleName") String moduleName);


    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    String getDiagnosticURI(
        @Param(name = "messageID") String messageID);

    /** helper class, in particular to convert results from {@link #queryServerLog} */
    public final class Helper {

        private Helper() {
        }

        public static LogQueryResult toLogQueryResult(final List<Serializable[]> items) {
            final LogQueryResult l = new LogQueryResultImpl(items);

            return l;
        }
    }
}






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

import java.util.Map;
import java.util.logging.Level;
import javax.management.MBeanOperationInfo;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;


/**
    Provides summary information about important logging events.
    <big>PRELIMINARY--SUBJECT TO CHANGES/ADDITIONS</big>

    @since AS 9.0
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
public interface LogAnalyzer
{
    /**
        Key into any Map returned from {@link #getErrorInfo}.
        value is of type Long.
    */
    public static final String TIMESTAMP_KEY        = "TimeStamp";

    /**
        Key into any Map returned from {@link #getErrorInfo}.
        value is of type Long.
    */
    public static final String SEVERE_COUNT_KEY        = "SevereCount";

    /**
        Key into any Map returned from {@link #getErrorInfo}.
        value is of type Long.
    */
    public static final String WARNING_COUNT_KEY    = "WarningCount";

    /**
        Key into any Map returned from {@link #getErrorDistribution}.
        value is of type String.
    */
    public static final String MODULE_NAME_KEY    = "ModuleName";


    /**
        Get a summary of the {@link Level#SEVERE} and {@link Level#WARNING} log
        entries for the known history. Each entry in the resulting array is a
        Map with the following keys:
        <ul>
        <li>{@link #TIMESTAMP_KEY} of type Long</li>
        <li>{@link #SEVERE_COUNT_KEY} of type Integer</li>
        <li>{@link #WARNING_COUNT_KEY} of type Integer</li>
        </ul>
        The entries are arranged from oldest to newest with the last entry being
        the most recent.
        <p>
        The timestamp obtained from each Map may be used as the timestamp when
        calling {@link #getErrorDistribution}. For example:<br>
<code>
final Map<String,Number>[]    infos    = logging.getErrorInfo();<br>
for( int i = 0; i < infos.length; ++i ) {<br>
    final Map<String,Object>    info    = infos[ i ];<br>
    final long timestamp    = ((Long)info.get( TIMESTAMP_KEY )).longValue();<br>
    <br>
    Map<String,Number>    counts    = getErrorDistribution( timestamp );<br>
}
</code>

        @return Map<String,Number>
     */
    @ManagedAttribute
    public Map<String,Number>[]    getErrorInfo();


    /**
        Get the number of log entries for a particular timestamp of a particular {@link Level}
        for all modules.  SEVERE and WARNING are the only levels supported.
        <p>
        The resulting Map is keyed by the module ID, which may be any of the values
        found in {@link LogModuleNames} or any valid Logger name.
        The corresponding value
        is the count for that module of the requested level.
        <p>

        @param timestamp a timestamp as obtained using TIME_STAMP_KEY from one of the Maps
        returned by {@link #getErrorInfo}.  Note that it is a 'long' not a 'Long' and is required.
        @param level
        @return Map<String,Integer>
     */
    @ManagedOperation
    public Map<String,Integer>    getErrorDistribution(
        @Param(name="timestamp") long timestamp,
        @Param(name="level") String level);

    /**
        @return all the logger names currently in use
     */
    @ManagedAttribute
    public String[] getLoggerNames();

    /**
        @return all the logger names currently in use under this logger.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String[]   getLoggerNamesUnder( @Param(name="loggerName") String loggerName );

    /**
        Set the number of intervals error statistics should be maintained.

        @param numIntervals number of intervals
     */
    @ManagedAttribute
    public void setKeepErrorStatisticsForIntervals( @Param(name="numIntervals") final int numIntervals );

    /*
        See {@link #setErrorStatisticsIntervals}.
     */
    @ManagedAttribute
    public int  getKeepErrorStatisticsForIntervals();

    /**
        Set the duration of an interval.

        @param minutes The duration of an interval in minutes.
     */
    @ManagedAttribute
    public void setErrorStatisticsIntervalMinutes( @Param(name="minutes") final long minutes);

    /*
        See {@link #setErrorStatisticsIntervalMinutes}.
     */
    @ManagedAttribute
    public long getErrorStatisticsIntervalMinutes();
}







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

package org.glassfish.web.admin.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.annotations.Reset;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.external.statistics.impl.TimeStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.web.admin.LogFacade;

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
@AMXMetadata(type="web-request-mon", group="monitoring")
@ManagedObject
@Description("Web Request Statistics")
public class RequestStatsProvider {

    private static final Logger logger = LogFacade.getLogger();

    private static final String ERROR_COUNT_DESCRIPTION =
        "Cumulative value of the error count, with error count representing the number of cases where the response code was greater than or equal to 400";
    private static final String REQUEST_COUNT_DESCRIPTION =
        "Cumulative number of requests processed so far";
    private static final String MAX_TIME_DESCRIPTION =
        "Longest response time for a request; not a cumulative value, but the largest response time from among the response times";
    private static final String PROCESSING_TIME_DESCRIPTION =
        "Average request processing time";

    private CountStatisticImpl errorCount = new CountStatisticImpl("ErrorCount",
            StatisticImpl.UNIT_COUNT, ERROR_COUNT_DESCRIPTION);
    private CountStatisticImpl requestCount = new CountStatisticImpl("RequestCount",
            StatisticImpl.UNIT_COUNT, REQUEST_COUNT_DESCRIPTION);
    private CountStatisticImpl maxTime = new CountStatisticImpl("MaxTime",
            StatisticImpl.UNIT_MILLISECOND, MAX_TIME_DESCRIPTION);
    private CountStatisticImpl processingTime = new CountStatisticImpl("ProcessingTime",
            StatisticImpl.UNIT_MILLISECOND, PROCESSING_TIME_DESCRIPTION);

    private TimeStatisticImpl requestProcessTime = new TimeStatisticImpl(0L, 0L, 0L, 0L,
            "", "", "", System.currentTimeMillis(), -1L);

    private String virtualServerName = null;
    private String moduleName = null;

    private ThreadLocal<TimeStatData> timeStatDataLocal = new ThreadLocal<TimeStatData> (){
        TimeStatData tsd;

        protected TimeStatData initialValue (){
            tsd = new TimeStatData ();
            return tsd;
        }
        public TimeStatData get (){
            if (tsd == null) {
                tsd = new TimeStatData();
            }
            return tsd;
        }
    };

    public RequestStatsProvider(String appName, String vsName) {
        this.virtualServerName = vsName;
        this.moduleName = appName;
    }

    @ManagedAttribute(id="errorcount")
    @Description(ERROR_COUNT_DESCRIPTION)
    public CountStatistic getErrorCount() {
        return errorCount;
    }

    @ManagedAttribute(id="requestcount")
    @Description(REQUEST_COUNT_DESCRIPTION)
    public CountStatistic getRequestCount() {
        requestCount.setCount(requestProcessTime.getCount());
        return requestCount;
    }

    @ManagedAttribute(id="maxtime")
    @Description(MAX_TIME_DESCRIPTION)
    public CountStatistic getMaxTime() {
        maxTime.setCount(requestProcessTime.getMaxTime());
        return maxTime;
    }

    @ManagedAttribute(id="processingtime")
    @Description(PROCESSING_TIME_DESCRIPTION)
    public CountStatistic getProcessingTime() {
        long count = requestProcessTime.getCount();
        long processTime = 0L;
        if (count != 0) {
            processTime = requestProcessTime.getTotalTime()/count;
        }
        processingTime.setCount(processTime);
        return processingTime;
    }

    @ProbeListener("glassfish:web:http-service:requestStartEvent")
    public void requestStartEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName,
            @ProbeParam("serverName") String serverName,
            @ProbeParam("serverPort") int serverPort,
            @ProbeParam("contextPath") String contextPath,
            @ProbeParam("servletPath") String servletPath) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(
                "[TM]requestStartEvent Unprocessed received - virtual-server = " +
                serverName + ":" + serverPort +
                ": application = " + contextPath +
                " : servlet = " + servletPath +
                " : Expecting (vsName, appName) = (" +
                virtualServerName + ", " + moduleName + ")");
        }
        if ((virtualServerName != null) && (moduleName != null)) {
            //String vs = WebTelemetryBootstrap.getVirtualServerName(
            //    hostName, String.valueOf(request.getServerPort()));
            if ((appName != null && hostName != null) &&
                    hostName.equals(virtualServerName) &&
                    appName.equals(moduleName)){
                //increment counts
                timeStatDataLocal.get().setEntryTime(System.currentTimeMillis());
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(
                        "[TM]requestStartEvent resolved - virtual-server = " +
                        serverName + ": application = " +
                        contextPath + " :appName = " + appName +
                        " : servlet = " + servletPath + " : port = " +
                        serverPort);
                }
            }
        } else {
            timeStatDataLocal.get().setEntryTime(System.currentTimeMillis());
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(
                    "[TM]requestStartEvent resolved - virtual-server = " +
                    serverName + ": application = " + contextPath +
                    " : servlet = " + servletPath);
            }
        }
    }

    @ProbeListener("glassfish:web:http-service:requestEndEvent")
    public void requestEndEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName,
            @ProbeParam("serverName") String serverName,
            @ProbeParam("serverPort") int serverPort,
            @ProbeParam("contextPath") String contextPath,
            @ProbeParam("servletPath") String servletPath,
            @ProbeParam("statusCode") int statusCode,
            @ProbeParam("method") String method,
            @ProbeParam("uri") String uri) {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(
                "[TM]requestEndEvent Unprocessed received - virtual-server = " +
                serverName + ": application = " +
                contextPath + " : servlet = " +
                servletPath + " :Response code = " +
                statusCode + " : Expecting (vsName, appName) = (" +
                virtualServerName + ", " + moduleName + ")");
        }
        if ((virtualServerName != null) && (moduleName != null)) {
            //String vs = WebTelemetryBootstrap.getVirtualServerName(
            //    hostName, String.valueOf(request.getServerPort()));
            if ((appName != null && hostName != null) &&
                    hostName.equals(virtualServerName) &&
                    appName.equals(moduleName)){
                //increment counts
                TimeStatData tsd = timeStatDataLocal.get();
                tsd.setExitTime(System.currentTimeMillis());
                requestProcessTime.incrementCount(tsd.getTotalTime());

                if (statusCode >= 400) {
                    errorCount.increment();
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(
                        "[TM]requestEndEvent resolved - virtual-server = " +
                        serverName + ": application = " + contextPath +
                        " :appName = " + appName +
                        " : servlet = " + servletPath +
                        " : port = " + serverPort +
                        " :Response code = " + statusCode);
                }
            }
        } else {
            TimeStatData tsd = timeStatDataLocal.get();
            tsd.setExitTime(System.currentTimeMillis());
            requestProcessTime.incrementCount(tsd.getTotalTime());

            if (statusCode >= 400) {
                errorCount.increment();
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(
                    "[TM]requestEndEvent resolved - virtual-server = " +
                    serverName + ": application = " +
                    contextPath + " : servlet = " +
                    servletPath + " : port = " +
                    serverPort  + " :Response code = " +
                    statusCode);
            }
        }
    }


    public long getProcessTime() {
        return requestProcessTime.getTotalTime()/requestProcessTime.getCount();
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVSName() {
        return virtualServerName;
    }

    @Reset
    public void reset() {
        this.requestProcessTime.reset();
        this.errorCount.reset();
        this.maxTime.reset();
        this.processingTime.reset();
        this.requestCount.reset();
    }

}

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
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.RangeStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
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
@AMXMetadata(type="servlet-mon", group="monitoring")
@ManagedObject
@Description("Web Container Servlet Statistics")
public class ServletStatsProvider {

    private static final Logger logger = LogFacade.getLogger();

    private static final String ACTIVE_SERVLETS_LOADED_DESCRIPTION =
        "Number of Servlets loaded";

    private static final String TOTAL_SERVLETS_LOADED_DESCRIPTION =
        "Total number of Servlets ever loaded";

    private static final String SERVLET_PROCESSING_TIMES_DESCRIPTION =
        "Cumulative Servlet processing times";

    private String moduleName;
    private String vsName;
    private RangeStatisticImpl activeServletsLoadedCount;
    private CountStatisticImpl totalServletsLoadedCount;
    private CountStatisticImpl servletProcessingTimes;

    public ServletStatsProvider(String moduleName, String vsName) {
        this.moduleName = moduleName;
        this.vsName = vsName;
        long curTime = System.currentTimeMillis();
        activeServletsLoadedCount = new RangeStatisticImpl(
            0L, 0L, 0L, "ActiveServletsLoaded", StatisticImpl.UNIT_COUNT,
            ACTIVE_SERVLETS_LOADED_DESCRIPTION, curTime, curTime);
        totalServletsLoadedCount = new CountStatisticImpl(
            "TotalServletsLoaded", StatisticImpl.UNIT_COUNT,
            TOTAL_SERVLETS_LOADED_DESCRIPTION);
        servletProcessingTimes = new CountStatisticImpl(
            "ServletProcessingTimes", StatisticImpl.UNIT_MILLISECOND,
            SERVLET_PROCESSING_TIMES_DESCRIPTION);
    }

    @ManagedAttribute(id="activeservletsloadedcount")
    @Description(ACTIVE_SERVLETS_LOADED_DESCRIPTION)
    public RangeStatistic getActiveServletsLoaded() {
        return activeServletsLoadedCount;
    }

    @ManagedAttribute(id="totalservletsloadedcount")
    @Description(TOTAL_SERVLETS_LOADED_DESCRIPTION)
    public CountStatistic getTotalServletsLoaded() {
        return totalServletsLoadedCount;
    }

    @ManagedAttribute(id="servletprocessingtimes")
    @Description(SERVLET_PROCESSING_TIMES_DESCRIPTION)
    public CountStatistic getServletProcessingTimes() {
        return servletProcessingTimes;
    }

    @ProbeListener("glassfish:web:servlet:servletInitializedEvent")
    public void servletInitializedEvent(
                    @ProbeParam("servletName") String servletName,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Servlet Loaded event received - " +
                          "servletName = " + servletName +
                          ": appName = " + appName + ": hostName = " +
                          hostName);
        }
        if (isValidEvent(appName, hostName)) {
            synchronized (activeServletsLoadedCount) {
                activeServletsLoadedCount.setCurrent(
                    activeServletsLoadedCount.getCurrent() + 1);
            }
            totalServletsLoadedCount.increment();
        }
    }

    @ProbeListener("glassfish:web:servlet:servletDestroyedEvent")
    public void servletDestroyedEvent(
                    @ProbeParam("servletName") String servletName,
                    @ProbeParam("appName") String appName,
                    @ProbeParam("hostName") String hostName) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Servlet Destroyed event received - " +
                          "servletName = " + servletName +
                          ": appName = " + appName + ": hostName = " +
                          hostName);
        }
        if (isValidEvent(appName, hostName)) {
            synchronized (activeServletsLoadedCount) {
                activeServletsLoadedCount.setCurrent(
                    activeServletsLoadedCount.getCurrent() - 1);
            }
        }
    }

    private boolean isValidEvent(String mName, String hostName) {
        //Temp fix, get the appname from the context root
        if ((moduleName == null) || (vsName == null)) {
            return true;
        }
        if ((moduleName.equals(mName)) && (vsName.equals(hostName))) {
            return true;
        }

        return false;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVSName() {
        return vsName;
    }

    void addServletProcessingTime(long servletProcessingTime) {
        servletProcessingTimes.increment(servletProcessingTime);
    }
}

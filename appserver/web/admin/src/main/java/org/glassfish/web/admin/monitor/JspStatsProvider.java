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

/**
 * Provides the monitoring data at the Web container level
 *
 * @author Prashanth Abbagani
 */
@AMXMetadata(type="jsp-mon", group="monitoring")
@ManagedObject
@Description("Web Container JSP Statistics")
public class JspStatsProvider {

    private static final String JSP_COUNT_DESCRIPTION =
        "Number of active JSP pages";

    private static final String TOTAL_JSP_COUNT_DESCRIPTION =
        "Total number of JSP pages ever loaded";

    private static final String JSP_RELOADED_COUNT_DESCRIPTION =
        "Total number of JSP pages that were reloaded";

    private static final String JSP_ERROR_COUNT_DESCRIPTION =
        "Total number of errors triggered by JSP page invocations";

    private String moduleName;
    private String vsName;
    private RangeStatisticImpl jspCount;
    private CountStatisticImpl totalJspCount;
    private CountStatisticImpl jspReloadedCount;
    private CountStatisticImpl jspErrorCount;

    public JspStatsProvider(String moduleName, String vsName) {
        this.moduleName = moduleName;
        this.vsName = vsName;
        long curTime = System.currentTimeMillis();
        jspCount = new RangeStatisticImpl(
            0L, 0L, 0L, "JspCount", StatisticImpl.UNIT_COUNT,
            JSP_COUNT_DESCRIPTION, curTime, curTime);
        totalJspCount = new CountStatisticImpl(
            "TotalJspCount", StatisticImpl.UNIT_COUNT,
            TOTAL_JSP_COUNT_DESCRIPTION);
        jspReloadedCount = new CountStatisticImpl(
            "JspReloadedCount", StatisticImpl.UNIT_COUNT,
            JSP_RELOADED_COUNT_DESCRIPTION);
        jspErrorCount = new CountStatisticImpl(
            "JspErrorCount", StatisticImpl.UNIT_COUNT,
            JSP_ERROR_COUNT_DESCRIPTION);
    }

    @ManagedAttribute(id="jspcount")
    @Description(JSP_COUNT_DESCRIPTION)
    public RangeStatistic getJspCount() {
        return jspCount;
    }

    @ManagedAttribute(id="totaljspcount")
    @Description(TOTAL_JSP_COUNT_DESCRIPTION)
    public CountStatistic getTotalJspCount() {
        return totalJspCount;
    }

    @ManagedAttribute(id="jspreloadedcount")
    @Description(JSP_RELOADED_COUNT_DESCRIPTION)
    public CountStatistic getJspReloadedCount() {
        return jspReloadedCount;
    }

    @ManagedAttribute(id="jsperrorcount")
    @Description(JSP_ERROR_COUNT_DESCRIPTION)
    public CountStatistic getJspErrorCount() {
        return jspErrorCount;
    }

    @ProbeListener("glassfish:web:jsp:jspLoadedEvent")
    public void jspLoadedEvent(
            @ProbeParam("jspUri") String jspUri,
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName) {
        if (isValidEvent(appName, hostName)) {
            synchronized (jspCount) {
                jspCount.setCurrent(
                    jspCount.getCurrent() + 1);
            }
            totalJspCount.increment();
        }
    }

    @ProbeListener("glassfish:web:jsp:jspReloadedEvent")
    public void jspReloadedEvent(
            @ProbeParam("jspUri") String jspUri,
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName) {
        if (isValidEvent(appName, hostName)) {
            jspReloadedCount.increment();
        }
    }

    @ProbeListener("glassfish:web:jsp:jspDestroyedEvent")
    public void jspDestroyedEvent(
            @ProbeParam("jspUri") String jspUri,
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName) {
        if (isValidEvent(appName, hostName)) {
            synchronized (jspCount) {
                jspCount.setCurrent(
                    jspCount.getCurrent() - 1);
            }
        }
    }

    @ProbeListener("glassfish:web:jsp:jspErrorEvent")
    public void jspErrorEvent(
            @ProbeParam("jspUri") String jspUri,
            @ProbeParam("appName") String appName,
            @ProbeParam("hostName") String hostName) {
        if (isValidEvent(appName, hostName)) {
            jspErrorCount.increment();
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVSName() {
        return vsName;
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
}

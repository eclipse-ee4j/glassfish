/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.RangeStatisticImpl;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Provider statistics for deployment lifecycle
 */
@AMXMetadata(type="deployment-mon", group="monitoring")
@ManagedObject
@Description("Deployment Module Statistics")
public class DeploymentLifecycleStatsProvider {

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    private static final String ACTIVE_APPLICATIONS_DEPLOYED_DESCRIPTION =
        "Number of applications deployed";

    private static final String TOTAL_APPLICATIONS_DEPLOYED_DESCRIPTION =
        "Total number of applications ever deployed";

    private static final String APPLICATIONS_INFORMATION_DESCRIPTION =
        "Information about deployed applications";

    private static final String MODULE_TYPE = "moduleType";
    private static final String LOADING_TIME = "loadingTime";

    private static final int COLUMN_LENGTH = 25;
    private static final String LINE_BREAK = "%%%EOL%%%";

    private RangeStatisticImpl activeApplicationsDeployedCount;
    private CountStatisticImpl totalApplicationsDeployedCount;

    private StringStatisticImpl appsInfoStat = new StringStatisticImpl(
        "ApplicationsInformation", "List",
        APPLICATIONS_INFORMATION_DESCRIPTION);

    private Map<String, Map<String, String>> appsInfoMap =
        new HashMap<String, Map<String, String>>();

    public DeploymentLifecycleStatsProvider() {
        long curTime = System.currentTimeMillis();
        activeApplicationsDeployedCount = new RangeStatisticImpl(
            0L, 0L, 0L, "ActiveApplicationsDeployed", StatisticImpl.UNIT_COUNT,
            ACTIVE_APPLICATIONS_DEPLOYED_DESCRIPTION, curTime, curTime);
        totalApplicationsDeployedCount = new CountStatisticImpl(
            "TotalApplicationsDeployed", StatisticImpl.UNIT_COUNT,
            TOTAL_APPLICATIONS_DEPLOYED_DESCRIPTION);
    }

    @ManagedAttribute(id="activeapplicationsdeployedcount")
    @Description(ACTIVE_APPLICATIONS_DEPLOYED_DESCRIPTION)
    public RangeStatistic getActiveApplicationsDeployed() {
        return activeApplicationsDeployedCount;
    }

    @ManagedAttribute(id="totalapplicationsdeployedcount")
    @Description(TOTAL_APPLICATIONS_DEPLOYED_DESCRIPTION)
    public CountStatistic getTotalApplicationsDeployed() {
        return totalApplicationsDeployedCount;
    }

    @ManagedAttribute(id="applicationsinfo")
    @Description(APPLICATIONS_INFORMATION_DESCRIPTION)
    public StringStatistic getApplicationsInfo() {
        StringBuffer strBuf = new StringBuffer(1024);
        if (!appsInfoMap.isEmpty()) {
            // Set the headings for the tabular output
            int appNameLength = COLUMN_LENGTH;
            int moduleTypeLength = COLUMN_LENGTH;
            for (String appName : appsInfoMap.keySet()) {
                if (appName.length() > appNameLength) {
                    appNameLength = appName.length() + 1;
                }
                String moduleType = appsInfoMap.get(appName).get(MODULE_TYPE);
                if (moduleType.length() > moduleTypeLength) {
                    moduleTypeLength = moduleType.length() + 1;
                }
            }

            strBuf.append(LINE_BREAK).append(LINE_BREAK);
            appendColumn(strBuf, "Application_Name", appNameLength);
            appendColumn(strBuf, "Module_Type", moduleTypeLength);
            appendColumn(strBuf, "Loading_Time(ms)", COLUMN_LENGTH);
            strBuf.append(LINE_BREAK);

            for (String appName : appsInfoMap.keySet()) {
                appendColumn(strBuf, appName, appNameLength);
                Map<String, String> appInfoMap = appsInfoMap.get(appName);
                String moduleType = appInfoMap.get(MODULE_TYPE);
                String loadingTime = appInfoMap.get(LOADING_TIME);
                appendColumn(strBuf, moduleType, COLUMN_LENGTH);
                appendColumn(strBuf, loadingTime, COLUMN_LENGTH);
                strBuf.append(LINE_BREAK);
            }
        }
        appsInfoStat.setCurrent(strBuf.toString());
        return appsInfoStat;
    }

    @ProbeListener("glassfish:deployment:lifecycle:applicationDeployedEvent")
    public void applicationDeployedEvent(
                    @ProbeParam("appName") String appName,
                    @ProbeParam("appType") String appType,
                    @ProbeParam("loadTime") String loadTime) {
        if (deplLogger.isLoggable(Level.FINEST)) {
            deplLogger.finest("Application deployed event received - " +
                          "appName = " + appName +
                          ": appType = " + appType +
                          ": loadTime = " + loadTime);
        }
        Map<String, String> appInfoMap = new HashMap<String, String>();
        appInfoMap.put(MODULE_TYPE, appType);
        appInfoMap.put(LOADING_TIME, loadTime);
        appsInfoMap.put(appName, appInfoMap);
        synchronized (activeApplicationsDeployedCount) {
            activeApplicationsDeployedCount.setCurrent(
                activeApplicationsDeployedCount.getCurrent() + 1);
        }
        totalApplicationsDeployedCount.increment();
    }

    @ProbeListener("glassfish:deployment:lifecycle:applicationUndeployedEvent")
    public void applicationUndeployedEvent(
                    @ProbeParam("appName") String appName,
                    @ProbeParam("appType") String appType) {
        if (deplLogger.isLoggable(Level.FINEST)) {
            deplLogger.finest("Application undeployed event received - " +
                          "appName = " + appName +
                          ": appType = " + appType);
        }
        appsInfoMap.remove(appName);
        synchronized (activeApplicationsDeployedCount) {
            activeApplicationsDeployedCount.setCurrent(
                activeApplicationsDeployedCount.getCurrent() - 1);
        }
    }

    private void appendColumn(StringBuffer buf, String text, int length) {
        buf.append(text);
        for (int i=text.length(); i<length; i++){
            buf.append(" ");
        }
    }
}

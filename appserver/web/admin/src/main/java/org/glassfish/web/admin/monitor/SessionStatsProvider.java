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
@AMXMetadata(type="session-mon", group="monitoring")
@ManagedObject
@Description( "Web Container Session Statistics" )
public class SessionStatsProvider{

    private static final Logger logger = LogFacade.getLogger();

    private static final String ACTIVE_SESSIONS_DESCRIPTION =
        "Number of active sessions";
    private static final String TOTAL_SESSIONS_DESCRIPTION =
        "Total number of sessions ever created";
    private static final String EXPIRED_SESSIONS_DESCRIPTION =
        "Total number of sessions ever expired";
    private static final String REJECTED_SESSIONS_DESCRIPTION =
        "Total number of sessions ever rejected";
    private static final String PERSISTED_SESSIONS_DESCRIPTION =
        "Total number of sessions ever persisted";
    private static final String PASSIVATED_SESSIONS_DESCRIPTION =
        "Total number of sessions ever passivated";
    private static final String ACTIVATED_SESSIONS_DESCRIPTION =
        "Total number of sessions ever activated";

    private String moduleName;
    private String vsName;

    private RangeStatisticImpl activeSessionsCount;
    private CountStatisticImpl sessionsTotal;
    private CountStatisticImpl expiredSessionsTotal;
    private CountStatisticImpl rejectedSessionsTotal;
    private CountStatisticImpl persistedSessionsTotal;
    private CountStatisticImpl passivatedSessionsTotal;
    private CountStatisticImpl activatedSessionsTotal;

    public SessionStatsProvider(String moduleName, String vsName) {
        this.moduleName = moduleName;
        this.vsName = vsName;
        long curTime = System.currentTimeMillis();
        activeSessionsCount = new RangeStatisticImpl(
            0L, 0L, 0L, "ActiveSessions", StatisticImpl.UNIT_COUNT,
            ACTIVE_SESSIONS_DESCRIPTION, curTime, curTime);
        sessionsTotal = new CountStatisticImpl("SessionsTotal",
            StatisticImpl.UNIT_COUNT, TOTAL_SESSIONS_DESCRIPTION);
        expiredSessionsTotal = new CountStatisticImpl(
            "ExpiredSessionsTotal", StatisticImpl.UNIT_COUNT,
            EXPIRED_SESSIONS_DESCRIPTION);
        rejectedSessionsTotal = new CountStatisticImpl(
            "RejectedSessionsTotal", StatisticImpl.UNIT_COUNT,
            REJECTED_SESSIONS_DESCRIPTION);
        persistedSessionsTotal = new CountStatisticImpl(
            "PersistedSessionsTotal", StatisticImpl.UNIT_COUNT,
            PERSISTED_SESSIONS_DESCRIPTION);
        passivatedSessionsTotal = new CountStatisticImpl(
            "PassivatedSessionsTotal", StatisticImpl.UNIT_COUNT,
            PASSIVATED_SESSIONS_DESCRIPTION);
        activatedSessionsTotal = new CountStatisticImpl(
            "ActivatedSessionsTotal", StatisticImpl.UNIT_COUNT,
            ACTIVATED_SESSIONS_DESCRIPTION);
    }

    @ManagedAttribute(id="activesessionscurrent")
    @Description(ACTIVE_SESSIONS_DESCRIPTION)
    public RangeStatistic getActiveSessions() {
        return activeSessionsCount;
    }

    @ManagedAttribute(id="sessionstotal")
    @Description(TOTAL_SESSIONS_DESCRIPTION)
    public CountStatistic getSessionsTotal() {
        return sessionsTotal;
    }

    @ManagedAttribute(id="rejectedsessionstotal")
    @Description(REJECTED_SESSIONS_DESCRIPTION)
    public CountStatistic getRejectedSessionsTotal() {
        return rejectedSessionsTotal;
    }

    @ManagedAttribute(id="expiredsessionstotal")
    @Description(EXPIRED_SESSIONS_DESCRIPTION)
    public CountStatistic getExpiredSessionsTotal() {
        return expiredSessionsTotal;
    }

    @ManagedAttribute(id="persistedsessionstotal")
    @Description(PERSISTED_SESSIONS_DESCRIPTION)
    public CountStatistic getPersistedSessionsTotal() {
        return persistedSessionsTotal;
    }

    @ManagedAttribute(id="passivatedsessionstotal")
    @Description(PASSIVATED_SESSIONS_DESCRIPTION)
    public CountStatistic getPassivatedSessionsTotal() {
        return passivatedSessionsTotal;
    }

    @ManagedAttribute(id="activatedsessionstotal")
    @Description(ACTIVATED_SESSIONS_DESCRIPTION)
    public CountStatistic getActivatedSessionsTotal() {
        return activatedSessionsTotal;
    }

    @ProbeListener("glassfish:web:session:sessionCreatedEvent")
    public void sessionCreatedEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionCreatedEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            incrementActiveSessions();
            sessionsTotal.increment();
        }
    }

    @ProbeListener("glassfish:web:session:sessionDestroyedEvent")
    public void sessionDestroyedEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionDestroyedEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            decrementActiveSessions();
        }
    }

    @ProbeListener("glassfish:web:session:sessionRejectedEvent")
    public void sessionRejectedEvent(
        @ProbeParam("maxThresholdSize") int maxSessions,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionRejectedEvent received - max sessions = " +
                          maxSessions + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            rejectedSessionsTotal.increment();
        }
    }

    @ProbeListener("glassfish:web:session:sessionExpiredEvent")
    public void sessionExpiredEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionExpiredEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            expiredSessionsTotal.increment();
        }
    }

    @ProbeListener("glassfish:web:session:sessionPersistedStartEvent")
    public void sessionPersistedStartEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionPersistedStartEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
    }

    @ProbeListener("glassfish:web:session:sessionPersistedEndEvent")
    public void sessionPersistedEndEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionPersistedEndEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            persistedSessionsTotal.increment();
        }
    }

    @ProbeListener("glassfish:web:session:sessionActivatedStartEvent")
    public void sessionActivatedStartEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionActivatedStartEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
    }

    @ProbeListener("glassfish:web:session:sessionActivatedEndEvent")
    public void sessionActivatedEndEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionActivatedEndEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            incrementActiveSessions();
            activatedSessionsTotal.increment();
        }
    }

    @ProbeListener("glassfish:web:session:sessionPassivatedStartEvent")
    public void sessionPassivatedStartEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionPassivatedStartEvent  received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
    }

    @ProbeListener("glassfish:web:session:sessionPassivatedEndEvent")
    public void sessionPassivatedEndEvent(
        @ProbeParam("sessionId") String sessionId,
        @ProbeParam("appName") String appName,
        @ProbeParam("hostName") String hostName){

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("[TM]sessionPassivatedEndEvent received - session = " +
                          sessionId + ": appname = " + appName +
                          ": hostName = " + hostName);
        }
        if (isValidEvent(appName, hostName)) {
            decrementActiveSessions();
            passivatedSessionsTotal.increment();
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVSName() {
        return vsName;
    }

    private void incrementActiveSessions() {
        synchronized (activeSessionsCount) {
            activeSessionsCount.setCurrent(
                activeSessionsCount.getCurrent() + 1);
        }
    }

    private void decrementActiveSessions() {
        synchronized (activeSessionsCount) {
            activeSessionsCount.setCurrent(
                activeSessionsCount.getCurrent() - 1);
        }
    }

    private boolean isValidEvent(String mName, String hostName) {
        //Temp fix, get the appname from the context root
        if ((moduleName == null) || (vsName == null)) {
            return true;
        }
        if (moduleName.equals(mName) && vsName.equals(hostName))
            return true;
        return false;
    }
}

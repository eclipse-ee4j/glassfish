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

package com.sun.enterprise.connectors.work.monitor;

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
 * Provides the monitoring data for Connector Work Manager
 *
 * @author Jagadish Ramu
 */
@AMXMetadata(type="connector-service-mon", group="monitoring")
@ManagedObject
@Description("Connector Container Work Management Statistics")
public class WorkManagementStatsProvider {

    private String moduleName; //ra-name
    private static final String DOTTED_NAME = "glassfish:jca:work-management:";

    public WorkManagementStatsProvider(String moduleName) {
        this.moduleName = moduleName;
    }

    long time = System.currentTimeMillis();

    private final CountStatisticImpl submittedWorkCount =
            new CountStatisticImpl("SubmittedWorkCount", StatisticImpl.UNIT_COUNT,
                    "Number of work objects submitted by a connector module for execution");
    private final CountStatisticImpl rejectedWorkCount =
            new CountStatisticImpl("RejectedWorkCount", StatisticImpl.UNIT_COUNT,
                    "Number of work objects rejected by the application server");
    private final CountStatisticImpl completedWorkCount =
            new CountStatisticImpl("CompletedWorkCount", StatisticImpl.UNIT_COUNT,
                    "Number of work objects completed execution");

    private final RangeStatisticImpl activeWorkCount =
            new RangeStatisticImpl(0, 0, 0, "ActiveWorkCount", StatisticImpl.UNIT_COUNT,
                    "Number of active work objects", time, time);

    private final RangeStatisticImpl waitQueueLength =
            new RangeStatisticImpl(0, 0, 0, "WaitQueueLength", StatisticImpl.UNIT_COUNT,
                    "Number of work objects waiting in the queue for execution", time, time);

    private final RangeStatisticImpl workRequestWaitTime =
            new RangeStatisticImpl(0, 0, 0, "WorkRequestWaitTime", StatisticImpl.UNIT_COUNT,
                    "Wait time of a work object before it gets executed", time, time);


    @ManagedAttribute(id = "submittedworkcount")
    @Description("Number of work objects submitted by a connector module for execution")
    public CountStatistic getSubmittedWorkCount() {
        return submittedWorkCount;
    }

    @ManagedAttribute(id = "rejectedworkcount")
    @Description("Number of work objects rejected by the application server")
    public CountStatistic getRejectedWorkCount() {
        return rejectedWorkCount;
    }

    @ManagedAttribute(id = "completedworkcount")
    @Description("Number of work objects completed execution")
    public CountStatistic getCompletedWorkCount() {
        return completedWorkCount;
    }

    @ManagedAttribute(id = "activeworkcount")
    @Description("Number of active work objects")
    public RangeStatistic getActiveWorkCount() {
        return activeWorkCount;
    }

    @ManagedAttribute(id = "waitqueuelength")
    @Description("Number of work objects waiting in the queue for execution")
    public RangeStatistic getWaitQueueLength() {
        return waitQueueLength;
    }

    @ManagedAttribute(id = "workrequestwaittime")
    @Description("Wait time of a work object before it gets executed")
    public RangeStatistic getWorkRequestWaitTime() {
        return workRequestWaitTime;
    }

    private boolean isValidEvent(String raName) {
        return (raName != null && moduleName.equals(raName));
    }


    @ProbeListener(DOTTED_NAME + "workSubmitted")
    public void workSubmitted(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            submittedWorkCount.increment();
        }
    }

    @ProbeListener(DOTTED_NAME + "workQueued")
    public void workQueued(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            synchronized (waitQueueLength) {
                waitQueueLength.setCurrent(waitQueueLength.getCurrent() + 1);
            }
        }
    }


    @ProbeListener(DOTTED_NAME + "workWaitedFor")
    public void workWaitedFor(
            @ProbeParam("raName") String raName,
            @ProbeParam("elapsedTime") long elapsedTime
    ) {
        if (isValidEvent(raName)) {
            workRequestWaitTime.setCurrent(elapsedTime);
        }
    }

    @ProbeListener(DOTTED_NAME + "workDequeued")
    public void workDequeued(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            synchronized (waitQueueLength) {
                waitQueueLength.setCurrent(waitQueueLength.getCurrent() - 1);
            }
        }
    }

    @ProbeListener(DOTTED_NAME + "workProcessingStarted")
    public void workProcessingStarted(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            synchronized (activeWorkCount) {
                activeWorkCount.setCurrent(activeWorkCount.getCurrent() + 1);
            }
        }
    }

    @ProbeListener(DOTTED_NAME + "workProcessingCompleted")
    public void workProcessingCompleted(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            synchronized (activeWorkCount) {
                activeWorkCount.setCurrent(activeWorkCount.getCurrent() - 1);
            }
        }
    }

    @ProbeListener(DOTTED_NAME + "workProcessed")
    public void workProcessed(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            completedWorkCount.increment();
        }
    }


    @ProbeListener(DOTTED_NAME + "workTimedOut")
    public void workTimedOut(
            @ProbeParam("raName") String raName
    ) {
        if (isValidEvent(raName)) {
            rejectedWorkCount.increment();
        }
    }
}

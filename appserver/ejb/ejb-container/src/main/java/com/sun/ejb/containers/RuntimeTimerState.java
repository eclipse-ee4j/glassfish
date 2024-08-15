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

package com.sun.ejb.containers;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbDescriptor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RuntimeTimerState holds all runtime state of an EJB
 * timer, including what fine-grained state it's in,
 * stats about the number of expirations and failed
 * deliveries that have occurred, and any existing JDK
 * timer task that is currently scheduled for this timer.
 * It also caches read-only state of a timer to improve
 * performance.
 *
 * @author Kenneth Saks
 */
public class RuntimeTimerState {

    private static final Logger logger = EjbContainerUtilImpl.getLogger();

    //
    // Fine-grained timer states
    //

    // Timer has been created but not committed.
    private static final int CREATED     = 0;

    // There is a scheduled JDK timer task for this timer.
    private static final int SCHEDULED   = 1;

    // The JDK timer task has expired and the timer's ejbTimeout method
    // is being called.
    private static final int BEING_DELIVERED   = 2;

    // The timer has been cancelled but the cancellation can still be
    // rolled back.
    private static final int CANCELLED   = 3;

    private int state_;

    //
    // Immutable timer state.
    //
    private TimerPrimaryKey timerId_;

    private Date initialExpiration_;
    private long intervalDuration_;
    private long containerId_;
    private Object timedObjectPrimaryKey_;
    private boolean persistent_ = true;
    private Serializable info_;
    private EJBTimerSchedule schedule_;

    private boolean expired_ = false;

    //
    private BaseContainer container_;

    // Handle to scheduled timer task. This is only set when timer is SCHEDULED
    private EJBTimerTask currentTask_;

    //
    // stats
    //

    private int numExpirations_;
    private int numFailedDeliveries_;

    public RuntimeTimerState(TimerPrimaryKey timerId,
                      Date initialExpiration, long intervalDuration,
                      BaseContainer container,
                      Object timedObjectPkey,
                      EJBTimerSchedule schedule,
                      Serializable info,
                      boolean persistent) {
        this(timerId, initialExpiration, intervalDuration, container.getContainerId(),
                container, timedObjectPkey, schedule, info, persistent);
    }

    RuntimeTimerState(TimerPrimaryKey timerId,
                      Date initialExpiration, long intervalDuration,
                      long containerId,
                      BaseContainer container,
                      Object timedObjectPkey,
                      EJBTimerSchedule schedule,
                      Serializable info,
                      boolean persistent) {

        state_       = CREATED;
        currentTask_ = null;

        timerId_           = timerId;
        initialExpiration_ = initialExpiration;
        intervalDuration_  = intervalDuration;
        timedObjectPrimaryKey_ = timedObjectPkey;
        persistent_        = persistent;
        info_              = info;
        container_         = container;
        schedule_          = schedule;

        containerId_       = containerId;

        if( logger.isLoggable(Level.FINE) ) {
            logger.log(Level.FINE, "RuntimeTimerState " + timerId_ +
                       " created");
        }

        if (schedule != null) {
            Calendar next = schedule.getNextTimeout();
            if( !schedule.isValid(next) ) {
                logger.log(Level.INFO, "Schedule: " +
                                      schedule.getScheduleAsString() +
                                      " already expired");
                // schedule-based timer will never expire.
                // we'll create it now, and remove soon or on server restart
                expired();
            }
        }
    }

    public TimerPrimaryKey getTimerId() {
        return timerId_;
    }

    Date getInitialExpiration() {
        return initialExpiration_;
    }

    BaseContainer getContainer() {
        return container_;
    }

    long getContainerId() {
        return containerId_;
    }

    Object getTimedObjectPrimaryKey() {
        return timedObjectPrimaryKey_;
    }

    long getIntervalDuration() {
        return intervalDuration_;
    }

    EJBTimerSchedule getTimerSchedule() {
        return schedule_;
    }


    //
    // Operations for performing state transitions.
    //

    void scheduled(EJBTimerTask timerTask) {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, SCHEDULED);
        }
        currentTask_ = timerTask;
        state_ = SCHEDULED;
        numFailedDeliveries_ = 0;
    }

    void rescheduled(EJBTimerTask timerTask) {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, SCHEDULED);
        }
        currentTask_ = timerTask;
        state_ = SCHEDULED;
        numFailedDeliveries_++;
    }

    /**
     * Transition from CANCELLED to DELIVERED when ejbTimeout calls
     * cancel and then rolls back.  Don't reset numFailedDeliveries.
     */
    void restoredToDelivered() {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, BEING_DELIVERED);
        }

        currentTask_ = null;
        state_ = BEING_DELIVERED;
    }

    void delivered() {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, BEING_DELIVERED);
        }

        currentTask_ = null;

        if( numFailedDeliveries_ == 0 ) {
            numExpirations_++;
        }

        state_ = BEING_DELIVERED;
    }

    void cancelled() {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, CANCELLED);
        }

        currentTask_ = null;
        state_ = CANCELLED;
    }

    String stateToString() {
        return stateToString(state_);
    }

    private String stateToString(int state) {
        switch(state) {
        case CREATED :
            return "CREATED";
        case SCHEDULED :
            return "SCHEDULED";
        case BEING_DELIVERED :
            return "BEING_DELIVERED";
        case CANCELLED :
            return "CANCELLED";
        }
        return state + " NOT FOUND";
    }

    private void printStateTransition(int fromState, int toState) {
        logger.log(Level.FINER, timerId_ + ": " + stateToString(fromState) +
                   " to " + stateToString(toState));
    }

    int getNumExpirations() {
        return numExpirations_;
    }

    /**
     * Number of failed deliveries since timer last transitioned to
     * the SCHEDULED state.
     */
    public int getNumFailedDeliveries() {
        return numFailedDeliveries_;
    }

    EJBTimerTask getCurrentTimerTask() {
        return currentTask_;
    }

    String getTimedObjectEjbName() {
        return container_.getEjbDescriptor().getName();
    }

    String getTimedObjectApplicationName() {
        EjbDescriptor ejbDesc = container_.getEjbDescriptor();
        Application app = ejbDesc.getApplication();
        return (app != null) ? app.getRegistrationName() : "";
    }

    boolean timedObjectIsEntity() {
        return (timedObjectPrimaryKey_ != null);
    }

    //
    // State-testing accessors.
    //

    boolean isActive() {
        return (state_ != CANCELLED);
    }

    boolean isCancelled() {
        return (state_ == CANCELLED);
    }

    boolean isCreated() {
        return (state_ == CREATED);
    }

    boolean isBeingDelivered() {
        return (state_ == BEING_DELIVERED);
    }

    boolean isScheduled() {
        return (state_ == SCHEDULED);
    }

    boolean isRescheduled() {
        return (isScheduled() && (numFailedDeliveries_ > 0));
    }

    Date getNextTimeout() {
        if( !isScheduled() && !isRescheduled() ) {
            throw new IllegalStateException();
        }
        return currentTask_.getTimeout();
    }

    long getTimeRemaining() {
        Date timeout = getNextTimeout();
        Date now = new Date();
        return (timeout.getTime() - now.getTime());
    }

    void expired() {
        expired_ = true;
    }

    public boolean isExpired() {
        return expired_;
    }

    /**
     * @return true if interval timer and false otherwise
     */
    public boolean isPeriodic() {
        // XXX ??? It'd be strange if the schedule-based timer is
        // not periodic. Otherwise need to check if schedule
        // already expired.
        return (schedule_ != null || intervalDuration_ > 0);
    }

    /**
     * @return true if this is a persistent timer
     */
    public boolean isPersistent() {
        return persistent_;
    }

    Serializable getInfo() {
        return info_;
    }

    //
    // java.lang.Object methods.
    //

    public int hashCode() {
        return timerId_.hashCode();
    }

    public boolean equals(Object other) {
        boolean equal = false;
        if( other instanceof RuntimeTimerState ) {
            equal = timerId_.equals(((RuntimeTimerState) other).timerId_);
        }
        return equal;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("'" + getTimerId() + "' ");
        buffer.append("'TimedObject = " + getTimedObjectEjbName() + "' ");
        buffer.append("'Application = " + getTimedObjectApplicationName()
                      + "' ");
        buffer.append("'" + stateToString() + "' ");
        buffer.append("'" + (isPeriodic() ? "PERIODIC' " : "SINGLE-ACTION' "));
        buffer.append("'Container ID = " + containerId_ + "' ");
        buffer.append("'" + getInitialExpiration() + "' ");
        buffer.append("'" + getIntervalDuration() + "' ");
        EJBTimerSchedule ts = getTimerSchedule();
        if( ts != null ) {
            buffer.append("'" + ts.getScheduleAsString() + "' ");
        }
        Object pk = getTimedObjectPrimaryKey();
        if( pk != null ) {
            buffer.append("'" + pk + "' ");
        }
        return buffer.toString();
    }
}

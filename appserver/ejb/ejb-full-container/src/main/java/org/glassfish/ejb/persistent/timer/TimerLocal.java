/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.persistent.timer;

import com.sun.ejb.containers.EJBTimerSchedule;
import com.sun.ejb.containers.TimerPrimaryKey;

import jakarta.ejb.CreateException;
import jakarta.ejb.Local;
import jakarta.ejb.TimerConfig;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Local view of the persistent representation of an EJB timer.
 *
 * @author Kenneth Saks
 * @author Marina Vatkina
 */
@Local
public interface TimerLocal {

    /**
     * Cancel timer.
     */
    void cancel(TimerPrimaryKey timerId) throws Exception;

    void cancelTimers(Collection<TimerState> timers);

    TimerState createTimer(String timerId,
                      long containerId, long applicationId, String ownerId,
                      Object timedObjectPrimaryKey,
                      Date initialExpiration, long intervalDuration,
                      EJBTimerSchedule schedule, TimerConfig timerConfig)
                      throws CreateException;

    TimerState findTimer(TimerPrimaryKey timerId);

    void remove(TimerPrimaryKey timerId);

    void remove(Set<TimerPrimaryKey> timerIds);

    //
    // Queries returning Timer Ids (TimerPrimaryKey)
    //

    Set findTimerIdsByContainer(long containerId);
    Set findActiveTimerIdsByContainer(long containerId);
    Set findActiveTimerIdsByContainers(Collection<Long> containerIds);
    Set findCancelledTimerIdsByContainer(long containerId);

    Set findTimerIdsOwnedByThisServerByContainer(long containerId);
    Set findActiveTimerIdsOwnedByThisServerByContainer(long containerId);
    Set findCancelledTimerIdsOwnedByThisServerByContainer(long containerId);

    Set findTimerIdsOwnedByThisServer();
    Set findActiveTimerIdsOwnedByThisServer();
    Set findCancelledTimerIdsOwnedByThisServer();

    Set findTimerIdsOwnedBy(String owner);
    Set findActiveTimerIdsOwnedBy(String owner);
    Set findCancelledTimerIdsOwnedBy(String owner);


    //
    // Queries returning Timer local objects
    //

    Set findTimersByContainer(long containerId);
    Set findActiveTimersByContainer(long containerId);
    Set findCancelledTimersByContainer(long containerId);

    Set findTimersOwnedByThisServerByContainer(long containerId);
    Set findActiveTimersOwnedByThisServerByContainer(long containerId);
    Set findCancelledTimersOwnedByThisServerByContainer(long containerId);

    Set findTimersOwnedByThisServer();
    Set findActiveTimersOwnedByThisServer();
    Set findCancelledTimersOwnedByThisServer();

    Set findTimersOwnedBy(String owner);
    Set findActiveTimersOwnedBy(String owner);
    Set findCancelledTimersOwnedBy(String owner);


    //
    // Queries returning counts
    //

    int countTimersByApplication(long applicationId);
    int countTimersByContainer(long containerId);
    int countActiveTimersByContainer(long containerId);
    int countCancelledTimersByContainer(long containerId);

    int countTimersOwnedByThisServerByContainer(long containerId);
    int countActiveTimersOwnedByThisServerByContainer(long containerId);
    int countCancelledTimersOwnedByThisServerByContainer(long containerId);

    int countTimersOwnedByThisServer();
    int countActiveTimersOwnedByThisServer();
    int countCancelledTimersOwnedByThisServer();

    int countTimersOwnedBy(String owner);
    int countActiveTimersOwnedBy(String owner);
    int countCancelledTimersOwnedBy(String owner);

    String[] countTimersOwnedByServerIds(String[] serverIds);


    // Perform health check on timer database
    boolean checkStatus(String resourceJndiName, boolean checkDatabase);

    // Migrate timers from one server instance to another via bulk update
    int migrateTimers(String fromOwnerId, String toOwnerId);

    // Delete all timers owned by this EJB (aka containerId)
    int deleteTimersByContainer(long containerId);

    // Delete all timers owned by this Application (aka applicationId)
    int deleteTimersByApplication(long applicationId);
}

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

package com.sun.ejb.containers;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EntityContext;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/*
 * EJBTimerServiceWrappers is the application-level representation
 * of the EJB timer service.
 *
 * @author Kenneth Saks
 */
public class EJBTimerServiceWrapper implements TimerService {

    private EJBTimerService timerService_;
    private EJBContextImpl ejbContext_;
    private EjbDescriptor ejbDescriptor_;

    private boolean entity_;

    // Only used for entity beans
    private Object timedObjectPrimaryKey_;

    public EJBTimerServiceWrapper(EJBTimerService timerService,
                                  EJBContextImpl ejbContext)
    {
        timerService_ = timerService;
        ejbContext_   = ejbContext;
        BaseContainer container = (BaseContainer) ejbContext.getContainer();
        ejbDescriptor_ = container.getEjbDescriptor();
        entity_       = false;
        timedObjectPrimaryKey_   = null;
    }

    public EJBTimerServiceWrapper(EJBTimerService timerService,
                                  EntityContext entityContext)
    {
        this(timerService, ((EJBContextImpl)entityContext));
        entity_       = true;
        // Delay access of primary key since this might have been called
        // from ejbCreate
        timedObjectPrimaryKey_   = null;
    }

    public Timer createTimer(long duration, Serializable info)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();
        checkDuration(duration);

        return createTimerInternal(duration, 0, info);
    }

    public Timer createTimer(long initialDuration, long intervalDuration,
                             Serializable info)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();
        checkInitialDuration(initialDuration);

        return createTimerInternal(initialDuration, intervalDuration, info);
    }

    public Timer createTimer(Date expiration, Serializable info)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();
        checkExpiration(expiration);

        return createTimerInternal(expiration, 0, info);
    }

    public Timer createTimer(Date initialExpiration, long intervalDuration,
                             Serializable info)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkCreateTimerCallPermission();
        checkExpiration(initialExpiration);

        return createTimerInternal(initialExpiration, intervalDuration, info);
    }

    public Timer createSingleActionTimer(long duration, TimerConfig timerConfig) throws
                     java.lang.IllegalArgumentException, java.lang.IllegalStateException,
                     jakarta.ejb.EJBException {
        checkCreateTimerCallPermission();
        checkDuration(duration);

        return createTimerInternal(duration, 0, timerConfig);
    }

    public Timer createIntervalTimer(long initialDuration,
                     long intervalDuration, TimerConfig timerConfig) throws
                     java.lang.IllegalArgumentException, java.lang.IllegalStateException,
                     jakarta.ejb.EJBException {
        checkCreateTimerCallPermission();
        checkInitialDuration(initialDuration);

        return createTimerInternal(initialDuration, intervalDuration, timerConfig);
    }

    public Timer createSingleActionTimer(Date initialExpiration,
                     TimerConfig timerConfig) throws
                     java.lang.IllegalArgumentException, java.lang.IllegalStateException,
                     jakarta.ejb.EJBException {
        checkCreateTimerCallPermission();
        checkExpiration(initialExpiration);

        return createTimerInternal(initialExpiration, 0, timerConfig);
    }

    public Timer createIntervalTimer(Date initialExpiration,
                     long intervalDuration, TimerConfig timerConfig) throws
                     java.lang.IllegalArgumentException, java.lang.IllegalStateException,
                     jakarta.ejb.EJBException {
        checkCreateTimerCallPermission();
        checkExpiration(initialExpiration);

        return createTimerInternal(initialExpiration, intervalDuration, timerConfig);
    }

    public Timer createCalendarTimer(ScheduleExpression schedule,
                  TimerConfig timerConfig) throws
                     java.lang.IllegalArgumentException, java.lang.IllegalStateException,
                     jakarta.ejb.EJBException {
        checkCreateTimerCallPermission();
        checkScheduleExpression(schedule);

        return createTimerInternal(schedule, timerConfig);
    }

    public Timer createCalendarTimer(ScheduleExpression schedule) throws
                     java.lang.IllegalArgumentException, java.lang.IllegalStateException,
                     jakarta.ejb.EJBException {
        checkCreateTimerCallPermission();
        checkScheduleExpression(schedule);

        TimerConfig tc = new TimerConfig();
        tc.setInfo(null);

        return createTimerInternal(schedule, tc);
    }

    public Collection<Timer> getTimers() throws IllegalStateException, EJBException {

        checkCallPermission();

        Collection timerIds = new HashSet();

        if( ejbContext_.isTimedObject() ) {
            try {
                timerIds = timerService_.getTimerIds
                    (ejbDescriptor_.getUniqueId(),  getTimedObjectPrimaryKey());
            } catch(Exception fe) {
                EJBException ejbEx = new EJBException();
                ejbEx.initCause(fe);
                throw ejbEx;
            }
        }

        Collection<Timer> timerWrappers = new HashSet();

        for(Iterator iter = timerIds.iterator(); iter.hasNext();) {
            TimerPrimaryKey next = (TimerPrimaryKey) iter.next();
            timerWrappers.add( new TimerWrapper(next, timerService_) );
        }

        return timerWrappers;
    }

    @Override
    public Collection<Timer> getAllTimers() throws IllegalStateException, EJBException {

        checkCallPermission();

        Collection<Timer> timerWrappers = new HashSet();
        Collection<Long> containerIds = ejbDescriptor_.getEjbBundleDescriptor().getDescriptorIds();
        Collection<TimerPrimaryKey> timerIds =
                timerService_.getTimerIds(containerIds);
        for (TimerPrimaryKey timerPrimaryKey : timerIds) {
            timerWrappers.add(new TimerWrapper(timerPrimaryKey, timerService_));
        }
        return timerWrappers;
    }

    private Object getTimedObjectPrimaryKey() {
        if( !entity_ ) {
            return null;
        } else {
            synchronized(this) {
                if( timedObjectPrimaryKey_ == null ) {
                    timedObjectPrimaryKey_ =
                        ((EntityContext) ejbContext_).getPrimaryKey();
                }
            }
        }
        return timedObjectPrimaryKey_;
    }

    private void checkCreateTimerCallPermission()
        throws IllegalStateException {
        if( ejbContext_.isTimedObject() ) {
            checkCallPermission();
        } else {
            throw new IllegalStateException("EJBTimerService.createTimer can "
                + "only be called from a timed object.  This EJB does not "
                + "implement jakarta.ejb.TimedObject");
        }
    }

    private void checkCallPermission()
        throws IllegalStateException {
        ejbContext_.checkTimerServiceMethodAccess();
    }

    private Timer createTimerInternal(long initialDuration, long intervalDuration,
                             Serializable info)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        TimerConfig tc = new TimerConfig();
        tc.setInfo(info);

        return createTimerInternal(initialDuration, intervalDuration, tc);
    }

    private Timer createTimerInternal(long initialDuration, long intervalDuration,
                             TimerConfig tc)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkIntervalDuration(intervalDuration);

        TimerPrimaryKey timerId = null;
        try {
            timerId = timerService_.createTimer(ejbDescriptor_.getUniqueId(),
                    ejbDescriptor_.getApplication().getUniqueId(),
                    getTimedObjectPrimaryKey(),
                initialDuration, intervalDuration, tc);
        } catch(CreateException ce) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ce);
            throw ejbEx;
        }

        return new TimerWrapper(timerId, timerService_);
    }

    private Timer createTimerInternal(Date initialExpiration, long intervalDuration,
                             Serializable info)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        TimerConfig tc = new TimerConfig();
        tc.setInfo(info);

        return createTimerInternal(initialExpiration, intervalDuration, tc);
    }

    private Timer createTimerInternal(Date initialExpiration, long intervalDuration,
                             TimerConfig tc)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        checkIntervalDuration(intervalDuration);

        TimerPrimaryKey timerId = null;
        try {
            timerId = timerService_.createTimer(ejbDescriptor_.getUniqueId(),
                    ejbDescriptor_.getApplication().getUniqueId(),
                    getTimedObjectPrimaryKey(),
                initialExpiration, intervalDuration, tc);
        } catch(CreateException ce) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ce);
            throw ejbEx;
        }

        return new TimerWrapper(timerId, timerService_);
    }

    private Timer createTimerInternal(ScheduleExpression schedule, TimerConfig tc)
        throws IllegalArgumentException, IllegalStateException, EJBException {

        TimerPrimaryKey timerId = null;
        try {
            timerId = timerService_.createTimer(ejbDescriptor_.getUniqueId(),
                    ejbDescriptor_.getApplication().getUniqueId(),
                    getTimedObjectPrimaryKey(),
                    new EJBTimerSchedule(schedule), tc);
        } catch(CreateException ce) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ce);
            throw ejbEx;
        }

        return new TimerWrapper(timerId, timerService_);
    }

    private void checkDuration(long duration)
            throws IllegalArgumentException {
        if( duration < 0 ) {
            throw new IllegalArgumentException("invalid duration=" + duration);
        }
    }

    private void checkInitialDuration(long initialDuration)
            throws IllegalArgumentException {
        if( initialDuration < 0 ) {
            throw new IllegalArgumentException("invalid initial duration = " +
                                               initialDuration);
        }
    }

    private void checkIntervalDuration(long intervalDuration)
            throws IllegalArgumentException {
        if( intervalDuration < 0 ) {
            throw new IllegalArgumentException("invalid interval duration = " +
                                               intervalDuration);
        }
    }

    private void checkScheduleExpression(ScheduleExpression expression)
            throws IllegalArgumentException {
        if( expression == null ) {
            throw new IllegalArgumentException("null ScheduleExpression");
        }
    }

    private void checkExpiration(Date expiration)
            throws IllegalArgumentException {
        if( expiration == null ) {
            throw new IllegalArgumentException("null expiration");
        }
        if( expiration.getTime() < 0 ) {
            throw new IllegalArgumentException("Negative expiration");
        }
    }
}

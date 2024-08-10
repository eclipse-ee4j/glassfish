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

package org.glassfish.ejb.deployment.annotation.handlers;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ScheduledTimerDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;

import jakarta.ejb.MessageDriven;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.Schedule
 * annotation on methods of a Bean class.
 *
 * @author Marina Vatkina
 */
@Service
@AnnotationHandlerFor(Schedule.class)
public class ScheduleHandler extends AbstractAttributeHandler {

    public ScheduleHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        return processSchedule((Schedule)ainfo.getAnnotation(), ainfo, ejbContexts);
    }

    protected HandlerProcessingResult processSchedule(Schedule sch,
            AnnotationInfo ainfo, EjbContext[] ejbContexts)
            throws AnnotationProcessorException {

        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = (EjbDescriptor) ejbContext.getDescriptor();

            if (ElementType.METHOD.equals(ainfo.getElementType())) {
                Method annMethod = (Method) ainfo.getAnnotatedElement();

                // .xml-defined timer method overrides @Schedule
                if( !ejbDesc.hasScheduledTimerMethodFromDD(annMethod)) {
                    ScheduledTimerDescriptor sd = new ScheduledTimerDescriptor();
                    sd.setSecond(sch.second());
                    sd.setMinute(sch.minute());
                    sd.setHour(sch.hour());
                    sd.setDayOfMonth(sch.dayOfMonth());
                    sd.setMonth(sch.month());
                    sd.setDayOfWeek(sch.dayOfWeek());
                    sd.setYear(sch.year());
                    sd.setTimezone(sch.timezone());
                    sd.setPersistent(sch.persistent());
                    sd.setInfo(sch.info());
                    sd.setTimeoutMethod(new MethodDescriptor(annMethod));

                    ejbDesc.addScheduledTimerDescriptor(sd);

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("@@@ Found Schedule on " + annMethod);

                        logger.fine("@@@ TimerConfig : " +
                                ((sd.getInfo() != null && !sd.getInfo().equals(""))? sd.getInfo() : null) +
                                " # " + sd.getPersistent());
                    }
                }
            }
        }

        return getDefaultProcessedResult();
    }

    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    public Class<? extends Annotation>[] getTypeDependencies() {

        return new Class[] {Stateless.class, Singleton.class, MessageDriven.class};

    }

    protected boolean supportTypeInheritance() {
        return true;
    }
}

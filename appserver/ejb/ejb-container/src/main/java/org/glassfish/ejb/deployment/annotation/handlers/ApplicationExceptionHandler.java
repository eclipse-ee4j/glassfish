/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.EjbApplicationExceptionInfo;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;

import jakarta.ejb.ApplicationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * Handles {@link ApplicationException}
 */
@Service
@AnnotationHandlerFor(ApplicationException.class)
public class ApplicationExceptionHandler extends AbstractHandler {

    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo) throws AnnotationProcessorException {
        AnnotatedElement element = ainfo.getAnnotatedElement();
        Annotation annotation = ainfo.getAnnotation();
        AnnotatedElementHandler handler = ainfo.getProcessingContext().getHandler();

        if (handler instanceof EjbBundleContext) {
            EjbBundleContext ejbBundleContext = (EjbBundleContext) handler;
            EjbBundleDescriptorImpl ejbBundle = ejbBundleContext.getDescriptor();
            ApplicationException appExcAnn = (ApplicationException) annotation;

            // Set on descriptor unless the same application exception was defined in ejb-jar.xml
            Class<?> annotatedClass = (Class<?>) element;
            if (!ejbBundle.getApplicationExceptions().containsKey(annotatedClass.getName())) {
                EjbApplicationExceptionInfo appExcInfo = new EjbApplicationExceptionInfo();
                appExcInfo.setExceptionClassName(annotatedClass.getName());
                appExcInfo.setRollback(appExcAnn.rollback());
                appExcInfo.setInherited(appExcAnn.inherited());
                ejbBundle.addApplicationException(appExcInfo);
            }
        }
        return getDefaultProcessedResult();
    }
}

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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import jakarta.ejb.ApplicationException;

import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;
import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbApplicationExceptionInfo;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * Handles @jakarta.ejb.ApplicationException
 */
@Service
@AnnotationHandlerFor(ApplicationException.class)
public class ApplicationExceptionHandler extends AbstractHandler {

    public ApplicationExceptionHandler() {
    }

     public HandlerProcessingResult processAnnotation
         (AnnotationInfo ainfo) throws AnnotationProcessorException {

        AnnotatedElement ae = ainfo.getAnnotatedElement();
        Annotation annotation = ainfo.getAnnotation();

        AnnotatedElementHandler aeHandler =
            ainfo.getProcessingContext().getHandler();


        if (aeHandler instanceof EjbBundleContext) {
            EjbBundleContext ejbBundleContext = (EjbBundleContext)aeHandler;

            EjbBundleDescriptorImpl ejbBundle = (EjbBundleDescriptorImpl) ejbBundleContext.getDescriptor();

            ApplicationException appExcAnn = (ApplicationException) annotation;

            EjbApplicationExceptionInfo appExcInfo = new
                EjbApplicationExceptionInfo();
            Class annotatedClass = (Class) ae;
            appExcInfo.setExceptionClassName(annotatedClass.getName());
            appExcInfo.setRollback(appExcAnn.rollback());
            appExcInfo.setInherited(appExcAnn.inherited());

            // Set on descriptor unless the same application exception was defined
            // in ejb-jar.xml
            if( !ejbBundle.getApplicationExceptions().containsKey(annotatedClass.getName()) ) {
                ejbBundle.addApplicationException(appExcInfo);
            }

        }

        return getDefaultProcessedResult();

     }

    protected boolean supportTypeInheritance() {
        return true;
    }
}

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
import com.sun.enterprise.deployment.annotation.context.EjbContext;

import jakarta.ejb.Remove;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbRemovalInfo;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.Remove attribute
 *
 */
@Service
@AnnotationHandlerFor(Remove.class)
public class RemoveHandler extends AbstractAttributeHandler {

    public RemoveHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            EjbContext[] ejbContexts) throws AnnotationProcessorException {

        Remove remove = (Remove) ainfo.getAnnotation();

        for(EjbContext next : ejbContexts) {

            EjbSessionDescriptor sessionDescriptor =
                (EjbSessionDescriptor) next.getDescriptor();

            Method m = (Method) ainfo.getAnnotatedElement();
            MethodDescriptor removeMethod =
                new MethodDescriptor(m, MethodDescriptor.EJB_BEAN);

            EjbRemovalInfo removalInfo =
                sessionDescriptor.getRemovalInfo(removeMethod);

            if (removalInfo == null) {
                // if this element is not defined in xml
                // use all information from annotation
                removalInfo = new EjbRemovalInfo();
                removalInfo.setRemoveMethod(removeMethod);
                removalInfo.setRetainIfException(remove.retainIfException());
                sessionDescriptor.addRemoveMethod(removalInfo);
            } else {
                // if this element is already defined in xml
                // set the retainIfException only if this subelement
                // is not defined in xml
                if (! removalInfo.isRetainIfExceptionSet()) {
                    removalInfo.setRetainIfException(
                        remove.retainIfException());
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
        return getEjbAnnotationTypes();
    }
}

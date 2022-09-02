/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.core.MetadataSource;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.Method;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling jakarta.annotation.PostConstruct
 */
@Service
@AnnotationHandlerFor(PostConstruct.class)
public class PostConstructHandler extends AbstractResourceHandler {


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        Method annMethod = (Method) ainfo.getAnnotatedElement();
        validateAnnotatedLifecycleMethod(annMethod);
        String pcMethodName = annMethod.getName();
        String pcClassName = annMethod.getDeclaringClass().getName();

        for (ResourceContainerContext rcContext : rcContexts) {
            LifecycleCallbackDescriptor postConstructDesc = new LifecycleCallbackDescriptor();
            postConstructDesc.setLifecycleCallbackClass(pcClassName);
            postConstructDesc.setLifecycleCallbackMethod(pcMethodName);
            postConstructDesc.setMetadataSource(MetadataSource.ANNOTATION);
            // override by xml is handled in addPostConstructDescriptor
            rcContext.addPostConstructDescriptor(postConstructDesc);
        }

        return getDefaultProcessedResult();
    }
}

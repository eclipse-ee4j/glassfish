/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.ContextServiceDefinitionDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.inject.Inject;

import java.util.Set;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.MetadataSource.ANNOTATION;

@Service
@AnnotationHandlerFor(ContextServiceDefinition.List.class)
public class ContextServiceDefinitionListHandler extends AbstractResourceHandler {

    @Inject
    private ContextServiceDefinitionConverter converter;

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        ContextServiceDefinition.List annotation = (ContextServiceDefinition.List) ainfo.getAnnotation();
        Set<ContextServiceDefinitionData> set = converter.convert(annotation.value());
        for (ContextServiceDefinitionData data : set) {
            ContextServiceDefinitionDescriptor descriptor = new ContextServiceDefinitionDescriptor(data, ANNOTATION);
            for (ResourceContainerContext context : rcContexts) {
                context.getResourceDescriptors(JavaEEResourceType.CSDD).add(descriptor);
            }
        }
        return getDefaultProcessedResult();
    }
}

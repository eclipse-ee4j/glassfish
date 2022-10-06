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

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.factory.ManagedExecutorDefinitionData;

import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.MetadataSource.ANNOTATION;


@Service
@AnnotationHandlerFor(ManagedExecutorDefinition.class)
public class ManagedExecutorDefinitionHandler extends ContextualResourceHandler {

    @Inject
    private ManagedExecutorDefinitionConverter converter;

    @Override
    protected Class<ManagedExecutorDefinitionDescriptor> getAcceptableDescriptorType() {
        return ManagedExecutorDefinitionDescriptor.class;
    }


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        ManagedExecutorDefinition annotation = (ManagedExecutorDefinition) ainfo.getAnnotation();
        ManagedExecutorDefinitionData data = converter.convert(annotation);
        updateDescriptors(data, rcContexts);
        return getDefaultProcessedResult();
    }


    private void updateDescriptors(ManagedExecutorDefinitionData data, ResourceContainerContext[] contexts) {
        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> descriptors = context.getResourceDescriptors(JavaEEResourceType.MEDD);
            List<ManagedExecutorDefinitionData> contextData = getExisting(data, descriptors);
            if (contextData.isEmpty()) {
                descriptors.add(new ManagedExecutorDefinitionDescriptor(data, ANNOTATION));
            } else {
                for (ManagedExecutorDefinitionData existingData : contextData) {
                    converter.merge(data, existingData);
                }
            }
        }
    }


    private List<ManagedExecutorDefinitionData> getExisting(ManagedExecutorDefinitionData data,
        Set<ResourceDescriptor> descriptors) {
        return descriptors.stream().filter(d -> isSameDefinition(data, d))
            .map(d -> getAcceptableDescriptorType().cast(d).getData()).collect(Collectors.toList());
    }
}

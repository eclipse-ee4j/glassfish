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

import com.sun.enterprise.deployment.ManagedScheduledExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;

import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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
@AnnotationHandlerFor(ManagedScheduledExecutorDefinition.List.class)
public class ManagedScheduledExecutorDefinitionListHandler extends ContextualResourceHandler {
    private static final Logger LOG = System.getLogger(ManagedScheduledExecutorDefinitionListHandler.class.getName());

    @Inject
    private ManagedScheduledExecutorDefinitionConverter converter;

    @Override
    protected Class<ManagedScheduledExecutorDefinitionDescriptor> getAcceptableDescriptorType() {
        return ManagedScheduledExecutorDefinitionDescriptor.class;
    }


    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        ManagedScheduledExecutorDefinition.List annotation = (ManagedScheduledExecutorDefinition.List) ainfo.getAnnotation();
        Set<ManagedScheduledExecutorDefinitionData> annotationDefinitions = converter.convert(annotation.value());
        for (ManagedScheduledExecutorDefinitionData data : annotationDefinitions) {
            updateDescriptors(data, rcContexts);
        }
        return getDefaultProcessedResult();
    }

    // FIXME: copy and paste with brother classes
    private void updateDescriptors(ManagedScheduledExecutorDefinitionData data, ResourceContainerContext[] contexts) {
        LOG.log(Level.DEBUG, "updateDescriptors(data={0}, contexts.length={1})", data, contexts.length);
        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> descriptors = context.getResourceDescriptors(JavaEEResourceType.MSEDD);
            List<ManagedScheduledExecutorDefinitionData> existingDefinitions = getExisting(data, descriptors);
            if (existingDefinitions.isEmpty()) {
                LOG.log(Level.DEBUG, "Adding: {0}", data);
                descriptors.add(new ManagedScheduledExecutorDefinitionDescriptor(data, ANNOTATION));
            } else {
                for (ManagedScheduledExecutorDefinitionData existingData : existingDefinitions) {
                    converter.merge(data, existingData);
                }
            }
        }
    }


    private List<ManagedScheduledExecutorDefinitionData> getExisting(ManagedScheduledExecutorDefinitionData data,
        Set<ResourceDescriptor> descriptors) {
        return descriptors.stream().filter(d -> isSameDefinition(data, d))
            .map(d -> getAcceptableDescriptorType().cast(d).getData()).collect(Collectors.toList());
    }
}

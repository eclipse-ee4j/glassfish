/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.deployment.MetadataSource;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

/**
 * @author David Matejcek
 */
@Service
class ManagedScheduledExecutorDefinitionConverter extends
    ConcurrencyDefinitionConvertor<ManagedScheduledExecutorDefinitionData, ManagedScheduledExecutorDefinitionDescriptor> {

    private static final Logger LOG = System.getLogger(ManagedScheduledExecutorDefinitionConverter.class.getName());

    ManagedScheduledExecutorDefinitionConverter() {
        super(ManagedScheduledExecutorDefinitionDescriptor.class, JavaEEResourceType.MSEDD);
    }


    @Override
    ManagedScheduledExecutorDefinitionDescriptor createDescriptor(ManagedScheduledExecutorDefinitionData data) {
        return new ManagedScheduledExecutorDefinitionDescriptor(data, MetadataSource.ANNOTATION);
    }


    @Override
    ManagedScheduledExecutorDefinitionData getData(ManagedScheduledExecutorDefinitionDescriptor descriptor) {
        return descriptor.getData();
    }


    Set<ManagedScheduledExecutorDefinitionData> convert(ManagedScheduledExecutorDefinition[] annotation) {
        LOG.log(Level.TRACE, "convert(annotation={0})", (Object) annotation);
        if (annotation == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(annotation).map(this::convert).collect(Collectors.toSet());
    }


    ManagedScheduledExecutorDefinitionData convert(ManagedScheduledExecutorDefinition annotation) {
        LOG.log(Level.DEBUG, "convert(annotation={0})", annotation);
        ManagedScheduledExecutorDefinitionData data = new ManagedScheduledExecutorDefinitionData();
        data.setName(TranslatedConfigView.expandValue(annotation.name()));
        for (Class<?> clazz : annotation.qualifiers()) {
            data.addQualifier(clazz.getCanonicalName());
        }
        data.setContext(TranslatedConfigView.expandValue(annotation.context()));
        data.setVirtual(annotation.virtual());
        if (annotation.hungTaskThreshold() < 0) {
            data.setHungTaskThreshold(0);
        } else {
            data.setHungTaskThreshold(annotation.hungTaskThreshold());
        }
        if (annotation.maxAsync() < 0) {
            data.setMaxAsync(Integer.MAX_VALUE);
        } else {
            data.setMaxAsync(annotation.maxAsync());
        }
        return data;
    }


    @Override
    void merge(ManagedScheduledExecutorDefinitionData annotation, ManagedScheduledExecutorDefinitionData descriptor) {
        LOG.log(Level.DEBUG, "merge(annotation={0}, descriptor={1})", annotation, descriptor);
        if (!annotation.getName().equals(descriptor.getName())) {
            throw new IllegalArgumentException("Cannot merge managed executors with different names: "
                + annotation.getName() + " x " + descriptor.getName());
        }

        mergeQualifiers(annotation, descriptor);

        if (!descriptor.isVirtual()) {
            descriptor.setVirtual(annotation.isVirtual());
        }
        if (descriptor.getHungTaskThreshold() <= 0 && annotation.getHungTaskThreshold() != 0) {
            descriptor.setHungTaskThreshold(annotation.getHungTaskThreshold());
        }
        if (descriptor.getMaxAsync() <= 0) {
            descriptor.setMaxAsync(annotation.getMaxAsync());
        }
        if (descriptor.getContext() == null && annotation.getContext() != null
            && !annotation.getContext().isBlank()) {
            descriptor.setContext(TranslatedConfigView.expandValue(annotation.getContext()));
        }
    }
}

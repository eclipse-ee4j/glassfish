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

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;

import jakarta.enterprise.concurrent.ManagedExecutorDefinition;

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
class ManagedExecutorDefinitionConverter
    extends ConcurrencyDefinitionConvertor<ManagedExecutorDefinitionData, ManagedExecutorDefinitionDescriptor> {

    private static final Logger LOG = System.getLogger(ManagedExecutorDefinitionConverter.class.getName());

    ManagedExecutorDefinitionConverter() {
        super(ManagedExecutorDefinitionDescriptor.class, JavaEEResourceType.MEDD);
    }


    @Override
    ManagedExecutorDefinitionDescriptor createDescriptor(ManagedExecutorDefinitionData data) {
        return new ManagedExecutorDefinitionDescriptor(data, MetadataSource.ANNOTATION);
    }


    @Override
    ManagedExecutorDefinitionData getData(ManagedExecutorDefinitionDescriptor descriptor) {
        return descriptor.getData();
    }


    Set<ManagedExecutorDefinitionData> convert(ManagedExecutorDefinition[] annotation) {
        LOG.log(Level.TRACE, "convert(annotation={0})", (Object) annotation);
        if (annotation == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(annotation).map(this::convert).collect(Collectors.toSet());
    }


    ManagedExecutorDefinitionData convert(ManagedExecutorDefinition annotation) {
        LOG.log(Level.DEBUG, "convert(annotation={0})", annotation);
        ManagedExecutorDefinitionData data = new ManagedExecutorDefinitionData();
        data.setName(TranslatedConfigView.expandValue(annotation.name()));
        data.setContext(TranslatedConfigView.expandValue(annotation.context()));
        for (Class<?> clazz : annotation.qualifiers()) {
            data.addQualifier(clazz.getCanonicalName());
        }
        data.setVirtual(annotation.virtual());

        if (annotation.hungTaskThreshold() < 0) {
            data.setHungAfterSeconds(0);
        } else {
            data.setHungAfterSeconds(annotation.hungTaskThreshold());
        }
        if (annotation.maxAsync() < 0) {
            data.setMaximumPoolSize(Integer.MAX_VALUE);
        } else {
            data.setMaximumPoolSize(annotation.maxAsync());
        }
        return data;
    }


    @Override
    void merge(ManagedExecutorDefinitionData annotation, ManagedExecutorDefinitionData descriptor) {
        LOG.log(Level.DEBUG, "merge(annotation={0}, descriptor={1})", annotation, descriptor);
        if (!annotation.getName().equals(descriptor.getName())) {
            throw new IllegalArgumentException("Cannot merge managed executors with different names: "
                + annotation.getName() + " x " + descriptor.getName());
        }

        mergeQualifiers(annotation, descriptor);

        if (!descriptor.isVirtual()) {
            descriptor.setVirtual(annotation.isVirtual());
        }
        if (descriptor.getHungAfterSeconds() <= 0 && annotation.getHungAfterSeconds() != 0) {
            descriptor.setHungAfterSeconds(annotation.getHungAfterSeconds());
        }
        if (descriptor.getMaximumPoolSize() <= 0 && annotation.getMaximumPoolSize() > 0
            && annotation.getMaximumPoolSize() < Integer.MAX_VALUE) {
            descriptor.setMaximumPoolSize(annotation.getMaximumPoolSize());
        }
        if (descriptor.getContext() == null && annotation.getContext() != null
            && !annotation.getContext().isBlank()) {
            descriptor.setContext(TranslatedConfigView.expandValue(annotation.getContext()));
        }
    }
}

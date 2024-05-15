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

import com.sun.enterprise.deployment.ManagedThreadFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;

import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;

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
class ManagedThreadFactoryDefinitionConverter extends
    ConcurrencyDefinitionConvertor<ManagedThreadFactoryDefinitionData, ManagedThreadFactoryDefinitionDescriptor> {

    private static final Logger LOG = System.getLogger(ManagedThreadFactoryDefinitionConverter.class.getName());

    ManagedThreadFactoryDefinitionConverter() {
        super(ManagedThreadFactoryDefinitionDescriptor.class, JavaEEResourceType.MTFDD);
    }


    @Override
    ManagedThreadFactoryDefinitionDescriptor createDescriptor(ManagedThreadFactoryDefinitionData data) {
        return new ManagedThreadFactoryDefinitionDescriptor(data, MetadataSource.ANNOTATION);
    }


    @Override
    ManagedThreadFactoryDefinitionData getData(ManagedThreadFactoryDefinitionDescriptor descriptor) {
        return descriptor.getData();
    }


    Set<ManagedThreadFactoryDefinitionData> convert(final ManagedThreadFactoryDefinition[] annotation) {
        LOG.log(Level.TRACE, "convert(annotation={0})", (Object) annotation);
        if (annotation == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(annotation).map(this::convert).collect(Collectors.toSet());
    }


    ManagedThreadFactoryDefinitionData convert(ManagedThreadFactoryDefinition annotation) {
        LOG.log(Level.DEBUG, "convert(annotation={0})", annotation);
        ManagedThreadFactoryDefinitionData data = new ManagedThreadFactoryDefinitionData();
        data.setName(TranslatedConfigView.expandValue(annotation.name()));
        data.setQualifiers(annotation.qualifiers());
        data.setContext(TranslatedConfigView.expandValue(annotation.context()));
        if (annotation.priority() <= 0) {
            data.setPriority(Thread.NORM_PRIORITY);
        } else {
            data.setPriority(annotation.priority());
        }
        return data;
    }


    @Override
    void merge(ManagedThreadFactoryDefinitionData annotationData,
        ManagedThreadFactoryDefinitionData descriptorData) {
        LOG.log(Level.DEBUG, "merge(annotationData={0}, descriptorData={1})", annotationData, descriptorData);
        if (!annotationData.getName().equals(descriptorData.getName())) {
            throw new IllegalArgumentException("Cannot merge managed thread factories with different names: "
                + annotationData.getName() + " x " + descriptorData.getName());
        }
        if (descriptorData.getQualifiers().length == 0) {
            descriptorData.setQualifiers(annotationData.getQualifiers());
        }
        if (descriptorData.getPriority() == -1 && annotationData.getPriority() != -1) {
            descriptorData.setPriority(annotationData.getPriority());
        }
        if (descriptorData.getContext() == null && annotationData.getContext() != null
            && !annotationData.getContext().isBlank()) {
            descriptorData.setContext(TranslatedConfigView.expandValue(annotationData.getContext()));
        }
    }
}

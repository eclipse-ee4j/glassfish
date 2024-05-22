/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2024 Payara Foundation and/or its affiliates
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
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;

import jakarta.enterprise.concurrent.ContextServiceDefinition;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.ResourceDescriptor.getJavaComponentJndiName;

/**
 * @author David Matejcek
 */
@Service
class ContextServiceDefinitionConverter {
    private static final Logger LOG = System.getLogger(ContextServiceDefinitionConverter.class.getName());

    Set<ContextServiceDefinitionData> convert(ContextServiceDefinition[] definitions) {
        LOG.log(Level.TRACE, "convert(definitions={0})", (Object) definitions);
        if (definitions == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(definitions).map(this::convert).collect(Collectors.toSet());
    }

    ContextServiceDefinitionData convert(ContextServiceDefinition annotation) {
        LOG.log(Level.DEBUG, "convert(definition={0})", annotation);
        Set<String> unused = collectUnusedContexts(annotation);
        ContextServiceDefinitionData data = new ContextServiceDefinitionData();
        data.setName(new SimpleJndiName(TranslatedConfigView.expandValue(annotation.name())));
        data.setPropagated(evaluateContexts(annotation.propagated(), unused));
        data.setCleared(evaluateContexts(annotation.cleared(), unused));
        data.setUnchanged(evaluateContexts(annotation.unchanged(), unused));
        for (Class<?> clazz : annotation.qualifiers()) {
            data.addQualifier(clazz.getCanonicalName());
        }
        return data;
    }


    private Set<String> collectUnusedContexts(ContextServiceDefinition definition) {
        Set<String> usedContexts = new HashSet<>();
        for (String context : definition.propagated()) {
            usedContexts.add(context);
        }
        for (String context : definition.cleared()) {
            boolean previous = usedContexts.add(context);
            if (!previous) {
                throw new IllegalArgumentException("Duplicit context " + context + " in " + usedContexts
                    + " and cleared context attributes in ContextServiceDefinition annotation!");
            }
        }
        for (String context : definition.unchanged()) {
            boolean previous = usedContexts.add(context);
            if (!previous) {
                throw new IllegalArgumentException("Duplicit context " + context + " in " + previous
                    + " and unchanged context attributes in ContextServiceDefinition annotation!");
            }
        }
        Set<String> allStandardContexts = new HashSet<>(Set.of(
            ContextServiceDefinition.APPLICATION,
            ContextServiceDefinition.SECURITY,
            ContextServiceDefinition.TRANSACTION));
        allStandardContexts.removeAll(usedContexts);
        return allStandardContexts;
    }

    private Set<String> evaluateContexts(String[] sourceContexts, Set<String> unusedContexts) {
        Set<String> contexts = new HashSet<>();
        for (String context : sourceContexts) {
            if (ContextServiceDefinition.ALL_REMAINING.equals(context)) {
                contexts.addAll(unusedContexts);
                contexts.add(ContextServiceDefinition.ALL_REMAINING);
            } else {
                contexts.add(context);
            }
        }
        return contexts;
    }

    private void merge(ContextServiceDefinitionData annotation, ContextServiceDefinitionData descriptor) {
        LOG.log(Level.DEBUG, "merge(annotation={0}, descriptor={1})", annotation, descriptor);
        if (!annotation.getName().equals(descriptor.getName())) {
            throw new IllegalArgumentException("Cannot merge context services with different names: "
                + annotation.getName() + " x " + descriptor.getName());
        }

        if (descriptor.getCleared() == null && annotation.getCleared() != null) {
            descriptor.setCleared(new HashSet<>(annotation.getCleared()));
        }

        if (descriptor.getPropagated() == null && annotation.getPropagated() != null) {
            descriptor.setPropagated(new HashSet<>(annotation.getPropagated()));
        }

        if (descriptor.getUnchanged() == null && annotation.getUnchanged() != null) {
            descriptor.setUnchanged(new HashSet<>(annotation.getUnchanged()));
        }

        // FIXME: descriptor can have one empty qualifier, then it should override annotation.
        // TODO: null or yet another additional attribute? 4ALL concurrency descriptors.
        if (descriptor.getQualifiers().isEmpty() && !annotation.getQualifiers().isEmpty()) {
            descriptor.setQualifiers(annotation.getQualifiers());
        }
    }

    public void updateDescriptors(ContextServiceDefinitionData annotation, ResourceContainerContext[] contexts) {
        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> descriptors = context.getResourceDescriptors(JavaEEResourceType.CSDD);
            List<ContextServiceDefinitionData> existing = getExisting(annotation, descriptors);
            if (existing.isEmpty()) {
                descriptors.add(new ContextServiceDefinitionDescriptor(annotation, MetadataSource.ANNOTATION));
            } else {
                for (ContextServiceDefinitionData existingData : existing) {
                    merge(annotation, existingData);
                }
            }
        }
    }

    private List<ContextServiceDefinitionData> getExisting(ContextServiceDefinitionData data, Set<ResourceDescriptor> descriptors) {
        return descriptors.stream().filter(d -> isSameDefinition(data, d)).map(d -> ((ContextServiceDefinitionDescriptor) d).getData())
            .collect(Collectors.toList());
    }


    private boolean isSameDefinition(ContextServiceDefinitionData data, ResourceDescriptor descriptor) {
        return descriptor instanceof ContextServiceDefinitionDescriptor
            && Objects.equals(getJavaComponentJndiName(descriptor.getJndiName().toString()),
                getJavaComponentJndiName(data.getName().toString()));
    }
}

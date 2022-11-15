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

import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.deployment.common.JavaEEResourceType;

import static com.sun.enterprise.deployment.ResourceDescriptor.getJavaComponentJndiName;

/**
 * @author David Matejcek
 */
abstract class ConcurrencyDefinitionConvertor<D extends ContextualResourceDefinition, T extends ResourceDescriptor> {

    private final Class<T> descriptorClass;
    private final JavaEEResourceType descriptorType;

    ConcurrencyDefinitionConvertor(Class<T> descriptorClass, JavaEEResourceType descriptorType) {
        this.descriptorClass = descriptorClass;
        this.descriptorType = descriptorType;
    }

    abstract T createDescriptor(D data);

    abstract D getData(T descriptor);

    abstract void merge(D annotationData, D descriptorData);

    final void updateDescriptors(D data, ResourceContainerContext[] contexts) {
        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> descriptors = context.getResourceDescriptors(descriptorType);
            List<D> existing = getExisting(data, descriptors);
            if (existing.isEmpty()) {
                descriptors.add(createDescriptor(data));
            } else {
                for (D existingData : existing) {
                    merge(data, existingData);
                }
            }
        }
    }


    // isSameDefinition ensures it.
    @SuppressWarnings("unchecked")
    private List<D> getExisting(D data, Set<ResourceDescriptor> descriptors) {
        return descriptors.stream().filter(d -> isSameDefinition(data, d)).map(d -> getData((T) d))
            .collect(Collectors.toList());
    }


    private boolean isSameDefinition(D data, ResourceDescriptor descriptor) {
        return descriptorClass.isInstance(descriptor) && Objects
            .equals(getJavaComponentJndiName(descriptor.getJndiName().toString()), getJavaComponentJndiName(data.getName()));
    }

}

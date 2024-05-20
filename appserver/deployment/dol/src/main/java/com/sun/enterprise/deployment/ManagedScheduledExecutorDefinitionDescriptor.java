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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.annotation.handlers.ContextualResourceDefinition;
import com.sun.enterprise.deployment.annotation.handlers.ManagedScheduledExecutorDefinitionData;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * @author David Matejcek
 */
public class ManagedScheduledExecutorDefinitionDescriptor extends ResourceDescriptor
    implements ContextualResourceDefinition {

    private static final long serialVersionUID = 1L;

    private final ManagedScheduledExecutorDefinitionData data;

    public ManagedScheduledExecutorDefinitionDescriptor() {
        this(new ManagedScheduledExecutorDefinitionData(), MetadataSource.XML);
    }


    public ManagedScheduledExecutorDefinitionDescriptor(ManagedScheduledExecutorDefinitionData data, MetadataSource source) {
        this.data = data;
        setResourceType(JavaEEResourceType.MSEDD);
        setMetadataSource(source);
    }


    @Override
    public String getName() {
        return this.data.getName();
    }


    @Override
    public void setName(String name) {
        this.data.setName(name);
    }


    @Override
    public String getContext() {
        return data.getContext();
    }


    @Override
    public void setContext(String context) {
        data.setContext(context);
    }


    @Override
    public List<Class<?>> getQualifiers() {
        return data.getQualifiers();
    }


    @Override
    public void setQualifiers(List<Class<?>> qualifiers) {
        data.setQualifiers(qualifiers);
    }


    @Override
    public void addQualifier(Class<?> qualifier) {
        data.addQualifier(qualifier);
    }


    @Override
    public boolean isVirtual() {
        return data.isVirtual();
    }


    @Override
    public void setVirtual(boolean virtual) {
        data.setVirtual(virtual);
    }


    public long getHungTaskThreshold() {
        return data.getHungTaskThreshold();
    }


    public void setHungTaskThreshold(long hungTaskThreshold) {
        data.setHungTaskThreshold(hungTaskThreshold);
    }


    public int getMaxAsync() {
        return data.getMaxAsync();
    }


    public void setMaxAsync(int maxAsync) {
        data.setMaxAsync(maxAsync);
    }


    public Properties getProperties() {
        return data.getProperties();
    }


    public void addManagedScheduledExecutorDefinitionDescriptor(ResourcePropertyDescriptor properties) {
        data.addManagedScheduledExecutorDefinitionDescriptor(properties.getName(), properties.getValue());
    }


    public void addManagedScheduledExecutorDefinitionDescriptor(String name, String value) {
        data.addManagedScheduledExecutorDefinitionDescriptor(name, value);
    }


    public ManagedScheduledExecutorDefinitionData getData() {
        return data;
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof ManagedScheduledExecutorDefinitionDescriptor) {
            ManagedScheduledExecutorDefinitionDescriptor another = (ManagedScheduledExecutorDefinitionDescriptor) object;
            return getName().equals(another.getName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }


    @Override
    public String toString() {
        return "ManagedScheduledExecutorDefinitionDescriptor{data=" + data + ')';
    }
}

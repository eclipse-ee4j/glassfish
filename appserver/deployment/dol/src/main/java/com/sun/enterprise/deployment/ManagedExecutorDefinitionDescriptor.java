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
import com.sun.enterprise.deployment.annotation.handlers.ManagedExecutorDefinitionData;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.glassfish.deployment.common.JavaEEResourceType;


/**
 * @author David Matejcek
 */
public class ManagedExecutorDefinitionDescriptor extends ResourceDescriptor implements ContextualResourceDefinition {

    private static final long serialVersionUID = 1L;

    private final ManagedExecutorDefinitionData data;

    public ManagedExecutorDefinitionDescriptor() {
        this(new ManagedExecutorDefinitionData(), MetadataSource.XML);
    }


    public ManagedExecutorDefinitionDescriptor(ManagedExecutorDefinitionData data, MetadataSource source) {
        this.data = data;
        setResourceType(JavaEEResourceType.MEDD);
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
    public List<String> getQualifiers() {
        return data.getQualifiers();
    }


    @Override
    public void addQualifier(String qualifier) {
        data.addQualifier(qualifier);
    }


    @Override
    public void setQualifiers(List<String> qualifiers) {
        data.setQualifiers(qualifiers);
    }


    @Override
    public boolean getUseVirtualThreads() {
        return data.getUseVirtualThreads();
    }


    @Override
    public void setUseVirtualThreads(boolean useVirtualThreads) {
        data.setUseVirtualThreads(useVirtualThreads);
    }


    public int getMaximumPoolSize() {
        return data.getMaximumPoolSize();
    }


    public void setMaximumPoolSize(int maximumPoolSize) {
        data.setMaximumPoolSize(maximumPoolSize);
    }


    public long getHungAfterSeconds() {
        return data.getHungAfterSeconds();
    }


    public void setHungAfterSeconds(long hungAfterSeconds) {
        data.setHungAfterSeconds(hungAfterSeconds);
    }


    public void addProperty(String key, String value) {
        data.addProperty(key, value);
    }


    public String getProperty(String key) {
        return data.getProperty(key);
    }


    public Properties getProperties() {
        return data.getProperties();
    }


    public void addManagedExecutorPropertyDescriptor(ResourcePropertyDescriptor property) {
        addManagedExecutorPropertyDescriptor(property.getName(), property.getValue());
    }


    public void addManagedExecutorPropertyDescriptor(String name, String value) {
        this.data.addManagedExecutorPropertyDescriptor(name, value);
    }


    public ManagedExecutorDefinitionData getData() {
        return data;
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof ManagedExecutorDefinitionDescriptor) {
            ManagedExecutorDefinitionDescriptor another = (ManagedExecutorDefinitionDescriptor) object;
            return getJndiName().equals(another.getJndiName());
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }


    @Override
    public String toString() {
        return "ManagedExecutorDefinitionDescriptor{data=" + data + ')';
    }
}

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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.annotation.handlers.ContextualResourceDefinition;
import com.sun.enterprise.deployment.annotation.handlers.ManagedScheduledExecutorDefinitionData;

import java.util.Properties;

import org.glassfish.deployment.common.JavaEEResourceType;

/**
 * @author David Matejcek
 */
public class ManagedScheduledExecutorDefinitionDescriptor extends ResourceDescriptor
    implements ContextualResourceDefinition {

    private static final long serialVersionUID = 1L;

    private final ManagedScheduledExecutorDefinitionData definition;

    public ManagedScheduledExecutorDefinitionDescriptor() {
        this(new ManagedScheduledExecutorDefinitionData(), null);
    }


    public ManagedScheduledExecutorDefinitionDescriptor(ManagedScheduledExecutorDefinitionData data, MetadataSource source) {
        this.definition = data;
        setResourceType(JavaEEResourceType.MSEDD);
        if (source != null) {
            setMetadataSource(source);
        }
    }


    @Override
    public String getName() {
        return this.definition.getName();
    }


    @Override
    public void setName(String name) {
        this.definition.setName(name);
    }


    @Override
    public String getContext() {
        return definition.getContext();
    }


    @Override
    public void setContext(String context) {
        definition.setContext(context);
    }


    public long getHungTaskThreshold() {
        return definition.getHungTaskThreshold();
    }


    public void setHungTaskThreshold(long hungTaskThreshold) {
        definition.setHungTaskThreshold(hungTaskThreshold);
    }


    public int getMaxAsync() {
        return definition.getMaxAsync();
    }


    public void setMaxAsync(int maxAsync) {
        definition.setMaxAsync(maxAsync);
    }


    public Properties getProperties() {
        return definition.getProperties();
    }


    public void setProperties(Properties properties) {
        definition.setProperties(properties);
    }


    public void addManagedScheduledExecutorDefinitionDescriptor(ResourcePropertyDescriptor properties) {
        definition.addManagedScheduledExecutorDefinitionDescriptor(properties.getName(), properties.getValue());
    }


    public void addManagedScheduledExecutorDefinitionDescriptor(String name, String value) {
        definition.addManagedScheduledExecutorDefinitionDescriptor(name, value);
    }


    public ManagedScheduledExecutorDefinitionData getData() {
        return definition;
    }


    @Override
    public String toString() {
        return "ManagedScheduledExecutorDefinitionDescriptor{definition=" + definition + ')';
    }
}

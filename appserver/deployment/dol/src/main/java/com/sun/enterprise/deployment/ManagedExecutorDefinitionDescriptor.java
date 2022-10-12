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

import com.sun.enterprise.deployment.annotation.factory.ManagedExecutorDefinitionData;
import com.sun.enterprise.deployment.annotation.handlers.ContextualResourceDefinition;

import java.util.Properties;

import org.glassfish.deployment.common.JavaEEResourceType;


/**
 * @author David Matejcek
 */
public class ManagedExecutorDefinitionDescriptor extends ResourceDescriptor implements ContextualResourceDefinition {

    private static final long serialVersionUID = 1L;

    private final ManagedExecutorDefinitionData definition;

    public ManagedExecutorDefinitionDescriptor() {
        this(new ManagedExecutorDefinitionData(), null);
    }


    public ManagedExecutorDefinitionDescriptor(ManagedExecutorDefinitionData data, MetadataSource source) {
        this.definition = data;
        setResourceType(JavaEEResourceType.MEDD);
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


    public int getMaximumPoolSize() {
        return definition.getMaximumPoolSize();
    }


    public void setMaximumPoolSize(int maximumPoolSize) {
        definition.setMaximumPoolSize(maximumPoolSize);
    }


    public long getHungAfterSeconds() {
        return definition.getHungAfterSeconds();
    }


    public void setHungAfterSeconds(long hungAfterSeconds) {
        definition.setHungAfterSeconds(hungAfterSeconds);
    }


    public void addProperty(String key, String value) {
        definition.addProperty(key, value);
    }


    public String getProperty(String key) {
        return definition.getProperty(key);
    }


    public Properties getProperties() {
        return definition.getProperties();
    }


    public void addManagedExecutorPropertyDescriptor(ResourcePropertyDescriptor property) {
        addManagedExecutorPropertyDescriptor(property.getName(), property.getValue());
    }


    public void addManagedExecutorPropertyDescriptor(String name, String value) {
        this.definition.addManagedExecutorPropertyDescriptor(name, value);
    }


    public ManagedExecutorDefinitionData getData() {
        return definition;
    }


    @Override
    public String toString() {
        return "ManagedExecutorDefinitionDescriptor{definition=" + definition + ')';
    }
}

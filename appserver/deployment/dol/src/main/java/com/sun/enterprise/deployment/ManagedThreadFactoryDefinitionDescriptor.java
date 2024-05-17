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
import com.sun.enterprise.deployment.annotation.handlers.ManagedThreadFactoryDefinitionData;

import java.util.Properties;

import org.glassfish.deployment.common.JavaEEResourceType;


/**
 * @author David Matejcek
 */
public class ManagedThreadFactoryDefinitionDescriptor extends ResourceDescriptor
    implements ContextualResourceDefinition {

    private static final long serialVersionUID = 6376196495209425819L;

    private final ManagedThreadFactoryDefinitionData data;

    public ManagedThreadFactoryDefinitionDescriptor() {
        this(new ManagedThreadFactoryDefinitionData(), MetadataSource.XML);
    }


    public ManagedThreadFactoryDefinitionDescriptor(ManagedThreadFactoryDefinitionData data, MetadataSource source) {
        this.data = data;
        setResourceType(JavaEEResourceType.MTFDD);
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
    public Class<?>[] getQualifiers() {
        return data.getQualifiers();
    }


    @Override
    public void setQualifiers(Class<?>[] qualifiers) {
        data.setQualifiers(qualifiers);
    }


    @Override
    public boolean isVirtual() {
        return data.isVirtual();
    }


    @Override
    public void setVirtual(boolean virtual) {
        data.setVirtual(virtual);
    }


    public int getPriority() {
        return data.getPriority();
    }


    public void setPriority(int priority) {
        data.setPriority(priority);
    }


    public Properties getProperties() {
        return data.getProperties();
    }


    public void setProperties(Properties properties) {
        data.setProperties(properties);
    }


    public void addManagedThreadFactoryPropertyDescriptor(ResourcePropertyDescriptor property) {
        addManagedThreadFactoryPropertyDescriptor(property.getName(), property.getValue());
    }


    public void addManagedThreadFactoryPropertyDescriptor(String name, String value) {
        data.addManagedThreadFactoryPropertyDescriptor(name, value);
    }


    public ManagedThreadFactoryDefinitionData getData() {
        return data;
    }


    @Override
    public String toString() {
        return "ManagedThreadFactoryDefinitionDescriptor[data=" + data + ']';
    }
}

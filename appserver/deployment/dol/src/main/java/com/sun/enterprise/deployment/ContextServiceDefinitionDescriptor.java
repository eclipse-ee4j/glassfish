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

package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.annotation.handlers.ConcurrencyResourceDefinition;
import com.sun.enterprise.deployment.annotation.handlers.ContextServiceDefinitionData;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.glassfish.deployment.common.JavaEEResourceType;


/**
 * @author David Matejcek
 */
public class ContextServiceDefinitionDescriptor extends ResourceDescriptor implements ConcurrencyResourceDefinition {

    private static final long serialVersionUID = 2537143519647534821L;

    private final ContextServiceDefinitionData data;

    public ContextServiceDefinitionDescriptor() {
        this(new ContextServiceDefinitionData(), MetadataSource.XML);
    }


    public ContextServiceDefinitionDescriptor(ContextServiceDefinitionData data, MetadataSource source) {
        this.data = data;
        setResourceType(JavaEEResourceType.CSDD);
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


    public Set<String> getCleared() {
        return data.getCleared();
    }


    public void setCleared(Set<String> cleared) {
        data.setCleared(cleared);
    }


    public void addCleared(String clearedItem) {
        data.addCleared(clearedItem);
    }


    public Set<String> getPropagated() {
        return data.getPropagated();
    }


    public void setPropagated(Set<String> propagated) {
        data.setPropagated(propagated);
    }


    public void addPropagated(String propagatedItem) {
        data.addPropagated(propagatedItem);
    }


    public Set<String> getUnchanged() {
        return data.getUnchanged();
    }


    public void setUnchanged(Set<String> unchanged) {
        data.setUnchanged(unchanged);
    }


    public void addUnchanged(String unchangedItem) {
        data.addUnchanged(unchangedItem);
    }


    public Properties getProperties() {
        return data.getProperties();
    }


    public void setProperties(Properties properties) {
        data.setProperties(properties);
    }


    public void addContextServiceExecutorDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        addContextServiceExecutorDescriptor(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }


    public void addContextServiceExecutorDescriptor(String name, String value) {
        data.addContextServiceExecutorDescriptor(name, value);
    }


    public ContextServiceDefinitionData getData() {
        return data;
    }


    @Override
    public boolean equals(Object object) {
        if (object instanceof ContextServiceDefinitionDescriptor) {
            ContextServiceDefinitionDescriptor another = (ContextServiceDefinitionDescriptor) object;
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
        return "ContextServiceDefinitionDescriptor[data=" + data + ']';
    }
}

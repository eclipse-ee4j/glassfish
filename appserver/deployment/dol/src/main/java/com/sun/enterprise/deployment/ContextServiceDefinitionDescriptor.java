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

import com.sun.enterprise.deployment.annotation.handlers.ContextServiceDefinitionData;

import java.util.Properties;
import java.util.Set;

import org.glassfish.deployment.common.JavaEEResourceType;


/**
 * @author David Matejcek
 */
public class ContextServiceDefinitionDescriptor extends ResourceDescriptor {

    private static final long serialVersionUID = 2537143519647534821L;

    private final ContextServiceDefinitionData definition;

    public ContextServiceDefinitionDescriptor() {
        this(new ContextServiceDefinitionData(), null);
    }


    public ContextServiceDefinitionDescriptor(ContextServiceDefinitionData data, MetadataSource source) {
        this.definition = data;
        setResourceType(JavaEEResourceType.CSDD);
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


    public Set<String> getCleared() {
        return definition.getCleared();
    }


    public void setCleared(Set<String> cleared) {
        definition.setCleared(cleared);
    }


    public void addCleared(String clearedItem) {
        definition.addCleared(clearedItem);
    }


    public Set<String> getPropagated() {
        return definition.getPropagated();
    }


    public void setPropagated(Set<String> propagated) {
        definition.setPropagated(propagated);
    }


    public void addPropagated(String propagatedItem) {
        definition.addPropagated(propagatedItem);
    }


    public Set<String> getUnchanged() {
        return definition.getUnchanged();
    }


    public void setUnchanged(Set<String> unchanged) {
        definition.setUnchanged(unchanged);
    }


    public void addUnchanged(String unchangedItem) {
        definition.addUnchanged(unchangedItem);
    }


    public Properties getProperties() {
        return definition.getProperties();
    }


    public void setProperties(Properties properties) {
        definition.setProperties(properties);
    }


    public void addContextServiceExecutorDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        addContextServiceExecutorDescriptor(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }


    public void addContextServiceExecutorDescriptor(String name, String value) {
        definition.addContextServiceExecutorDescriptor(name, value);
    }


    public ContextServiceDefinitionData getData() {
        return definition;
    }


    @Override
    public String toString() {
        return "ContextServiceDefinitionDescriptor[definition=" + definition + ']';
    }
}

/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.api;

import com.sun.enterprise.repository.ResourceProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.glassfish.resourcebase.resources.api.ResourceInfo;

/**
 * Base class for common JavaEE Resource implementation.
 */
public abstract class JavaEEResourceBase implements JavaEEResource, Serializable {

    private static final long serialVersionUID = 1L;
    private final ResourceInfo resourceInfo;
    private final Map<String, ResourceProperty> properties;
    private boolean enabled;
    private String description;

    public JavaEEResourceBase(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        this.properties = new HashMap<>();
    }

    @Override
    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setDescription(String value) {
        description = value;
    }

    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public abstract int getType();


    @Override
    public Set<ResourceProperty> getProperties() {
        Set<ResourceProperty> shallowCopy = new HashSet<>();
        Collection<ResourceProperty> collection = properties.values();
        for (ResourceProperty next : collection) {
            shallowCopy.add(next);
        }
        return shallowCopy;
    }


    @Override
    public void addProperty(ResourceProperty property) {
        properties.put(property.getName(), property);
    }


    @Override
    public boolean removeProperty(ResourceProperty property) {
        return properties.remove(property.getName()) != null;
    }


    @Override
    public ResourceProperty getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public JavaEEResource makeClone(ResourceInfo resourceInfo) {
        JavaEEResource clone = doClone(resourceInfo);
        Set<Entry<String, ResourceProperty>> entrySet = properties.entrySet();
        for (Entry<String, ResourceProperty> next : entrySet) {
            ResourceProperty propClone = new ResourcePropertyImpl(next.getKey());
            propClone.setValue(next.getValue());
            clone.addProperty(propClone);
        }
        clone.setEnabled(isEnabled());
        clone.setDescription(getDescription());
        return clone;
    }

    protected abstract JavaEEResource doClone(ResourceInfo resourceInfo);
}

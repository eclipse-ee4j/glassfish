/*
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
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import java.io.Serializable;
import java.util.*;

/**
 * Base class for common JavaEE Resource implementation.
 */
public abstract class JavaEEResourceBase implements JavaEEResource, Serializable {

    ResourceInfo resourceInfo;
    Map properties_;
    // START OF IASRI #4626188
    boolean enabled_;
    String description_;
    // END OF IASRI #4626188

    public JavaEEResourceBase(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        properties_ = new HashMap();
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    // START OF IASRI #4626188
    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setDescription(String value) {
        description_ = value;
    }

    public String getDescription() {
        return description_;
    }
    // END OF IASRI #4626188

    public abstract int getType();

    public Set getProperties() {
        Set shallowCopy = new HashSet();
        Collection collection = properties_.values();
        for (Iterator iter = collection.iterator(); iter.hasNext();) {
            ResourceProperty next = (ResourceProperty) iter.next();
            shallowCopy.add(next);
        }
        return shallowCopy;
    }

    public void addProperty(ResourceProperty property) {
        properties_.put(property.getName(), property);
    }

    public boolean removeProperty(ResourceProperty property) {
        Object removedObj = properties_.remove(property.getName());
        return (removedObj != null);
    }

    public ResourceProperty getProperty(String propertyName) {
        return (ResourceProperty) properties_.get(propertyName);
    }

    public JavaEEResource makeClone(ResourceInfo resourceInfo) {
        JavaEEResource clone = doClone(resourceInfo);
        Set entrySet = properties_.entrySet();
        for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
            Map.Entry next = (Map.Entry) iter.next();
            ResourceProperty propClone =
                    new ResourcePropertyImpl((String) next.getKey());
            propClone.setValue(next.getValue());

            clone.addProperty(propClone);
        }
        // START OF IASRI #4626188
        clone.setEnabled(isEnabled());
        clone.setDescription(getDescription());
        // END OF IASRI #4626188
        return clone;
    }

    protected String getPropsString() {
        StringBuffer propsBuffer = new StringBuffer();
        Set props = getProperties();
        if (!props.isEmpty()) {
            for (Iterator iter = props.iterator(); iter.hasNext();) {
                if (propsBuffer.length() == 0) {
                    propsBuffer.append("[ ");
                } else {
                    propsBuffer.append(" , ");
                }
                ResourceProperty next = (ResourceProperty) iter.next();
                propsBuffer.append(next.getName() + "=" + next.getValue());
            }
            propsBuffer.append(" ]");
        }
        return propsBuffer.toString();
    }

    protected abstract JavaEEResource doClone(ResourceInfo resourceInfo);
}

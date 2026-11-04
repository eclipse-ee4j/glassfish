/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.mirror;

import com.sun.rave.designtime.CategoryDescriptor;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.ValueHolder;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a component class or a non-component base class from a dependent
 * library, discovered using introspection.
 */
public final class IntrospectedClassInfo extends ClassInfo {

    /**
     * Bean info for the introspected class.
     */
    private final BeanInfo beanInfo;

    /**
     * Property info map.
     */
    private Map<String, PropertyInfo> propertyInfoMap;

    /**
     * Event info map.
     */
    private Map<String, EventInfo> eventInfoMap;

    /**
     * Category descriptors.
     */
    private Set<CategoryDescriptor> categoryDescriptors;

    /**
     * Method names.
     */
    private Set<String> methodNameSet;

    /**
     * Create a new instance.
     * @param bInfo bean info
     */
    IntrospectedClassInfo(final BeanInfo bInfo) {
        this.beanInfo = bInfo;
    }

    /**
     * Get the class name.
     * @return String
     */
    public String getName() {
        return this.beanInfo.getBeanDescriptor().getName();
    }

    @Override
    public String getClassName() {
        return this.beanInfo.getBeanDescriptor().getBeanClass()
                .getSimpleName();
    }

    @Override
    public String getPackageName() {
        return this.beanInfo.getBeanDescriptor().getBeanClass().getPackage()
                .getName();
    }

    /**
     * Get the bean info.
     * @return BeanInfo
     */
    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

    @Override
    public ClassInfo getSuperClassInfo() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isAssignableTo(final String qualifiedClassName) {
        try {
            Class superClass = Class.forName(qualifiedClassName);
            if (superClass.isAssignableFrom(this.getBeanInfo()
                    .getBeanDescriptor().getBeanClass())) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, PropertyInfo> getPropertyInfos() {
        return this.propertyInfoMap;
    }

    /**
     * Set the property info map.
     * @param propInfoMap new property info map
     */
    void setPropertyInfos(final Map<String, PropertyInfo> propInfoMap) {
        this.propertyInfoMap = propInfoMap;
    }

    @Override
    public Map<String, EventInfo> getEventInfos() {
        return this.eventInfoMap;
    }

    /**
     * Set the event info map.
     * @param evtInfoMap new event info map
     */
    void setEventInfos(final Map<String, EventInfo> evtInfoMap) {
        this.eventInfoMap = evtInfoMap;
    }

    @Override
    public PropertyInfo getDefaultPropertyInfo() {
        String defaultPropertyName = null;
        int index = this.beanInfo.getDefaultPropertyIndex();
        if (index >= 0) {
            defaultPropertyName = this.beanInfo.getPropertyDescriptors()[index]
                    .getName();
        } else {
                if (ValueHolder.class.isAssignableFrom(
                        this.getBeanInfo().getBeanDescriptor()
                                .getBeanClass())) {
                    defaultPropertyName = "value";
                }
        }
        if (defaultPropertyName == null) {
            return null;
        }
        return this.getPropertyInfos().get(defaultPropertyName);
    }

    @Override
    public EventInfo getDefaultEventInfo() {
        String defaultEventName = null;
        int index = this.beanInfo.getDefaultEventIndex();
        if (index >= 0) {
            defaultEventName = this.beanInfo.getEventSetDescriptors()[index]
                    .getName();
        } else {
            Class beanClass = this.getBeanInfo().getBeanDescriptor()
                    .getBeanClass();
            if (ActionSource.class.isAssignableFrom(beanClass)) {
                defaultEventName = "action";
            } else if (EditableValueHolder.class.isAssignableFrom(
                    beanClass)) {
                defaultEventName = "valueChange";
            }
        }
        if (defaultEventName == null) {
            return null;
        }
        return this.getEventInfos().get(defaultEventName);
    }

    @Override
    Set<String> getMethodNames() {
        if (this.methodNameSet == null) {
            this.methodNameSet = new HashSet<String>();
            for (MethodDescriptor method : this.getBeanInfo()
                    .getMethodDescriptors()) {
                this.methodNameSet.add(method.getName());
            }
        }
        return this.methodNameSet;
    }
}

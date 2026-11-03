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

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.ValueHolder;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Represents a component class or a non-component base class declared in the
 * current compilation unit. This class offers several different "views" of its
 * properties, which can be useful during source code generation. In addition to
 * providing a map of all declared properties, it provides a map which contains
 * all inherited properties.
 *
 */
public class DeclaredClassInfo extends DeclaredTypeInfo {

    /**
     * Inherited property info map.
     */
    private Map<String, PropertyInfo> inheritedPropertyInfoMap;

    /**
     * Inherited event info map.
     */
    private Map<String, EventInfo> inheritedEventInfoMap;

    /**
     * Create a new instance.
     * @param env annotation processing environment
     * @param decl type element representing the class
     */
    DeclaredClassInfo(final ProcessingEnvironment env, final TypeElement decl) {
        super(env, decl);
    }

    /**
     * Returns this class's default property. If no default property was
     * declared explicitly via a {@link com.sun.faces.annotation.Property}
     * annotation, and this type implements or extends
     * {@link jakarta.faces.component.ValueHolder}, then the {@code value}
     * property is made default.
     *
     * @return PropertyInfo
     */
    @Override
    public final PropertyInfo getDefaultPropertyInfo() {
        PropertyInfo defaultPropertyInfo = super.getDefaultPropertyInfo();
        if (defaultPropertyInfo == null) {
            if (this.getSuperClassInfo() != null) {
                defaultPropertyInfo = this.getSuperClassInfo()
                        .getDefaultPropertyInfo();
            }
            if (defaultPropertyInfo == null) {
                for (TypeMirror ifaceTypeMirror : getDeclaration()
                        .getInterfaces()) {
                    TypeElement ifaceType = (TypeElement) getEnv()
                            .getTypeUtils()
                            .asElement(ifaceTypeMirror);
                    if (ifaceType.getQualifiedName().toString().equals(
                            ValueHolder.class.getName())) {
                        defaultPropertyInfo = this.getPropertyInfos()
                                .get("value");
                        if (defaultPropertyInfo == null) {
                            defaultPropertyInfo = this
                                    .getInheritedPropertyInfos().get("value");
                        }
                    }
                }
            }
        }
        return defaultPropertyInfo;
    }

    /**
     * Returns this class's default Event. If no default Event was declared
     * explicitly via a {@link com.sun.faces.annotation.Event} annotation, and
     * this type implements or extends
     * {@link jakarta.faces.component.ValueHolder}, then the {@code value} Event
     * is made default.
     * @return EventInfo
     */
    @Override
    public final EventInfo getDefaultEventInfo() {
        EventInfo defaultEventInfo = super.getDefaultEventInfo();
        if (defaultEventInfo == null) {
            if (this.getSuperClassInfo() != null) {
                defaultEventInfo = this.getSuperClassInfo()
                        .getDefaultEventInfo();
            }
            if (defaultEventInfo == null) {
                for (TypeMirror ifaceTypeMirror : getDeclaration()
                        .getInterfaces()) {
                    TypeElement ifaceType = (TypeElement) getEnv()
                            .getTypeUtils()
                            .asElement(ifaceTypeMirror);
                    if (ifaceType.getQualifiedName().toString()
                            .equals(EditableValueHolder.class.getName())) {
                        defaultEventInfo = this.getEventInfos()
                                .get("valueChange");
                        if (defaultEventInfo == null) {
                            defaultEventInfo = this.getInheritedEventInfos()
                                    .get("valueChange");
                        }
                    }
                    if (ifaceType.getQualifiedName().toString()
                            .equals(ActionSource.class.getName())) {
                        defaultEventInfo = this.getEventInfos().get("action");
                        if (defaultEventInfo == null) {
                            defaultEventInfo = this.getInheritedEventInfos()
                                    .get("action");
                        }
                    }
                }
            }
        }
        return defaultEventInfo;
    }

    /**
     * Returns a map of all properties inherited by this class, but not
     * overridden by properties in this class.
     * @return {@code Map<String, PropertyInfo>}
     */
    public final Map<String, PropertyInfo> getInheritedPropertyInfos() {
        if (this.inheritedPropertyInfoMap == null) {
            ClassInfo cInfo = this.getSuperClassInfo();
            this.inheritedPropertyInfoMap = new HashMap<String, PropertyInfo>();
            while (cInfo != null) {
                for (PropertyInfo propInfo
                        : cInfo.getPropertyInfos().values()) {
                    String name = propInfo.getName();
                    if (!getPropertyInfos().containsKey(name)
                            && !this.inheritedPropertyInfoMap
                                    .containsKey(name)) {
                        this.inheritedPropertyInfoMap.put(name, propInfo);
                    }
                }
                cInfo = cInfo.getSuperClassInfo();
            }
        }
        return this.inheritedPropertyInfoMap;
    }

    /**
     * Returns a map of all events inherited by this class, but not overridden
     * by properties in this class.
     * @return map of inherited events
     */
    public final Map<String, EventInfo> getInheritedEventInfos() {
        if (this.inheritedEventInfoMap == null) {
            ClassInfo cInfo = this.getSuperClassInfo();
            this.inheritedEventInfoMap = new HashMap<String, EventInfo>();
            while (cInfo != null) {
                for (EventInfo eventInfo : cInfo.getEventInfos().values()) {
                    String name = eventInfo.getName();
                    if (!getEventInfos().containsKey(name)
                            && !this.inheritedEventInfoMap.containsKey(name)) {
                        this.inheritedEventInfoMap.put(name, eventInfo);
                    }
                }
                cInfo = cInfo.getSuperClassInfo();
            }
        }
        return this.inheritedEventInfoMap;
    }
}

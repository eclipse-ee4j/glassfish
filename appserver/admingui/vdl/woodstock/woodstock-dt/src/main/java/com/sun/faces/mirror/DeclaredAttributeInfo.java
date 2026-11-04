/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an attribute for a property declared on a class in the current
 * compilation unit.
 */
public final class DeclaredAttributeInfo implements AttributeInfo {

    /**
     * Attribute name.
     */
    private static final String NAME = "name";

    /**
     * Required flag key.
     */
    private static final String IS_REQUIRED = "isRequired";

    /**
     * Bind-able flag key.
     */
    private static final String IS_BINDABLE = "isBindable";

    /**
     * Annotation value map.
     */
    private final Map<String, Object> annotationValueMap;

    /**
     * Parent property info.
     */
    private PropertyInfo parentPropertyInfo;

    /**
     * Method signature.
     */
    private String methodSignature;

    /**
     * Description.
     */
    private String description;

    /**
     * Create a new instance.
     * @param parentPropInfo parent property info
     */
    DeclaredAttributeInfo(final PropertyInfo parentPropInfo) {
        this(null, parentPropInfo);
    }

    /**
     * Create a new instance.
     * @param attrInfo attribute info
     */
    DeclaredAttributeInfo(final AttributeInfo attrInfo) {
        this.annotationValueMap = new HashMap<String, Object>();
        this.annotationValueMap.put(NAME, attrInfo.getName());
        this.annotationValueMap.put(IS_REQUIRED, attrInfo.isRequired());
        this.annotationValueMap.put(IS_BINDABLE, attrInfo.isBindable());
        this.setDescription(attrInfo.getDescription());
        this.setMethodSignature(attrInfo.getMethodSignature());
        if (attrInfo instanceof DeclaredAttributeInfo) {
            this.parentPropertyInfo =
                    ((DeclaredAttributeInfo) attrInfo).parentPropertyInfo;
        }
    }

    /**
     * Create a new instance.
     * @param annotValueMap annotation value map
     * @param parentPropInfo parent property info
     */
    DeclaredAttributeInfo(final Map<String, Object> annotValueMap,
            final PropertyInfo parentPropInfo) {

        this.annotationValueMap = annotValueMap;
        this.parentPropertyInfo = parentPropInfo;
    }

    @Override
    public String getName() {
        if (this.annotationValueMap == null) {
            return this.parentPropertyInfo.getName();
        }
        String name = (String) this.annotationValueMap.get(NAME);
        if (name == null) {
            return this.parentPropertyInfo.getName();
        }
        return name;
    }

    @Override
    public boolean isRequired() {
        if (this.annotationValueMap == null) {
            return false;
        }
        return Boolean.TRUE.equals(this.annotationValueMap.get(IS_REQUIRED));
    }

    @Override
    public boolean isBindable() {
        if (this.annotationValueMap == null) {
            return true;
        }
        return !Boolean.FALSE.equals(this.annotationValueMap.get(IS_BINDABLE));
    }

    @Override
    public String getMethodSignature() {
        return this.methodSignature;
    }

    /**
     * Set the method signature.
     * @param methodSig new method signature
     */
    void setMethodSignature(final String methodSig) {
        this.methodSignature = methodSig;
    }

    @Override
    public String getDescription() {
        if (description == null && this.parentPropertyInfo != null) {
            return ((DeclaredPropertyInfo) this.parentPropertyInfo)
                    .getDocComment();
        }
        return this.description;
    }

    /**
     * Set the attribute description.
     * @param desc new description
     */
    void setDescription(final String desc) {
        this.description = desc;
    }

    @Override
    public String getWriteMethodName() {
        String name = this.getName();
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}

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

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * Represents a property of a class from a dependent library, discovered using
 * introspection.
 */
public final class IntrospectedPropertyInfo extends PropertyInfo {

    /**
     * Property descriptor.
     */
    private final PropertyDescriptor propertyDescriptor;

    /**
     * Category info.
     */
    private CategoryInfo categoryInfo;

    /**
     * Create a new instance.
     * @param propDesc property descriptor
     */
    IntrospectedPropertyInfo(final PropertyDescriptor propDesc) {
        this.propertyDescriptor = propDesc;
    }

    /**
     * Get the property descriptor.
     * @return PropertyDescriptor
     */
    public PropertyDescriptor getPropertyDescriptor() {
        return this.propertyDescriptor;
    }

    @Override
    public String getName() {
        return this.propertyDescriptor.getName();
    }

    @Override
    public String getInstanceName() {
        String name = this.getName();
        if (PropertyInfo.JAVA_KEYWORD_PATTERN.matcher(name).matches()) {
            return "_" + name;
        }
        return name;
    }

    @Override
    public String getType() {
        return this.propertyDescriptor.getPropertyType().getName();
    }

    @Override
    public String getWriteMethodName() {
        Method method = this.propertyDescriptor.getWriteMethod();
        if (method == null) {
            return null;
        }
        return method.getName();
    }

    @Override
    public String getReadMethodName() {
        Method method = this.propertyDescriptor.getReadMethod();
        if (method == null) {
            return null;
        }
        return method.getName();
    }

    @Override
    public String getShortDescription() {
        return this.propertyDescriptor.getShortDescription();
    }

    @Override
    public String getDisplayName() {
        return this.propertyDescriptor.getDisplayName();
    }

    @Override
    public boolean isHidden() {
        return this.propertyDescriptor.isHidden();
    }

    @Override
    public String getEditorClassName() {
        Class editorClass = this.propertyDescriptor.getPropertyEditorClass();
        if (editorClass == null) {
            return null;
        }
        return editorClass.getName();
    }

    @Override
    public CategoryInfo getCategoryInfo() {
        return this.categoryInfo;
    }

    /**
     * Set the category info.
     * @param catInfo new category info
     */
    void setCategoryInfo(final CategoryInfo catInfo) {
        this.categoryInfo = catInfo;
    }

    @Override
    String getCategoryReferenceName() {
        if (this.propertyDescriptor
                .getValue(Constants.PropertyDescriptor.CATEGORY) != null) {
            return ((CategoryDescriptor) this.propertyDescriptor
                    .getValue(Constants.PropertyDescriptor.CATEGORY))
                    .getName();
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PropertyInfo)) {
            return false;
        }
        PropertyInfo that = (PropertyInfo) obj;
        if (!this.getName().equals(that.getName())) {
            return false;
        }
        String thisReadName = this.getReadMethodName();
        String thatReadName = that.getReadMethodName();
        if (thisReadName != null && (thatReadName == null
                || !thatReadName.equals(thisReadName))) {
            return false;
        }
        if (thatReadName == null && thatReadName != null) {
            return false;
        }
        String thisWriteName = this.getReadMethodName();
        String thatWriteName = that.getReadMethodName();
        if (thisWriteName != null && (thatWriteName == null
                || !thatWriteName.equals(thisWriteName))) {
            return false;
        }
        return !(thatWriteName == null && thatWriteName != null);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash;
        if (this.propertyDescriptor != null) {
            hash = hash + this.propertyDescriptor.hashCode();
        }
        hash = 41 * hash;
        if (this.categoryInfo != null) {
            hash = hash + this.categoryInfo.hashCode();
        }
        return hash;
    }

    @Override
    public AttributeInfo getAttributeInfo() {
        AttributeDescriptor attributeDescriptor
                = (AttributeDescriptor) this.propertyDescriptor.getValue(
                        Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
        if (attributeDescriptor == null) {
            return null;
        }
        return new IntrospectedAttributeInfo(attributeDescriptor);
    }
}

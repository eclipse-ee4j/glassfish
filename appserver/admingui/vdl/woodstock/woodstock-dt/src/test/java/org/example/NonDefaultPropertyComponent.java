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
package org.example;

import com.sun.faces.annotation.Attribute;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

/**
 * A non-default component.
 */
@Component()
public class NonDefaultPropertyComponent {

    /**
     * A property that is not an attribute.
     */
    @Property(isAttribute = false)
    private String nonAttribute;

    /**
     * A read-only property.
     */
    @Property(isAttribute = false)
    private String readOnlyProperty;

    /**
     * A write-only property.
     */
    @Property()
    private String writeOnlyProperty;

    /**
     * A categorized property.
     */
    @Property(category = "category")
    private String categorizedProperty;

    /**
     * A hidden property.
     */
    @Property(isHidden = true)
    private String hiddenProperty;

    /**
     * A property that serves as this component's default property.
     */
    @Property(isDefault = true)
    private String defaultProperty;

    @Property(name = "namedProperty")
    private String namedPropertyField;

    @Property(name = "readNamedProperty", readMethodName = "getDifferentReadNamedProperty")
    private String readNamedPropertyField;

    @Property(name = "writeNamedProperty", writeMethodName = "setDifferentWriteNamedProperty")
    private String writeNamedPropertyField;

    @Property(
            name = "bothNamedProperty",
            writeMethodName = "setDifferentBothNamedProperty",
            readMethodName = "getDifferentBothNamedProperty")
    private String differentNamedPropertyField;

    /**
     * A property with a special name.
     */
    @Property(attribute = @Attribute(name = "specialNamedAttribute"))
    private String namedAttribute;

    /**
     * A required property.
     */
    @Property(attribute = @Attribute(isRequired = true))
    private String requiredAttribute;

    public String getNonAttribute() {
        return this.nonAttribute;
    }

    public void setNonAttribute(String nonAttribute) {
        this.nonAttribute = nonAttribute;
    }

    public String getNamedAttribute() {
        return this.namedAttribute;
    }

    public void setNamedAttribute(String namedAttribute) {
        this.namedAttribute = namedAttribute;
    }

    public String getRequiredAttribute() {
        return this.requiredAttribute;
    }

    public void setRequiredAttribute(String requiredAttribute) {
        this.requiredAttribute = requiredAttribute;
    }

    public String getReadOnlyProperty() {
        return this.readOnlyProperty;
    }

    public void setWriteOnlyProperty(String writeOnlyProperty) {
        this.writeOnlyProperty = writeOnlyProperty;
    }

    public String getCategorizedProperty() {
        return this.categorizedProperty;
    }

    public void setCategorizedProperty(String categorizedProperty) {
        this.categorizedProperty = categorizedProperty;
    }

    public String getHiddenProperty() {
        return this.hiddenProperty;
    }

    public void setHiddenProperty(String hiddenProperty) {
        this.hiddenProperty = hiddenProperty;
    }

    public String getDefaultProperty() {
        return this.defaultProperty;
    }

    public void setDefaultProperty(String defaultProperty) {
        this.defaultProperty = defaultProperty;
    }

    public String getNamedProperty() {
        return this.namedPropertyField;
    }

    public void setNamedProperty(String namedProperty) {
        this.namedPropertyField = namedProperty;
    }

    public String getDifferentReadNamedProperty() {
        return this.readNamedPropertyField;
    }

    public void setReadNamedProperty(String readNamedProperty) {
        this.readNamedPropertyField = readNamedProperty;
    }

    public String getWriteNamedProperty() {
        return this.differentNamedPropertyField;
    }

    public void setDifferentWriteNamedProperty(String writeNamedProperty) {
        this.differentNamedPropertyField = writeNamedProperty;
    }

    public String getDifferentBothNamedProperty() {
        return this.differentNamedPropertyField;
    }

    public void setDifferentBothNamedProperty(String value) {
        this.differentNamedPropertyField = value;
    }
}

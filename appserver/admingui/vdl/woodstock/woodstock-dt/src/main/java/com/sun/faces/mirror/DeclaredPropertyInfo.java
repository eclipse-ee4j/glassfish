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

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/**
 * Represents a property declared on a component class or non-component base
 * class, in the current compilation unit.
 */
public final class DeclaredPropertyInfo extends PropertyInfo {

    /**
     * Property name key.
     */
    static final String NAME = "name";

    /**
     * Property display name key.
     */
    static final String DISPLAY_NAME = "displayName";

    /**
     * Property short description key.
     */
    static final String SHORT_DESCRIPTION = "shortDescription";

    /**
     * Property editor class name key.
     */
    static final String EDITOR_CLASS_NAME = "editorClassName";

    /**
     * Property help key.
     */
    static final String HELP_KEY = "helpKey";

    /**
     * Property category key.
     */
    static final String CATEGORY = "category";

    /**
     * Property default flag key.
     */
    static final String IS_DEFAULT = "isDefault";

    /**
     * Property isAttribute key.
     */
    static final String IS_ATTRIBUTE = "isAttribute";

    /**
     * Property isHidden key.
     */
    static final String IS_HIDDEN = "isHidden";

    /**
     * Property attribute key.
     */
    static final String ATTRIBUTE = "attribute";

    /**
     * Property read method name key.
     */
    static final String READ_METHOD_NAME = "readMethodName";

    /**
     * Property write method name key.
     */
    static final String WRITE_METHOD_NAME = "writeMethodName";

    /**
     * Element representing the declared property.
     */
    private final Element decl;

    /**
     * Annotation value map.
     */
    private final Map<String, Object> annotationValueMap;

    /**
     * Annotation processing environment.
     */
    private final ProcessingEnvironment env;

    /**
     * Property name.
     */
    private String name;

    /**
     * Property type.
     */
    private String type;

    /**
     * Property write method name.
     */
    private String writeMethodName;

    /**
     * Property read method name.
     */
    private String readMethodName;

    /**
     * Property category info.
     */
    private CategoryInfo categoryInfo;

    /**
     * Property attribute info.
     */
    private AttributeInfo attributeInfo;

    /**
     * Create a new instance.
     * @param annotValueMap annotation value map
     * @param eltDecl element declaration representing this property
     * @param processingEnv annotation processing environment
     */
    DeclaredPropertyInfo(final Map<String, Object> annotValueMap,
            final Element eltDecl, final ProcessingEnvironment processingEnv) {

        this.annotationValueMap = annotValueMap;
        this.decl = eltDecl;
        this.env = processingEnv;
    }

    /**
     * Get the annotation value map.
     * @return {@code Map<String, Object>}
     */
    Map<String, Object> getAnnotationValueMap() {
        return annotationValueMap;
    }

    /**
     * Get the declaration element for this property.
     * @return Element
     */
    public Element getDeclaration() {
        return this.decl;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name for this property.
     * @param propName new property name
     */
    void setName(final String propName) {
        this.name = propName;
    }

    @Override
    public String getInstanceName() {
        String instanceName = this.getName();
        if (PropertyInfo.JAVA_KEYWORD_PATTERN.matcher(
                instanceName).matches()) {
            return "_" + instanceName;
        }
        return instanceName;
    }

    @Override
    public String getDisplayName() {
        String displayName = (String) this.annotationValueMap
                .get(DISPLAY_NAME);
        if (displayName == null) {
            displayName = this.getName();
        }
        return displayName;
    }

    @Override
    public String getShortDescription() {
        String shortDescription = (String) this.annotationValueMap
                .get(SHORT_DESCRIPTION);
        if (shortDescription == null) {
            String comment = this.getDocComment();
            if (comment == null || comment.trim().length() == 0) {
                shortDescription = this.getDisplayName();
            } else {
                char[] chars = comment.toCharArray();
                StringBuilder buffer = new StringBuilder();
                int index = 0;
                while (index < chars.length
                        && Character.isSpaceChar(chars[index])) {
                    index++;
                }
                OUTER:
                while (index < chars.length) {
                    switch (chars[index]) {
                        case '<':
                            index++;
                            while (index < chars.length
                                    && chars[index] != '>') {
                                index++;
                            }   break;
                        case '\n':
                            buffer.append(" ");
                            break;
                        case '"':
                            buffer.append("&quot;");
                            break;
                        case '.':
                            if (index == chars.length - 1
                                    || Character.isSpaceChar(
                                            chars[index + 1])) {
                                break OUTER;
                            }
                            buffer.append('.');
                            break;
                        default:
                            buffer.append(chars[index]);
                            break;
                    }
                    index++;
                }
                shortDescription = buffer.toString();
            }
        }
        return shortDescription;
    }

    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Set the property type.
     * @param propType new property type
     */
    void setType(final String propType) {
        this.type = propType;
    }

    @Override
    public String getWriteMethodName() {
        String methodName = this.writeMethodName;
        if (methodName == null) {
            methodName = (String) this.annotationValueMap
                    .get(WRITE_METHOD_NAME);
        }
        return methodName;
    }

    /**
     * Set the write method name.
     * @param methodName new write method name
     */
    void setWriteMethodName(final String methodName) {
        this.writeMethodName = methodName;
    }

    @Override
    public String getReadMethodName() {
        String methodName = this.readMethodName;
        if (methodName == null) {
            methodName = (String) this.annotationValueMap
                    .get(READ_METHOD_NAME);
        }
        return methodName;
    }

    /**
     * Set the read method name.
     * @param methodName new read method name
     */
    void setReadMethodName(final String methodName) {
        this.readMethodName = methodName;
    }

    @Override
    public String getEditorClassName() {
        return (String) this.annotationValueMap.get(EDITOR_CLASS_NAME);
    }

    @Override
    public boolean isHidden() {
        if (this.annotationValueMap.containsKey(IS_HIDDEN)) {
            return (Boolean) this.annotationValueMap.get(IS_HIDDEN);
        }
        return false;
    }

    @Override
    public CategoryInfo getCategoryInfo() {
        return this.categoryInfo;
    }

    /**
     * Set the category info for this property.
     * @param catInfo new category info
     */
    void setCategoryInfo(final CategoryInfo catInfo) {
        this.categoryInfo = catInfo;
    }

    @Override
    String getCategoryReferenceName() {
        return (String) this.annotationValueMap.get(CATEGORY);
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
        if (thisReadName != null
                && (thatReadName == null
                || !thatReadName.equals(thisReadName))) {
            return false;
        }
        if (thatReadName == null && thatReadName != null) {
            return false;
        }
        String thisWriteName = this.getReadMethodName();
        String thatWriteName = that.getReadMethodName();
        if (thisWriteName != null
                && (thatWriteName == null
                 || !thatWriteName.equals(thisWriteName))) {
            return false;
        }
        return !(thatWriteName == null && thatWriteName != null);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash;
        if (this.decl != null) {
            hash = hash + this.decl.hashCode();
        }
        hash = 31 * hash;
        if (this.name != null) {
            hash = hash + this.name.hashCode();
        }
        hash = 31 * hash;
        if (this.type != null) {
            hash = hash + this.type.hashCode();
        }
        return hash;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AttributeInfo getAttributeInfo() {
        if (this.attributeInfo == null) {
            if (Boolean.FALSE
                    .equals(this.annotationValueMap.get(IS_ATTRIBUTE))) {
                return null;
            }
            if (this.annotationValueMap.containsKey(ATTRIBUTE)) {
                this.attributeInfo = new DeclaredAttributeInfo(
                        (Map<String, Object>) this.annotationValueMap
                                .get(ATTRIBUTE), this);
            } else {
                this.attributeInfo = new DeclaredAttributeInfo(this);
            }
        }
        return this.attributeInfo;
    }

    /**
     * Set the attribute info.
     * @param attrInfo new attribute info
     */
    void setAttributeInfo(final AttributeInfo attrInfo) {
        this.attributeInfo = attrInfo;
    }

    /**
     * Update any missing values in this property info with the values in the
     * property info specified. Values will be copies only if they correspond to
     * annotation elements, and only if the elements where not explicitly
     * declared for this property. Also, if a read or write method is inherited
     * but not overridden, the method names will be copied.
     * @param propertyInfo property info
     */
    void updateInheritedValues(final PropertyInfo propertyInfo) {
        if (!this.annotationValueMap.containsKey(DISPLAY_NAME)) {
            this.annotationValueMap.put(DISPLAY_NAME,
                    propertyInfo.getDisplayName());
        }
        if (!this.annotationValueMap.containsKey(SHORT_DESCRIPTION)
                && this.getDocComment() == null) {
            this.annotationValueMap.put(SHORT_DESCRIPTION,
                    propertyInfo.getShortDescription());
        }
        if (!this.annotationValueMap.containsKey(EDITOR_CLASS_NAME)) {
            this.annotationValueMap.put(EDITOR_CLASS_NAME,
                    propertyInfo.getEditorClassName());
        }
        if (!this.annotationValueMap.containsKey(IS_HIDDEN)) {
            this.annotationValueMap.put(IS_HIDDEN,
                    propertyInfo.isHidden());
        }
        if (!this.annotationValueMap.containsKey(CATEGORY)) {
            this.annotationValueMap.put(CATEGORY,
                    propertyInfo.getCategoryReferenceName());
        }
        if (!this.annotationValueMap.containsKey(IS_ATTRIBUTE)) {
            AttributeInfo attrInfo = propertyInfo.getAttributeInfo();
            if (attrInfo == null) {
                this.annotationValueMap.put(IS_ATTRIBUTE, Boolean.FALSE);
                this.attributeInfo = null;
            } else {
                this.annotationValueMap.put(IS_ATTRIBUTE, Boolean.TRUE);
                this.attributeInfo = new DeclaredAttributeInfo(attrInfo);
                if (this.getDocComment() != null) {
                    ((DeclaredAttributeInfo) this.attributeInfo)
                            .setDescription(this.getDocComment());
                }
            }
        }
        if (getReadMethodName() == null) {
            setReadMethodName(propertyInfo.getReadMethodName());
        }
        if (getWriteMethodName() == null) {
            setWriteMethodName(propertyInfo.getWriteMethodName());
        }
        if (getCategoryInfo() == null && getCategoryReferenceName() != null) {
            this.setCategoryInfo(propertyInfo.getCategoryInfo());
        }
    }

    /**
     * Returns the JavaDoc comments associated with the member field or method
     * where this property was defined.
     * @return String
     */
    public String getDocComment() {
        return env.getElementUtils().getDocComment(getDeclaration());
    }
}

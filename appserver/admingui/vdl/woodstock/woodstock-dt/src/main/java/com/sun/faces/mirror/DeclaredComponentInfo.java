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
import javax.lang.model.element.TypeElement;

/**
 * Represents a JSF component class declared in the current compilation unit.
 */
public final class DeclaredComponentInfo extends DeclaredClassInfo {

    /**
     * Type key.
     */
    static final String TYPE = "type";

    /**
     * Family key.
     */
    static final String FAMILY = "family";

    /**
     * Display name key.
     */
    static final String DISPLAY_NAME = "displayName";

    /**
     * Instance name key.
     */
    static final String INSTANCE_NAME = "instanceName";

    /**
     * Tag name key.
     */
    static final String TAG_NAME = "tagName";

    /**
     * Tag renderer type key.
     */
    static final String TAG_RENDERER_TYPE = "tagRendererType";

    /**
     * Short description key.
     */
    static final String SHORT_DESCRIPTION = "shortDescription";

    /**
     * Help key.
     */
    static final String HELP_KEY = "helpKey";

    /**
     * isContainer flag key.
     */
    static final String IS_CONTAINER = "isContainer";

    /**
     * isTag flag key.
     */
    static final String IS_TAG = "isTag";

    /**
     * Properties help key.
     */
    static final String PROPERTIES_HELP_KEY = "propertiesHelpKey";

    /**
     * Tag description.
     */
    private String tagDescription;

    /**
     * Tag renderer type.
     */
    private String tagRendererType;

    /**
     * Annotation value map.
     */
    private final Map<String, Object> annotationValues;

    /**
     * TypeElement representing the class declaration for this component.
     */
    private TypeElement tagClassDeclaration;

    /**
     * Create a new instance.
     * @param env annotation processing environment
     * @param values annotation value map
     * @param decl type declaration
     */
    DeclaredComponentInfo(final ProcessingEnvironment env,
            final Map<String, Object> values, final TypeElement decl) {

        super(env, decl);
        this.annotationValues = values;
    }

    /**
     * Get the component type.
     * @return String
     */
    public String getType() {
        String type = (String) this.annotationValues.get(TYPE);
        if (type == null) {
            type = getDeclaration().getQualifiedName().toString();
        }
        return type;
    }

    /**
     * Get the component family.
     * @return String
     */
    public String getFamily() {
        String family = (String) this.annotationValues.get(FAMILY);
        if (family == null) {
            family = this.getType();
        }
        return family;
    }

    /**
     * Get the short description.
     * @return String
     */
    public String getShortDescription() {
        String shortDescription = (String) this.annotationValues
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
                            }
                            break;
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

    /**
     * Get the display name.
     * @return String
     */
    public String getDisplayName() {
        String displayName = (String) this.annotationValues.get(DISPLAY_NAME);
        if (displayName == null) {
            displayName = getDeclaration().getSimpleName().toString();
        }
        return displayName;
    }

    /**
     * Get the instance name.
     * @return String
     */
    public String getInstanceName() {
        String instanceName = (String) this.annotationValues
                .get(INSTANCE_NAME);
        if (instanceName == null) {
            String name = getDeclaration().getSimpleName().toString();
            instanceName = name.substring(0, 1).toLowerCase()
                    + name.substring(1);
        }
        return instanceName;
    }

    /**
     * Get the isTag flag value.
     * @return {@code true} if is a tag, {@code false} otherwise
     */
    public boolean isTag() {
        Boolean isTag = (Boolean) this.annotationValues.get(IS_TAG);
        if (isTag == null) {
            return true;
        }
        return isTag;
    }

    /**
     * Get the tag name.
     * @return String
     */
    public String getTagName() {
        String tagName = (String) this.annotationValues.get(TAG_NAME);
        if (tagName == null) {
            tagName = this.getInstanceName();
        }
        return tagName;
    }

    /**
     * Get the tag renderer type.
     * @return String
     */
    public String getTagRendererType() {
        if (this.tagRendererType == null) {
            return (String) this.annotationValues.get(TAG_RENDERER_TYPE);
        }
        return this.tagRendererType;
    }

    /**
     * Set the tag renderer type.
     * @param newTagRendererType new tag renderer type
     */
    void setTagRendererType(final String newTagRendererType) {
        this.tagRendererType = newTagRendererType;
    }

    /**
     * Returns the tag class declaration that was found in the current
     * compilation unit for this component, or null if none was found.
     *
     * @return TypeElement
     */
    public TypeElement getTagClassDeclaration() {
        return this.tagClassDeclaration;
    }

    /**
     * Set the tag class declaration.
     * @param typeDecl new tag class declaration
     */
    void setTagClassDeclaration(final TypeElement typeDecl) {
        this.tagClassDeclaration = typeDecl;
    }

    /**
     * Get the tag class qualifier name.
     * @return String
     */
    public String getTagClassQualifiedName() {
        TypeElement tagClassDecl = this.getTagClassDeclaration();
        if (tagClassDecl == null) {
            return this.getQualifiedName() + "Tag";
        }
        return tagClassDecl.getQualifiedName().toString();
    }

    /**
     * Returns the description of this component's tag, appropriate for use as a
     * description element in the taglib configuration. If no tag description
     * was set, returns the class doc comment.
     *
     * @return String
     */
    public String getTagDescription() {
        if (tagDescription == null) {
            return this.getDocComment();
        }
        return this.tagDescription;
    }

    /**
     * Set the tag description.
     * @param newTagDesc new tag description
     */
    void setTagDescription(final String newTagDesc) {
        this.tagDescription = newTagDesc;
    }

    /**
     * Get the help key.
     * @return String
     */
    public String getHelpKey() {
        String helpKey = (String) this.annotationValues.get(HELP_KEY);
        if (helpKey == null) {
            return "";
        }
        return helpKey;
    }

    /**
     * Get the help key properties.
     * @return String
     */
    public String getPropertiesHelpKey() {
        String propertiesHelpKey = (String) this.annotationValues
                .get(PROPERTIES_HELP_KEY);
        if (propertiesHelpKey == null) {
            return "";
        }
        return propertiesHelpKey;
    }

    /**
     * Get the isContainer flag value.
     * @return {@code true} if a container, {@code false} otherwise
     */
    public Boolean isContainer() {
        if (Boolean.TRUE.equals(this.annotationValues.get(IS_CONTAINER))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
